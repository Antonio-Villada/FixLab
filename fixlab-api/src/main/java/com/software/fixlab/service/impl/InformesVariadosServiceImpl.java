package com.software.fixlab.service.impl;

import com.software.fixlab.dto.resp.informes.*;
import com.software.fixlab.entity.*;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.repository.*;
import com.software.fixlab.service.interfaces.InformesVariadosService;
import com.software.fixlab.service.interfaces.ReporteExistenciasService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InformesVariadosServiceImpl implements InformesVariadosService {

    private static final int RANGO_MAXIMO_DIAS = 366;
    private static final int TOP_CLIENTES = 20;

    private final EntradaMercanciaRepository entradaMercanciaRepository;
    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ReparacionRepository reparacionRepository;
    private final ReparacionProductoRepository reparacionProductoRepository;
    private final SolicitudPqrRepository solicitudPqrRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReporteExistenciasService reporteExistenciasService;

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new BadRequestException("Las fechas desde y hasta son obligatorias.");
        }
        if (desde.isAfter(hasta)) {
            throw new BadRequestException("La fecha 'desde' no puede ser posterior a 'hasta'.");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > RANGO_MAXIMO_DIAS) {
            throw new BadRequestException("El rango máximo permitido es de " + RANGO_MAXIMO_DIAS + " días.");
        }
    }

    private InformeMetadatosDTO meta(LocalDate desde, LocalDate hasta) {
        return InformeMetadatosDTO.builder()
                .desde(desde)
                .hasta(hasta)
                .generadoEn(Instant.now())
                .build();
    }

    private LocalDateTime inicioDia(LocalDate d) {
        return d.atStartOfDay();
    }

    private LocalDateTime inicioDiaSiguiente(LocalDate d) {
        return d.plusDays(1).atStartOfDay();
    }

    private Instant instantInicio(LocalDate d) {
        return d.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant instantFinExclusivo(LocalDate d) {
        return d.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientosStockReporteDTO movimientosStock(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        Instant d1 = instantInicio(desde);
        Instant d2 = instantFinExclusivo(hasta);
        List<MovimientoStockInformeLineaDTO> lineas = entradaMercanciaRepository
                .findByFechaRegistroGreaterThanEqualAndFechaRegistroBeforeOrderByFechaRegistroDesc(d1, d2)
                .stream()
                .map(e -> {
                    Producto p = e.getProducto();
                    return MovimientoStockInformeLineaDTO.builder()
                            .id(e.getId())
                            .fechaRegistro(e.getFechaRegistro())
                            .productoId(p.getId())
                            .sku(p.getSku())
                            .nombreProducto(p.getNombre())
                            .cantidad(e.getCantidad())
                            .comentario(e.getComentario())
                            .build();
                })
                .collect(Collectors.toList());
        return MovimientosStockReporteDTO.builder()
                .meta(meta(desde, hasta))
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductosSinVentasReporteDTO productosSinVentas(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        List<Producto> productos = productoRepository.findActivosSinVentaPagadaEnPeriodo(d1, d2);
        List<ProductoSinVentasLineaDTO> lineas = productos.stream()
                .map(p -> ProductoSinVentasLineaDTO.builder()
                        .productoId(p.getId())
                        .sku(p.getSku())
                        .nombre(p.getNombre())
                        .categoriaNombre(p.getCategoria().getNombre())
                        .tipoProductoNombre(p.getTipoProducto().getNombre())
                        .stock(p.getStock())
                        .activo(p.getActivo())
                        .build())
                .sorted(Comparator.comparing(ProductoSinVentasLineaDTO::getCategoriaNombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ProductoSinVentasLineaDTO::getSku, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return ProductosSinVentasReporteDTO.builder()
                .meta(meta(desde, hasta))
                .totalProductosActivosSinVentas(lineas.size())
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RotacionProductosReporteDTO rotacionProductos(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        List<RotacionProductoLineaDTO> lineas = new ArrayList<>();
        for (Object[] row : detallePedidoRepository.rotacionPorProducto(d1, d2)) {
            Long id = ((Number) row[0]).longValue();
            String sku = (String) row[1];
            String nombre = (String) row[2];
            String cat = (String) row[3];
            long vendidas = ((Number) row[4]).longValue();
            int stock = ((Number) row[5]).intValue();
            double indice = vendidas / (double) Math.max(stock, 1);
            lineas.add(RotacionProductoLineaDTO.builder()
                    .productoId(id)
                    .sku(sku)
                    .nombre(nombre)
                    .categoriaNombre(cat)
                    .unidadesVendidasPeriodo((int) Math.min(vendidas, Integer.MAX_VALUE))
                    .stockActual(stock)
                    .indiceRotacion(indice)
                    .build());
        }
        return RotacionProductosReporteDTO.builder()
                .meta(meta(desde, hasta))
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VentasResumenDTO ventasResumen(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        long total = pedidoRepository.countByFechaCreacionGreaterThanEqualAndFechaCreacionBefore(d1, d2);
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (Object[] row : pedidoRepository.contarPedidosPorEstadoEnPeriodo(d1, d2)) {
            porEstado.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        long pagados = porEstado.getOrDefault("PAGADO", 0L);
        long entregados = porEstado.getOrDefault("ENTREGADO", 0L);
        long cancelados = porEstado.getOrDefault("CANCELADO", 0L);
        long otros = total - pagados - entregados - cancelados;
        Double sumPagados = pedidoRepository.sumarTotalPedidosPorEstadoEnPeriodo("PAGADO", d1, d2);
        Double sumEntregados = pedidoRepository.sumarTotalPedidosPorEstadoEnPeriodo("ENTREGADO", d1, d2);
        double montoPagados = sumPagados != null ? sumPagados : 0.0;
        double montoEntregados = sumEntregados != null ? sumEntregados : 0.0;
        Double sumTodos = pedidoRepository.sumarTotalTodosPedidosEnPeriodo(d1, d2);
        double totalMontoTodos = sumTodos != null ? sumTodos : 0.0;
        Double ticketProm = pagados > 0 ? montoPagados / pagados : null;
        return VentasResumenDTO.builder()
                .meta(meta(desde, hasta))
                .totalPedidosPeriodo(total)
                .pedidosPagados(pagados)
                .pedidosEntregados(entregados)
                .pedidosCancelados(cancelados)
                .pedidosOtrosEstados(Math.max(0, otros))
                .totalMontoPedidosPagados(montoPagados)
                .totalMontoPedidosEntregados(montoEntregados)
                .totalMontoTodosEstados(totalMontoTodos)
                .ticketPromedioPagados(ticketProm)
                .pedidosPorEstado(porEstado)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VentasPorCategoriaReporteDTO ventasPorCategoria(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        List<Object[]> rows = detallePedidoRepository.sumarVentasPorCategoria(d1, d2);
        double montoTotal = rows.stream()
                .mapToDouble(r -> ((Number) r[3]).doubleValue())
                .sum();
        List<VentasPorCategoriaLineaDTO> lineas = new ArrayList<>();
        for (Object[] r : rows) {
            double monto = ((Number) r[3]).doubleValue();
            double part = montoTotal > 0 ? (monto / montoTotal) * 100.0 : 0.0;
            lineas.add(VentasPorCategoriaLineaDTO.builder()
                    .categoriaId(((Number) r[0]).intValue())
                    .categoriaNombre((String) r[1])
                    .unidadesVendidas(((Number) r[2]).longValue())
                    .montoTotal(monto)
                    .participacionPorcentaje(part)
                    .build());
        }
        return VentasPorCategoriaReporteDTO.builder()
                .meta(meta(desde, hasta))
                .montoTotalPagado(montoTotal)
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TopProductosReporteDTO topProductosVendidos(LocalDate desde, LocalDate hasta, int limite) {
        validarRango(desde, hasta);
        if (limite < 1 || limite > 100) {
            throw new BadRequestException("El límite debe estar entre 1 y 100.");
        }
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        List<Object[]> rows = detallePedidoRepository.topProductosVenta(d1, d2, PageRequest.of(0, limite));
        List<TopProductoVentaLineaDTO> lineas = new ArrayList<>();
        for (Object[] r : rows) {
            lineas.add(TopProductoVentaLineaDTO.builder()
                    .productoId(((Number) r[0]).longValue())
                    .sku((String) r[1])
                    .nombre((String) r[2])
                    .categoriaNombre((String) r[3])
                    .tipoProductoNombre((String) r[4])
                    .unidadesVendidas(((Number) r[5]).longValue())
                    .montoTotal(((Number) r[6]).doubleValue())
                    .build());
        }
        return TopProductosReporteDTO.builder()
                .meta(meta(desde, hasta))
                .limite(limite)
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidosLogisticaReporteDTO pedidosPendientesLogistica() {
        List<String> estados = List.of("PAGADO", "ENVIADO");
        List<Pedido> pedidos = pedidoRepository.findTop500ByEstadoInOrderByFechaCreacionDesc(estados);
        List<PedidoLogisticaLineaDTO> lineas = pedidos.stream()
                .map(p -> PedidoLogisticaLineaDTO.builder()
                        .pedidoId(p.getId())
                        .fechaCreacion(p.getFechaCreacion())
                        .estado(p.getEstado())
                        .total(p.getTotal())
                        .clienteCedula(p.getCliente().getCedula())
                        .clienteNombre(p.getCliente().getNombre() + " " + p.getCliente().getApellido())
                        .direccionEnvio(p.getDireccionEnvio())
                        .build())
                .collect(Collectors.toList());
        return PedidosLogisticaReporteDTO.builder()
                .generadoEn(Instant.now())
                .totalCoincidencias(lineas.size())
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReparacionesPorEstadoReporteDTO reparacionesPorEstado(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        long total = reparacionRepository.countByFechaCreacionGreaterThanEqualAndFechaCreacionBefore(d1, d2);
        List<ReparacionPorEstadoLineaDTO> lineas = reparacionRepository.contarPorEstadoEnPeriodo(d1, d2).stream()
                .map(row -> ReparacionPorEstadoLineaDTO.builder()
                        .estado(String.valueOf(row[0]))
                        .cantidad(((Number) row[1]).longValue())
                        .build())
                .sorted(Comparator.comparing(ReparacionPorEstadoLineaDTO::getEstado))
                .collect(Collectors.toList());
        return ReparacionesPorEstadoReporteDTO.builder()
                .meta(meta(desde, hasta))
                .totalReparacionesPeriodo(total)
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReparacionesPorTecnicoReporteDTO reparacionesPorTecnico(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        List<ReparacionPorTecnicoLineaDTO> lineas = reparacionRepository
                .resumenPorTecnico(d1, d2, EstadoReparacion.ENTREGADO, EstadoReparacion.CANCELADO)
                .stream()
                .map(row -> ReparacionPorTecnicoLineaDTO.builder()
                        .tecnicoCedula((String) row[0])
                        .tecnicoNombre((String) row[1])
                        .reparacionesEntregadas(((Number) row[2]).longValue())
                        .reparacionesActivasOtras(((Number) row[3]).longValue())
                        .build())
                .collect(Collectors.toList());
        return ReparacionesPorTecnicoReporteDTO.builder()
                .meta(meta(desde, hasta))
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RepuestosTallerReporteDTO repuestosTaller(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        List<RepuestoTallerLineaDTO> lineas = new ArrayList<>();
        for (Object[] row : reparacionProductoRepository.sumarRepuestosPorProductoEnPeriodo(d1, d2)) {
            lineas.add(RepuestoTallerLineaDTO.builder()
                    .productoId(((Number) row[0]).longValue())
                    .sku((String) row[1])
                    .nombreProducto((String) row[2])
                    .unidadesUsadas(((Number) row[3]).longValue())
                    .valorTotal(((Number) row[4]).doubleValue())
                    .build());
        }
        return RepuestosTallerReporteDTO.builder()
                .meta(meta(desde, hasta))
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PqrsResumenReporteDTO pqrsResumen(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        List<SolicitudPqr> list = solicitudPqrRepository
                .findByFechaRadicacionGreaterThanEqualAndFechaRadicacionBeforeOrderByFechaRadicacionDesc(d1, d2);
        Map<String, Long> porTipo = list.stream()
                .collect(Collectors.groupingBy(s -> s.getTipo().name(), Collectors.counting()));
        Map<String, Long> porEstado = list.stream()
                .collect(Collectors.groupingBy(s -> s.getEstado().name(), Collectors.counting()));
        List<Double> diasCierre = new ArrayList<>();
        for (SolicitudPqr s : list) {
            if (s.getEstado() == EstadoSolicitudPqr.RESUELTO || s.getEstado() == EstadoSolicitudPqr.CERRADO) {
                if (s.getFechaRadicacion() != null && s.getFechaActualizacion() != null) {
                    long d = ChronoUnit.DAYS.between(s.getFechaRadicacion(), s.getFechaActualizacion());
                    diasCierre.add((double) Math.max(0, d));
                }
            }
        }
        Double prom = diasCierre.isEmpty()
                ? null
                : diasCierre.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return PqrsResumenReporteDTO.builder()
                .meta(meta(desde, hasta))
                .totalSolicitudes(list.size())
                .diasPromedioHastaCierre(prom)
                .porTipo(porTipo.entrySet().stream()
                        .map(e -> PqrTipoConteoDTO.builder().tipo(e.getKey()).cantidad(e.getValue()).build())
                        .sorted(Comparator.comparing(PqrTipoConteoDTO::getTipo))
                        .collect(Collectors.toList()))
                .porEstado(porEstado.entrySet().stream()
                        .map(e -> PqrEstadoConteoDTO.builder().estado(e.getKey()).cantidad(e.getValue()).build())
                        .sorted(Comparator.comparing(PqrEstadoConteoDTO::getEstado))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GarantiasServicioReporteDTO garantiasServicio(int diasVentana) {
        if (diasVentana < 1 || diasVentana > 730) {
            throw new BadRequestException("La ventana de días debe estar entre 1 y 730.");
        }
        LocalDate hoy = LocalDate.now();
        List<Reparacion> vencidas = reparacionRepository.findByEstadoAndFechaFinGarantiaServicioIsNotNullAndFechaFinGarantiaServicioBefore(
                EstadoReparacion.ENTREGADO, hoy);
        List<Reparacion> proximas = reparacionRepository.findByEstadoAndFechaFinGarantiaServicioBetween(
                EstadoReparacion.ENTREGADO, hoy, hoy.plusDays(diasVentana));
        List<GarantiaServicioLineaDTO> lineas = new ArrayList<>();
        for (Reparacion r : vencidas) {
            lineas.add(lineaGarantia(r, "VENCIDA"));
        }
        for (Reparacion r : proximas) {
            lineas.add(lineaGarantia(r, "PRÓXIMA"));
        }
        lineas.sort(Comparator.comparing(GarantiaServicioLineaDTO::getFechaFinGarantia,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return GarantiasServicioReporteDTO.builder()
                .generadoEn(Instant.now())
                .diasVentana(diasVentana)
                .vencidas(vencidas.size())
                .proximasAVencer(proximas.size())
                .lineas(lineas)
                .build();
    }

    private GarantiaServicioLineaDTO lineaGarantia(Reparacion r, String situacion) {
        Usuario c = r.getCliente();
        return GarantiaServicioLineaDTO.builder()
                .reparacionId(r.getId())
                .numeroTicket(r.getNumeroTicket())
                .clienteNombre(c.getNombre() + " " + c.getApellido())
                .fechaFinGarantia(r.getFechaFinGarantiaServicio())
                .situacion(situacion)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientesComprasReporteDTO clientesCompras(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        long distintos = pedidoRepository.contarClientesDistintosConPedidoPagado(d1, d2);
        List<Object[]> top = pedidoRepository.topClientesPorMontoPagado(d1, d2, PageRequest.of(0, TOP_CLIENTES));
        List<ClienteCompraLineaDTO> lineas = top.stream()
                .map(row -> ClienteCompraLineaDTO.builder()
                        .clienteCedula((String) row[0])
                        .clienteNombre((String) row[1])
                        .pedidosPagados(((Number) row[2]).longValue())
                        .montoTotal(((Number) row[3]).doubleValue())
                        .build())
                .collect(Collectors.toList());
        return ClientesComprasReporteDTO.builder()
                .meta(meta(desde, hasta))
                .clientesDistintosConCompra(distintos)
                .topClientes(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuariosPorRolReporteDTO usuariosPorRol() {
        List<UsuarioPorRolLineaDTO> lineas = new ArrayList<>();
        long total = 0;
        for (RolUsuario rol : RolUsuario.values()) {
            long c = usuarioRepository.countByRol(rol);
            long a = usuarioRepository.countByRolAndActivoTrue(rol);
            total += c;
            lineas.add(UsuarioPorRolLineaDTO.builder()
                    .rol(rol.name())
                    .cantidadUsuarios(c)
                    .activos(a)
                    .build());
        }
        lineas.sort(Comparator.comparing(UsuarioPorRolLineaDTO::getRol));
        return UsuariosPorRolReporteDTO.builder()
                .generadoEn(Instant.now())
                .totalUsuarios(total)
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FinancieroSnapshotDTO financieroSnapshot(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime d1 = inicioDia(desde);
        LocalDateTime d2 = inicioDiaSiguiente(hasta);
        Double valorInv = reporteExistenciasService.generarReporte().getResumen().getValorInventarioActivos();
        Double ventasPag = pedidoRepository.sumarTotalPedidosPorEstadoEnPeriodo("PAGADO", d1, d2);
        Double ventasEnt = pedidoRepository.sumarTotalPedidosPorEstadoEnPeriodo("ENTREGADO", d1, d2);
        long pedidosPagados = 0L;
        for (Object[] row : pedidoRepository.contarPedidosPorEstadoEnPeriodo(d1, d2)) {
            if ("PAGADO".equals(String.valueOf(row[0]))) {
                pedidosPagados = ((Number) row[1]).longValue();
                break;
            }
        }
        return FinancieroSnapshotDTO.builder()
                .meta(meta(desde, hasta))
                .generadoEn(Instant.now())
                .valorInventarioActivos(valorInv != null ? valorInv : 0.0)
                .ventasPagadasPeriodo(ventasPag != null ? ventasPag : 0.0)
                .ventasEntregadasPeriodo(ventasEnt != null ? ventasEnt : 0.0)
                .pedidosPagadosPeriodo(pedidosPagados)
                .build();
    }
}
