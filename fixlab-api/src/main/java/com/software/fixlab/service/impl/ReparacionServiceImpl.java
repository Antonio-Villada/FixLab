package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.*;
import com.software.fixlab.dto.resp.*;
import com.software.fixlab.entity.*;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExisteEquipoException;
import com.software.fixlab.exception.NoExisteReparacionException;
import com.software.fixlab.exception.NoExisteTallerException;
import com.software.fixlab.exception.ResourceNotFoundException;
import com.software.fixlab.repository.*;
import com.software.fixlab.service.interfaces.ReparacionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReparacionServiceImpl implements ReparacionService {

    private static final Logger log = LoggerFactory.getLogger(ReparacionServiceImpl.class);

    private static final Map<EstadoReparacion, Set<EstadoReparacion>> TRANSICIONES_PERMITIDAS;

    static {
        Map<EstadoReparacion, Set<EstadoReparacion>> m = new EnumMap<>(EstadoReparacion.class);
        m.put(EstadoReparacion.RECIBIDO, EnumSet.of(EstadoReparacion.EN_DIAGNOSTICO, EstadoReparacion.CANCELADO));
        m.put(EstadoReparacion.EN_DIAGNOSTICO,
                EnumSet.of(EstadoReparacion.COTIZADO_PENDIENTE_APROBACION, EstadoReparacion.CANCELADO));
        m.put(EstadoReparacion.COTIZADO_PENDIENTE_APROBACION,
                EnumSet.of(EstadoReparacion.APROBADO, EstadoReparacion.EN_DIAGNOSTICO, EstadoReparacion.CANCELADO));
        m.put(EstadoReparacion.APROBADO, EnumSet.of(EstadoReparacion.EN_REPARACION, EstadoReparacion.CANCELADO));
        m.put(EstadoReparacion.EN_REPARACION, EnumSet.of(EstadoReparacion.EN_PRUEBAS, EstadoReparacion.CANCELADO));
        m.put(EstadoReparacion.EN_PRUEBAS,
                EnumSet.of(EstadoReparacion.LISTO_ENTREGA, EstadoReparacion.EN_REPARACION));
        m.put(EstadoReparacion.LISTO_ENTREGA, EnumSet.of(EstadoReparacion.ENTREGADO));
        TRANSICIONES_PERMITIDAS = Collections.unmodifiableMap(m);
    }

    /**
     * Valor único acotado a 30 caracteres (límite de {@code numero_ticket} en BD) para el primer {@code save}
     * antes de asignar el código definitivo FL-AAAA-NNNNN.
     */
    private static String generarNumeroTicketTemporal() {
        String hex = UUID.randomUUID().toString().replace("-", "");
        return "TMP-" + hex.substring(0, 26);
    }

    private final ReparacionRepository reparacionRepository;
    private final EquipoRepository equipoRepository;
    private final TallerRepository tallerRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final ReparacionProductoRepository reparacionProductoRepository;
    private final ReparacionEvidenciaRepository reparacionEvidenciaRepository;
    private final ReparacionHistorialEstadoRepository reparacionHistorialEstadoRepository;
    private final TipoEquipoRepository tipoEquipoRepository;
    private final TipoTallerRepository tipoTallerRepository;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public List<TipoEquipoRespDTO> listarTiposEquipo() {
        return tipoEquipoRepository.findAll().stream()
                .sorted(Comparator.comparing(TipoEquipo::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(t -> TipoEquipoRespDTO.builder()
                        .id(t.getId())
                        .nombre(t.getNombre())
                        .fechaCreacion(t.getFechaCreacion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoTallerRespDTO> listarTiposTaller() {
        return tipoTallerRepository.findAll().stream()
                .sorted(Comparator.comparing(TipoTaller::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(t -> TipoTallerRespDTO.builder()
                        .id(t.getId())
                        .nombre(t.getNombre())
                        .ciclo(t.getCiclo())
                        .estado(t.getEstado())
                        .fechaCreacion(t.getFechaCreacion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TallerRespDTO> listarTalleres() {
        return tallerRepository.findAll().stream()
                .sorted(Comparator.comparing(Taller::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::mapearTaller)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReparacionRespDTO crear(ReparacionCreateReqDTO dto, String emailUsuario, RolUsuario rol) {
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Equipo equipo = equipoRepository.findById(dto.getEquipoId())
                .orElseThrow(() -> new NoExisteEquipoException("Equipo no encontrado con id: " + dto.getEquipoId()));
        Taller taller = tallerRepository.findById(dto.getTallerId())
                .orElseThrow(() -> new NoExisteTallerException("Taller no encontrado con id: " + dto.getTallerId()));

        if (rol == RolUsuario.CLIENTE && !equipo.getCliente().getCedula().equals(actor.getCedula())) {
            throw new ResourceNotFoundException("No tiene acceso a este equipo");
        }
        if (rol != RolUsuario.CLIENTE && rol != RolUsuario.ADMIN && rol != RolUsuario.TECNICO
                && rol != RolUsuario.RECEPCIONISTA) {
            throw new BadRequestException("Rol no autorizado para crear tickets");
        }

        Reparacion rep = Reparacion.builder()
                .numeroTicket(generarNumeroTicketTemporal())
                .equipo(equipo)
                .cliente(equipo.getCliente())
                .taller(taller)
                .descripcionFalla(dto.getDescripcionFalla().trim())
                .estado(EstadoReparacion.RECIBIDO)
                .build();
        rep = reparacionRepository.save(rep);
        rep.setNumeroTicket("FL-" + Year.now().getValue() + "-" + String.format("%05d", rep.getId()));
        rep = reparacionRepository.save(rep);

        registrarHistorial(rep, null, EstadoReparacion.RECIBIDO, actor, "Ticket creado");
        notificarClienteCambioEstado(rep, null, EstadoReparacion.RECIBIDO,
                "Tu orden quedó registrada. Te iremos informando por correo cuando avance el servicio.");
        return mapearCompleto(rep, rol);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReparacionRespDTO> listar(String emailUsuario, RolUsuario rol) {
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Reparacion> lista;
        if (rol == RolUsuario.CLIENTE) {
            lista = reparacionRepository.findByCliente_CedulaOrderByFechaCreacionDesc(actor.getCedula());
        } else if (rol == RolUsuario.TECNICO || rol == RolUsuario.RECEPCIONISTA) {
            lista = reparacionRepository.findAllByOrderByFechaCreacionDesc();
        } else if (rol == RolUsuario.ADMIN) {
            lista = reparacionRepository.findAllByOrderByFechaCreacionDesc();
        } else {
            throw new BadRequestException("Rol no autorizado");
        }

        return lista.stream().map(r -> mapearBasico(r, rol)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReparacionRespDTO obtenerPorId(Integer id, String emailUsuario, RolUsuario rol) {
        Reparacion r = reparacionRepository.findById(id)
                .orElseThrow(() -> new NoExisteReparacionException("Reparación no encontrada con id: " + id));
        asegurarLectura(r, emailUsuario, rol);
        return mapearCompleto(r, rol);
    }

    @Override
    @Transactional(readOnly = true)
    public ReparacionRespDTO obtenerPorNumeroTicket(String numeroTicket, String emailUsuario, RolUsuario rol) {
        Reparacion r = reparacionRepository.findByNumeroTicket(numeroTicket.trim())
                .orElseThrow(() -> new NoExisteReparacionException("Ticket no encontrado: " + numeroTicket));
        asegurarLectura(r, emailUsuario, rol);
        return mapearCompleto(r, rol);
    }

    @Override
    @Transactional
    public ReparacionRespDTO asignarTecnico(Integer id, ReparacionAsignarTecnicoReqDTO dto,
                                            String emailUsuario, RolUsuario rol) {
        asegurarPuedeAsignarTecnico(rol);
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Reparacion r = cargar(id);

        Usuario tecnico = usuarioRepository.findById(dto.getTecnicoCedula().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado con esa cédula"));
        if (tecnico.getRol() != RolUsuario.TECNICO && tecnico.getRol() != RolUsuario.ADMIN) {
            throw new BadRequestException("El usuario indicado no es técnico ni administrador");
        }

        EstadoReparacion estadoAnterior = r.getEstado();
        r.setTecnicoAsignado(tecnico);
        if (estadoAnterior == EstadoReparacion.RECIBIDO) {
            validarTransicion(estadoAnterior, EstadoReparacion.EN_DIAGNOSTICO);
            r.setEstado(EstadoReparacion.EN_DIAGNOSTICO);
        }
        r = reparacionRepository.save(r);
        if (estadoAnterior == EstadoReparacion.RECIBIDO && r.getEstado() == EstadoReparacion.EN_DIAGNOSTICO) {
            registrarHistorial(r, estadoAnterior, EstadoReparacion.EN_DIAGNOSTICO, actor,
                    "Técnico asignado: " + tecnico.getCedula() + " · ingreso a diagnóstico");
        } else {
            registrarHistorial(r, null, r.getEstado(), actor, "Técnico asignado: " + tecnico.getCedula());
        }
        if (estadoAnterior != r.getEstado()) {
            notificarClienteCambioEstado(r, estadoAnterior, r.getEstado(),
                    "Se asignó un técnico a tu orden.");
        }
        return mapearCompleto(r, rol);
    }

    @Override
    @Transactional
    public ReparacionRespDTO registrarDiagnosticoCotizacion(Integer id, ReparacionDiagnosticoCotizacionReqDTO dto,
                                                            String emailUsuario, RolUsuario rol) {
        asegurarStaff(rol);
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Reparacion r = cargar(id);

        if (r.getEstado() != EstadoReparacion.EN_DIAGNOSTICO) {
            throw new BadRequestException("El diagnóstico solo se registra en estado EN_DIAGNOSTICO");
        }

        validarTransicion(r.getEstado(), EstadoReparacion.COTIZADO_PENDIENTE_APROBACION);
        EstadoReparacion anterior = r.getEstado();
        r.setDiagnostico(dto.getDiagnostico());
        r.setCotizacionTotal(dto.getCotizacionTotal());
        r.setFechaDiagnostico(LocalDateTime.now());
        r.setEstado(EstadoReparacion.COTIZADO_PENDIENTE_APROBACION);
        r = reparacionRepository.save(r);
        registrarHistorial(r, anterior, EstadoReparacion.COTIZADO_PENDIENTE_APROBACION, actor, "Diagnóstico y cotización");
        notificarClienteCambioEstado(r, anterior, EstadoReparacion.COTIZADO_PENDIENTE_APROBACION,
                "Ya hay diagnóstico y cotización. Entra a FixLab para revisar y aprobar cuando estés listo.");
        return mapearCompleto(r, rol);
    }

    @Override
    @Transactional
    public ReparacionRespDTO cambiarEstado(Integer id, ReparacionCambiarEstadoReqDTO dto,
                                           String emailUsuario, RolUsuario rol) {
        asegurarStaff(rol);
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Reparacion r = cargar(id);

        EstadoReparacion nuevo;
        try {
            nuevo = EstadoReparacion.valueOf(dto.getEstadoNuevo().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + dto.getEstadoNuevo());
        }

        if (nuevo == EstadoReparacion.APROBADO) {
            throw new BadRequestException("La aprobación del cliente se realiza con el endpoint de aprobación de cotización");
        }

        validarTransicion(r.getEstado(), nuevo);
        EstadoReparacion anterior = r.getEstado();
        r.setEstado(nuevo);
        r = reparacionRepository.save(r);
        registrarHistorial(r, anterior, nuevo, actor, dto.getComentario());
        notificarClienteCambioEstado(r, anterior, nuevo, dto.getComentario());
        return mapearCompleto(r, rol);
    }

    @Override
    @Transactional
    public ReparacionRespDTO agregarProducto(Integer id, ReparacionProductoReqDTO dto,
                                             String emailUsuario, RolUsuario rol) {
        asegurarStaff(rol);
        Reparacion r = cargar(id);
        if (r.getEstado() == EstadoReparacion.ENTREGADO || r.getEstado() == EstadoReparacion.CANCELADO) {
            throw new BadRequestException("No se pueden agregar repuestos a una reparación cerrada");
        }

        Producto p = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (!Boolean.TRUE.equals(p.getActivo())) {
            throw new BadRequestException("El producto no está activo o no se puede usar como repuesto");
        }
        int cant = dto.getCantidad() == null ? 0 : dto.getCantidad();
        int stock = p.getStock() == null ? 0 : p.getStock();
        if (stock < cant) {
            if (stock <= 0) {
                throw new BadRequestException("Sin stock disponible para: " + p.getNombre());
            }
            throw new BadRequestException("Stock insuficiente para: " + p.getNombre() + " (disponible: " + stock + ")");
        }

        p.setStock(stock - cant);
        productoRepository.save(p);

        ReparacionProducto linea = ReparacionProducto.builder()
                .reparacion(r)
                .producto(p)
                .cantidad(cant)
                .precioUnitarioSnapshot(p.getPrecio())
                .build();
        reparacionProductoRepository.save(linea);
        return mapearCompleto(reparacionRepository.findById(id).orElseThrow(), rol);
    }

    @Override
    @Transactional
    public ReparacionRespDTO agregarEvidencia(Integer id, ReparacionEvidenciaReqDTO dto,
                                              String emailUsuario, RolUsuario rol) {
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Reparacion r = cargar(id);

        if (rol == RolUsuario.CLIENTE) {
            asegurarClienteDueño(r, actor);
        } else if (rol != RolUsuario.ADMIN && rol != RolUsuario.TECNICO) {
            throw new BadRequestException("Rol no autorizado");
        }

        TipoEvidenciaReparacion tipo;
        try {
            tipo = TipoEvidenciaReparacion.valueOf(dto.getTipo().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de evidencia inválido: " + dto.getTipo());
        }

        ReparacionEvidencia ev = ReparacionEvidencia.builder()
                .reparacion(r)
                .url(dto.getUrl().trim())
                .tipo(tipo)
                .orden(dto.getOrden())
                .build();
        reparacionEvidenciaRepository.save(ev);
        return mapearCompleto(reparacionRepository.findById(id).orElseThrow(), rol);
    }

    @Override
    @Transactional
    public ReparacionRespDTO aprobarCotizacion(Integer id, String emailUsuario, RolUsuario rol) {
        if (rol != RolUsuario.CLIENTE) {
            throw new BadRequestException("Solo el cliente puede aprobar la cotización");
        }
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Reparacion r = cargar(id);
        asegurarClienteDueño(r, actor);

        if (r.getEstado() != EstadoReparacion.COTIZADO_PENDIENTE_APROBACION) {
            throw new BadRequestException("La reparación no está pendiente de aprobación del cliente");
        }

        validarTransicion(r.getEstado(), EstadoReparacion.APROBADO);
        EstadoReparacion anterior = r.getEstado();
        r.setAprobadoCliente(true);
        r.setFechaAprobacionCliente(LocalDateTime.now());
        r.setEstado(EstadoReparacion.APROBADO);
        r = reparacionRepository.save(r);
        registrarHistorial(r, anterior, EstadoReparacion.APROBADO, actor, "Cotización aprobada por el cliente");
        return mapearCompleto(r, rol);
    }

    private Reparacion cargar(Integer id) {
        return reparacionRepository.findById(id)
                .orElseThrow(() -> new NoExisteReparacionException("Reparación no encontrada con id: " + id));
    }

    private void asegurarStaff(RolUsuario rol) {
        if (rol != RolUsuario.ADMIN && rol != RolUsuario.TECNICO) {
            throw new BadRequestException("Solo personal del taller puede realizar esta acción");
        }
    }

    /** Incluye recepcionista para asignación en mostrador. */
    private void asegurarPuedeAsignarTecnico(RolUsuario rol) {
        if (rol != RolUsuario.ADMIN && rol != RolUsuario.TECNICO && rol != RolUsuario.RECEPCIONISTA) {
            throw new BadRequestException("No está autorizado para asignar técnico");
        }
    }

    private void asegurarClienteDueño(Reparacion r, Usuario u) {
        if (!r.getCliente().getCedula().equals(u.getCedula())) {
            throw new ResourceNotFoundException("No tiene acceso a esta reparación");
        }
    }

    private void asegurarLectura(Reparacion r, String emailUsuario, RolUsuario rol) {
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (rol == RolUsuario.CLIENTE) {
            asegurarClienteDueño(r, actor);
        } else if (rol != RolUsuario.ADMIN && rol != RolUsuario.TECNICO && rol != RolUsuario.RECEPCIONISTA) {
            throw new BadRequestException("Rol no autorizado");
        }
    }

    private void validarTransicion(EstadoReparacion desde, EstadoReparacion hasta) {
        if (desde == EstadoReparacion.ENTREGADO || desde == EstadoReparacion.CANCELADO) {
            throw new BadRequestException("La reparación ya está cerrada");
        }
        Set<EstadoReparacion> permitidos = TRANSICIONES_PERMITIDAS.get(desde);
        if (permitidos == null || !permitidos.contains(hasta)) {
            throw new BadRequestException("Transición no permitida: " + desde + " → " + hasta);
        }
    }

    private void registrarHistorial(Reparacion rep, EstadoReparacion anterior, EstadoReparacion nuevo,
                                    Usuario actor, String comentario) {
        ReparacionHistorialEstado h = ReparacionHistorialEstado.builder()
                .reparacion(rep)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .usuario(actor)
                .comentario(comentario != null && !comentario.isBlank() ? comentario.trim() : null)
                .build();
        reparacionHistorialEstadoRepository.save(h);
    }

    /** Envío best-effort: no debe fallar la operación de taller si el correo falla. */
    private void notificarClienteCambioEstado(Reparacion r, EstadoReparacion anterior, EstadoReparacion nuevo,
                                              String lineaExtra) {
        try {
            Usuario c = r.getCliente();
            if (c == null || c.getEmail() == null || c.getEmail().isBlank()) {
                return;
            }
            String nombre = ((c.getNombre() != null ? c.getNombre() : "") + " "
                    + (c.getApellido() != null ? c.getApellido() : "")).trim();
            if (nombre.isEmpty()) {
                nombre = "cliente";
            }
            String antLeg = anterior == null ? "Registro inicial" : etiquetaEstadoParaCliente(anterior);
            String nuevoLeg = etiquetaEstadoParaCliente(nuevo);

            // En cotización, enviar detalle (repuestos + mano de obra) en HTML.
            if (nuevo == EstadoReparacion.COTIZADO_PENDIENTE_APROBACION) {
                String extraHtml = construirBloqueHtmlCotizacion(r);
                if (lineaExtra != null && !lineaExtra.isBlank()) {
                    extraHtml = "<p style=\"color:#555;font-size:14px;margin:12px 0;\">" + esc(lineaExtra) + "</p>" + extraHtml;
                }
                emailService.enviarNotificacionCambioEstadoReparacionConHtmlExtra(
                        c.getEmail().trim(),
                        nombre,
                        r.getNumeroTicket(),
                        antLeg,
                        nuevoLeg,
                        extraHtml
                );
            } else {
                emailService.enviarNotificacionCambioEstadoReparacion(
                        c.getEmail().trim(),
                        nombre,
                        r.getNumeroTicket(),
                        antLeg,
                        nuevoLeg,
                        lineaExtra);
            }
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de cambio de estado al cliente (ticket {}): {}",
                    r.getNumeroTicket(), e.getMessage());
        }
    }

    private String construirBloqueHtmlCotizacion(Reparacion r) {
        var lineas = reparacionProductoRepository.findByReparacion_IdOrderByIdAsc(r.getId());
        double totalRepuestos = 0.0;
        StringBuilder rows = new StringBuilder();
        for (ReparacionProducto l : lineas) {
            String nombre = (l.getProducto() != null && l.getProducto().getNombre() != null)
                    ? l.getProducto().getNombre()
                    : "Producto";
            int cant = l.getCantidad() != null ? l.getCantidad() : 0;
            double unit = l.getPrecioUnitarioSnapshot() != null ? l.getPrecioUnitarioSnapshot() : 0.0;
            double sub = cant * unit;
            totalRepuestos += sub;
            rows.append("<tr>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;\">").append(esc(nombre)).append("</td>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;text-align:right;\">").append(cant).append("</td>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;text-align:right;\">").append(esc(fmtMoney(unit))).append("</td>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;text-align:right;\">").append(esc(fmtMoney(sub))).append("</td>")
                    .append("</tr>");
        }
        if (rows.isEmpty()) {
            rows.append("<tr><td colspan=\"4\" style=\"padding:10px 8px;color:#6b7280;\">Sin repuestos registrados.</td></tr>");
        }

        double total = r.getCotizacionTotal() != null ? r.getCotizacionTotal() : 0.0;
        double manoObra = Math.max(0.0, total - totalRepuestos);

        String diagnostico = (r.getDiagnostico() != null && !r.getDiagnostico().isBlank())
                ? r.getDiagnostico().trim()
                : null;

        String diagBlock = diagnostico != null
                ? "<div style=\"margin:12px 0 14px;\">"
                + "<div style=\"font-size:13px;color:#6b7280;margin-bottom:6px;\">Diagnóstico</div>"
                + "<div style=\"font-size:14px;color:#111827;white-space:pre-wrap;\">" + esc(diagnostico) + "</div>"
                + "</div>"
                : "";

        return "<div style=\"margin:14px 0 0;padding:14px 14px;border:1px solid #e5e7eb;border-radius:10px;background:#fcfcfd;\">"
                + "<div style=\"font-size:15px;font-weight:700;color:#111827;margin:0 0 10px;\">Detalle de cotización</div>"
                + diagBlock
                + "<table style=\"width:100%;border-collapse:collapse;font-size:13px;\">"
                + "<thead><tr>"
                + "<th style=\"text-align:left;padding:10px 8px;border-bottom:1px solid #e5e7eb;background:#f9fafb;color:#374151;\">Repuesto / Producto</th>"
                + "<th style=\"text-align:right;padding:10px 8px;border-bottom:1px solid #e5e7eb;background:#f9fafb;color:#374151;\">Cant.</th>"
                + "<th style=\"text-align:right;padding:10px 8px;border-bottom:1px solid #e5e7eb;background:#f9fafb;color:#374151;\">Unitario</th>"
                + "<th style=\"text-align:right;padding:10px 8px;border-bottom:1px solid #e5e7eb;background:#f9fafb;color:#374151;\">Subtotal</th>"
                + "</tr></thead>"
                + "<tbody>" + rows + "</tbody>"
                + "</table>"
                + "<div style=\"margin-top:12px;display:flex;justify-content:flex-end;\">"
                + "<table style=\"border-collapse:collapse;font-size:13px;min-width:240px;\">"
                + "<tr><td style=\"padding:6px 0;color:#6b7280;\">Repuestos</td><td style=\"padding:6px 0;text-align:right;\">" + esc(fmtMoney(totalRepuestos)) + "</td></tr>"
                + "<tr><td style=\"padding:6px 0;color:#6b7280;\">Mano de obra</td><td style=\"padding:6px 0;text-align:right;\">" + esc(fmtMoney(manoObra)) + "</td></tr>"
                + "<tr><td style=\"padding:8px 0;font-weight:800;color:#111827;border-top:1px solid #e5e7eb;\">Total</td>"
                + "<td style=\"padding:8px 0;text-align:right;font-weight:800;color:#111827;border-top:1px solid #e5e7eb;\">" + esc(fmtMoney(total)) + "</td></tr>"
                + "</table>"
                + "</div>"
                + "</div>";
    }

    private static String fmtMoney(double value) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        return nf.format(value);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String etiquetaEstadoParaCliente(EstadoReparacion e) {
        return switch (e) {
            case RECIBIDO -> "Recibido en taller";
            case EN_DIAGNOSTICO -> "En diagnóstico";
            case COTIZADO_PENDIENTE_APROBACION -> "Cotizado (pendiente de tu aprobación)";
            case APROBADO -> "Cotización aprobada";
            case EN_REPARACION -> "En reparación";
            case EN_PRUEBAS -> "En pruebas";
            case LISTO_ENTREGA -> "Listo para entrega";
            case ENTREGADO -> "Entregado";
            case CANCELADO -> "Cancelado";
        };
    }

    private TallerRespDTO mapearTaller(Taller t) {
        return TallerRespDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .tipoTallerId(t.getTipoTaller().getId())
                .tipoTallerNombre(t.getTipoTaller().getNombre())
                .fechaCreacion(t.getFechaCreacion())
                .fechaActualizacion(t.getFechaActualizacion())
                .build();
    }

    private EquipoRespDTO mapearEquipo(Equipo e) {
        Usuario c = e.getCliente();
        return EquipoRespDTO.builder()
                .id(e.getId())
                .tipoEquipoId(e.getTipoEquipo().getId())
                .tipoEquipoNombre(e.getTipoEquipo().getNombre())
                .clienteCedula(c.getCedula())
                .clienteNombre(c.getNombre())
                .clienteApellido(c.getApellido())
                .marca(e.getMarca())
                .modelo(e.getModelo())
                .numeroSerie(e.getNumeroSerie())
                .observaciones(e.getObservaciones())
                .fechaCreacion(e.getFechaCreacion())
                .fechaActualizacion(e.getFechaActualizacion())
                .build();
    }

    private ReparacionRespDTO mapearBasico(Reparacion r, RolUsuario visor) {
        return mapearInterno(r, visor, false);
    }

    private ReparacionRespDTO mapearCompleto(Reparacion r, RolUsuario visor) {
        return mapearInterno(r, visor, true);
    }

    private ReparacionRespDTO mapearInterno(Reparacion r, RolUsuario visor, boolean detalle) {
        Usuario cli = r.getCliente();
        Usuario tec = r.getTecnicoAsignado();

        ReparacionRespDTO.ReparacionRespDTOBuilder b = ReparacionRespDTO.builder()
                .id(r.getId())
                .numeroTicket(r.getNumeroTicket())
                .estado(r.getEstado().name())
                .equipo(mapearEquipo(r.getEquipo()))
                .tallerId(r.getTaller().getId())
                .tallerNombre(r.getTaller().getNombre())
                .clienteCedula(cli.getCedula())
                .clienteNombre(cli.getNombre())
                .clienteApellido(cli.getApellido())
                .tecnicoCedula(tec != null ? tec.getCedula() : null)
                .tecnicoNombre(tec != null ? tec.getNombre() : null)
                .tecnicoApellido(tec != null ? tec.getApellido() : null)
                .descripcionFalla(r.getDescripcionFalla())
                .diagnostico(r.getDiagnostico())
                .cotizacionTotal(r.getCotizacionTotal())
                .fechaDiagnostico(r.getFechaDiagnostico())
                .aprobadoCliente(r.isAprobadoCliente())
                .fechaAprobacionCliente(r.getFechaAprobacionCliente())
                .mesesGarantiaServicio(r.getMesesGarantiaServicio())
                .fechaFinGarantiaServicio(r.getFechaFinGarantiaServicio())
                .fechaCreacion(r.getFechaCreacion())
                .fechaActualizacion(r.getFechaActualizacion());

        if (visor == RolUsuario.CLIENTE) {
            b.notasInternas(null);
        } else {
            b.notasInternas(r.getNotasInternas());
        }

        if (!detalle) {
            return b.lineasProducto(Collections.emptyList())
                    .evidencias(Collections.emptyList())
                    .historialEstados(Collections.emptyList())
                    .build();
        }

        List<ReparacionProductoLineRespDTO> lineas = reparacionProductoRepository
                .findByReparacion_IdOrderByIdAsc(r.getId()).stream()
                .map(l -> {
                    Producto p = l.getProducto();
                    double sub = l.getCantidad() * l.getPrecioUnitarioSnapshot();
                    return ReparacionProductoLineRespDTO.builder()
                            .id(l.getId())
                            .productoId(p.getId())
                            .nombreProducto(p.getNombre())
                            .sku(p.getSku())
                            .cantidad(l.getCantidad())
                            .precioUnitarioSnapshot(l.getPrecioUnitarioSnapshot())
                            .subtotal(sub)
                            .build();
                })
                .collect(Collectors.toList());

        List<ReparacionEvidenciaRespDTO> evs = reparacionEvidenciaRepository
                .findByReparacion_IdOrderByOrdenAscIdAsc(r.getId()).stream()
                .map(e -> ReparacionEvidenciaRespDTO.builder()
                        .id(e.getId())
                        .url(e.getUrl())
                        .tipo(e.getTipo().name())
                        .orden(e.getOrden())
                        .fechaCreacion(e.getFechaCreacion())
                        .build())
                .collect(Collectors.toList());

        List<ReparacionHistorialEstadoRespDTO> hist = reparacionHistorialEstadoRepository
                .findByReparacion_IdOrderByFechaCambioAsc(r.getId()).stream()
                .map(h -> {
                    Usuario u = h.getUsuario();
                    return ReparacionHistorialEstadoRespDTO.builder()
                            .id(h.getId())
                            .estadoAnterior(h.getEstadoAnterior() != null ? h.getEstadoAnterior().name() : null)
                            .estadoNuevo(h.getEstadoNuevo().name())
                            .usuarioCedula(u != null ? u.getCedula() : null)
                            .usuarioNombre(u != null ? u.getNombre() : null)
                            .usuarioApellido(u != null ? u.getApellido() : null)
                            .comentario(h.getComentario())
                            .fechaCambio(h.getFechaCambio())
                            .build();
                })
                .collect(Collectors.toList());

        return b.lineasProducto(lineas).evidencias(evs).historialEstados(hist).build();
    }
}
