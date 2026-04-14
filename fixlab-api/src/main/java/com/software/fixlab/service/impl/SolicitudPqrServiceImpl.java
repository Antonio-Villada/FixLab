package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.SolicitudPqrCambiarEstadoReqDTO;
import com.software.fixlab.dto.req.SolicitudPqrCreateReqDTO;
import com.software.fixlab.dto.req.SolicitudPqrValidacionGarantiaReqDTO;
import com.software.fixlab.dto.resp.SolicitudPqrRespDTO;
import com.software.fixlab.entity.*;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExisteSolicitudPqrException;
import com.software.fixlab.exception.ResourceNotFoundException;
import com.software.fixlab.repository.PedidoRepository;
import com.software.fixlab.repository.ReparacionRepository;
import com.software.fixlab.repository.SolicitudPqrRepository;
import com.software.fixlab.repository.UsuarioRepository;
import com.software.fixlab.service.interfaces.SolicitudPqrService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudPqrServiceImpl implements SolicitudPqrService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudPqrServiceImpl.class);

    private static final int MAX_EVIDENCIAS = 15;

    private static final Map<EstadoSolicitudPqr, Set<EstadoSolicitudPqr>> TRANSICIONES = Map.of(
            EstadoSolicitudPqr.ABIERTO, EnumSet.of(EstadoSolicitudPqr.EN_ANALISIS, EstadoSolicitudPqr.CERRADO),
            EstadoSolicitudPqr.EN_ANALISIS,
            EnumSet.of(EstadoSolicitudPqr.RESUELTO, EstadoSolicitudPqr.ABIERTO, EstadoSolicitudPqr.CERRADO),
            EstadoSolicitudPqr.RESUELTO, EnumSet.of(EstadoSolicitudPqr.CERRADO),
            EstadoSolicitudPqr.CERRADO, EnumSet.noneOf(EstadoSolicitudPqr.class)
    );

    private static final Set<String> ESTADOS_PEDIDO_ELEGIBLES_GARANTIA = Set.of(
            "PAGADO", "EN_PREPARACION", "DESPACHADO", "ENTREGADO"
    );

    private final SolicitudPqrRepository solicitudPqrRepository;
    private final PedidoRepository pedidoRepository;
    private final ReparacionRepository reparacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @Value("${fixlab.pqrs.garantia-venta-meses:12}")
    private int garantiaVentaMeses;

    private static String radicadoTemporal() {
        String hex = UUID.randomUUID().toString().replace("-", "");
        return "TMP-PQR-" + hex.substring(0, 20);
    }

    @Override
    @Transactional
    public SolicitudPqrRespDTO crear(SolicitudPqrCreateReqDTO dto, String emailUsuario) {
        Usuario cliente = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (cliente.getRol() != RolUsuario.CLIENTE) {
            throw new BadRequestException("Solo los clientes pueden radicar PQRS desde esta vía");
        }

        validarOrigenYReferencias(dto);

        Pedido pedido = null;
        Reparacion reparacion = null;
        if (dto.getOrigenDocumento() == OrigenDocumentoPqr.FACTURA_PEDIDO) {
            pedido = pedidoRepository.findByIdAndCliente_Cedula(dto.getPedidoId(), cliente.getCedula())
                    .orElseThrow(() -> new BadRequestException(
                            "La factura de venta no existe o no está asociada a su cuenta"));
        } else {
            reparacion = reparacionRepository.findByIdAndCliente_Cedula(dto.getReparacionId(), cliente.getCedula())
                    .orElseThrow(() -> new BadRequestException(
                            "El ticket de servicio no existe o no está asociado a su cuenta"));
        }

        boolean garantiaOk = false;
        if (dto.getTipo() == TipoSolicitudPqr.SOLICITUD_GARANTIA) {
            if (pedido != null) {
                validarVigenciaGarantiaPedido(pedido);
            } else {
                validarVigenciaGarantiaReparacion(reparacion);
            }
            garantiaOk = true;
        }

        List<String> urls = normalizarEvidencias(dto.getEvidenciasUrls());

        SolicitudPqr ent = SolicitudPqr.builder()
                .radicado(radicadoTemporal())
                .cliente(cliente)
                .origenDocumento(dto.getOrigenDocumento())
                .pedido(pedido)
                .reparacion(reparacion)
                .tipo(dto.getTipo())
                .descripcion(dto.getDescripcion().trim())
                .evidenciasUrls(new ArrayList<>(urls))
                .estado(EstadoSolicitudPqr.ABIERTO)
                .garantiaVigenteAlRadicar(garantiaOk)
                .build();

        ent = solicitudPqrRepository.save(ent);
        ent.setRadicado("PQR-" + Year.now().getValue() + "-" + String.format("%06d", ent.getId()));
        ent = solicitudPqrRepository.save(ent);

        notificarCorreoSeguro(ent, null, ent.getEstado(), dto.getTipo(),
                "Hemos registrado tu solicitud. Conserva tu número de radicado para cualquier consulta.");

        return mapear(ent, false);
    }

    private void validarOrigenYReferencias(SolicitudPqrCreateReqDTO dto) {
        if (dto.getOrigenDocumento() == OrigenDocumentoPqr.FACTURA_PEDIDO) {
            if (dto.getPedidoId() == null || dto.getReparacionId() != null) {
                throw new BadRequestException("Indique solo el número de pedido (factura) asociado a su compra");
            }
        } else {
            if (dto.getReparacionId() == null || dto.getPedidoId() != null) {
                throw new BadRequestException("Indique solo el identificador del ticket de servicio (reparación)");
            }
        }
    }

    private void validarVigenciaGarantiaPedido(Pedido p) {
        String e = p.getEstado() == null ? "" : p.getEstado().trim().toUpperCase(Locale.ROOT);
        if (!ESTADOS_PEDIDO_ELEGIBLES_GARANTIA.contains(e)) {
            throw new BadRequestException(
                    "La garantía por venta aplica únicamente a pedidos pagados o en entrega. Verifique el estado de su pedido.");
        }
        if (p.getFechaCreacion() == null) {
            throw new BadRequestException("No se puede calcular la garantía: falta la fecha del pedido.");
        }
        LocalDate fin = p.getFechaCreacion().toLocalDate().plusMonths(garantiaVentaMeses);
        if (fin.isBefore(LocalDate.now())) {
            throw new BadRequestException("La garantía por producto ha vencido (política FixLab: "
                    + garantiaVentaMeses + " meses desde la fecha de la compra).");
        }
    }

    private void validarVigenciaGarantiaReparacion(Reparacion r) {
        if (r.getEstado() != EstadoReparacion.ENTREGADO) {
            throw new BadRequestException("La garantía del servicio aplica cuando la orden consta como entregada.");
        }
        if (r.getFechaFinGarantiaServicio() == null) {
            throw new BadRequestException(
                    "No hay vigencia de garantía registrada para este ticket; comuníquese con el taller.");
        }
        if (r.getFechaFinGarantiaServicio().isBefore(LocalDate.now())) {
            throw new BadRequestException("La garantía del servicio técnico ya venció.");
        }
    }

    private List<String> normalizarEvidencias(List<String> evidenciasUrls) {
        if (evidenciasUrls == null || evidenciasUrls.isEmpty()) {
            return List.of();
        }
        List<String> out = evidenciasUrls.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (out.size() > MAX_EVIDENCIAS) {
            throw new BadRequestException("Máximo " + MAX_EVIDENCIAS + " enlaces de evidencia por solicitud");
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudPqrRespDTO> listarMis(String emailUsuario) {
        Usuario u = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (u.getRol() != RolUsuario.CLIENTE) {
            throw new BadRequestException("Use el panel de gestión para ver solicitudes de clientes");
        }
        return solicitudPqrRepository.findByCliente_CedulaOrderByFechaRadicacionDesc(u.getCedula()).stream()
                .map(s -> mapear(s, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudPqrRespDTO> listarGestion(String emailUsuario, RolUsuario rol) {
        asegurarRolGestionLectura(rol);
        usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return solicitudPqrRepository.findAllByOrderByFechaRadicacionDesc().stream()
                .map(s -> mapear(s, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudPqrRespDTO obtenerPorId(Long id, String emailUsuario, RolUsuario rol) {
        SolicitudPqr s = cargarSegunRol(id, emailUsuario, rol);
        return mapear(s, rol != RolUsuario.CLIENTE);
    }

    @Override
    @Transactional
    public SolicitudPqrRespDTO cambiarEstado(Long id, SolicitudPqrCambiarEstadoReqDTO dto,
                                             String emailUsuario, RolUsuario rol) {
        if (rol != RolUsuario.ADMIN) {
            throw new BadRequestException("Solo un administrador puede cambiar el estado de la PQRS");
        }
        usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        SolicitudPqr s = solicitudPqrRepository.findDetailedById(id)
                .orElseThrow(() -> new NoExisteSolicitudPqrException("Solicitud no encontrada"));

        EstadoSolicitudPqr anterior = s.getEstado();
        EstadoSolicitudPqr nuevo = dto.getNuevoEstado();
        Set<EstadoSolicitudPqr> permitidos = TRANSICIONES.getOrDefault(anterior, EnumSet.noneOf(EstadoSolicitudPqr.class));
        if (!permitidos.contains(nuevo)) {
            throw new BadRequestException("Transición de estado no permitida: " + anterior + " → " + nuevo);
        }

        s.setEstado(nuevo);
        appendNotasInternas(s, dto.getNotasInternas());
        solicitudPqrRepository.save(s);

        notificarCorreoSeguro(s, anterior, nuevo, s.getTipo(), dto.getMensajeParaCliente());
        return mapear(s, true);
    }

    @Override
    @Transactional
    public SolicitudPqrRespDTO registrarValidacionGarantiaFisica(
            Long id, SolicitudPqrValidacionGarantiaReqDTO dto, String emailUsuario, RolUsuario rol) {
        if (rol != RolUsuario.ADMIN && rol != RolUsuario.TECNICO) {
            throw new BadRequestException("Solo técnico o administrador puede registrar la validación física de garantía");
        }
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        SolicitudPqr s = solicitudPqrRepository.findDetailedById(id)
                .orElseThrow(() -> new NoExisteSolicitudPqrException("Solicitud no encontrada"));

        if (s.getTipo() != TipoSolicitudPqr.SOLICITUD_GARANTIA) {
            throw new BadRequestException("La validación física de garantía aplica a solicitudes de tipo garantía");
        }

        s.setGarantiaFisicaValidada(Boolean.TRUE.equals(dto.getGarantiaFisicaValidada()));
        s.setFechaValidacionGarantiaFisica(LocalDateTime.now());
        s.setTecnicoValidacion(actor);
        appendNotasInternas(s, dto.getNotas());
        solicitudPqrRepository.save(s);

        return mapear(s, true);
    }

    private void appendNotasInternas(SolicitudPqr s, String nota) {
        if (nota == null || nota.isBlank()) {
            return;
        }
        String linea = LocalDateTime.now() + " — " + nota.trim();
        s.setNotasInternas(s.getNotasInternas() == null || s.getNotasInternas().isBlank()
                ? linea
                : s.getNotasInternas() + "\n" + linea);
    }

    private SolicitudPqr cargarSegunRol(Long id, String emailUsuario, RolUsuario rol) {
        Usuario u = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (rol == RolUsuario.CLIENTE) {
            return solicitudPqrRepository.findDetailedByIdAndCliente_Cedula(id, u.getCedula())
                    .orElseThrow(() -> new NoExisteSolicitudPqrException("Solicitud no encontrada"));
        }
        asegurarRolGestionLectura(rol);
        return solicitudPqrRepository.findDetailedById(id)
                .orElseThrow(() -> new NoExisteSolicitudPqrException("Solicitud no encontrada"));
    }

    private static void asegurarRolGestionLectura(RolUsuario rol) {
        if (rol != RolUsuario.ADMIN && rol != RolUsuario.TECNICO && rol != RolUsuario.RECEPCIONISTA) {
            throw new BadRequestException("No autorizado para gestión de postventa");
        }
    }

    private SolicitudPqrRespDTO mapear(SolicitudPqr s, boolean incluirNotasInternas) {
        String ticket = null;
        if (s.getReparacion() != null) {
            ticket = s.getReparacion().getNumeroTicket();
        }
        String tecNombre = null;
        if (s.getTecnicoValidacion() != null) {
            tecNombre = (s.getTecnicoValidacion().getNombre() + " " + s.getTecnicoValidacion().getApellido()).trim();
        }
        return SolicitudPqrRespDTO.builder()
                .id(s.getId())
                .radicado(s.getRadicado())
                .tipo(s.getTipo())
                .estado(s.getEstado())
                .origenDocumento(s.getOrigenDocumento())
                .pedidoId(s.getPedido() != null ? s.getPedido().getId() : null)
                .reparacionId(s.getReparacion() != null ? s.getReparacion().getId() : null)
                .reparacionNumeroTicket(ticket)
                .descripcion(s.getDescripcion())
                .evidenciasUrls(s.getEvidenciasUrls() == null ? List.of() : new ArrayList<>(s.getEvidenciasUrls()))
                .fechaRadicacion(s.getFechaRadicacion())
                .fechaActualizacion(s.getFechaActualizacion())
                .notasInternas(incluirNotasInternas ? s.getNotasInternas() : null)
                .garantiaFisicaValidada(s.isGarantiaFisicaValidada())
                .fechaValidacionGarantiaFisica(s.getFechaValidacionGarantiaFisica())
                .tecnicoValidacionCedula(s.getTecnicoValidacion() != null ? s.getTecnicoValidacion().getCedula() : null)
                .tecnicoValidacionNombre(tecNombre)
                .garantiaVigenteAlRadicar(s.isGarantiaVigenteAlRadicar())
                .build();
    }

    private void notificarCorreoSeguro(
            SolicitudPqr s,
            EstadoSolicitudPqr estadoAnterior,
            EstadoSolicitudPqr estadoNuevo,
            TipoSolicitudPqr tipo,
            String mensajeExtra) {
        try {
            Usuario c = s.getCliente();
            if (c == null || c.getEmail() == null || c.getEmail().isBlank()) {
                return;
            }
            String nombre = (c.getNombre() + " " + c.getApellido()).trim();
            emailService.enviarNotificacionCambioEstadoPqr(
                    c.getEmail(),
                    nombre,
                    s.getRadicado(),
                    estadoLegible(estadoAnterior),
                    estadoLegible(estadoNuevo),
                    tipoLegible(tipo),
                    mensajeExtra);
        } catch (Exception e) {
            log.warn("No se pudo enviar correo de PQRS {}: {}", s.getRadicado(), e.getMessage());
        }
    }

    private static String estadoLegible(EstadoSolicitudPqr e) {
        if (e == null) {
            return "Radicación";
        }
        return switch (e) {
            case ABIERTO -> "Abierto";
            case EN_ANALISIS -> "En análisis";
            case RESUELTO -> "Resuelto";
            case CERRADO -> "Cerrado";
        };
    }

    private static String tipoLegible(TipoSolicitudPqr t) {
        if (t == null) {
            return "PQRS";
        }
        return switch (t) {
            case PETICION -> "Petición";
            case QUEJA -> "Queja";
            case RECLAMO -> "Reclamo";
            case SOLICITUD_GARANTIA -> "Solicitud de garantía";
        };
    }
}
