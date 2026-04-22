package com.software.fixlab.controller;

import com.software.fixlab.dto.resp.informes.*;
import com.software.fixlab.service.interfaces.InformesVariadosService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InformesVariadosController {

    private final InformesVariadosService informesVariadosService;

    private LocalDate defDesde(LocalDate desde) {
        return desde != null ? desde : LocalDate.now().minusDays(30);
    }

    private LocalDate defHasta(LocalDate hasta) {
        return hasta != null ? hasta : LocalDate.now();
    }

    @GetMapping(value = "/inventario/movimientos-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    public MovimientosStockReporteDTO movimientosStock(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.movimientosStock(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/inventario/productos-sin-ventas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductosSinVentasReporteDTO sinVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.productosSinVentas(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/inventario/rotacion-productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public RotacionProductosReporteDTO rotacion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.rotacionProductos(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/ventas/resumen", produces = MediaType.APPLICATION_JSON_VALUE)
    public VentasResumenDTO ventasResumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.ventasResumen(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/ventas/por-categoria", produces = MediaType.APPLICATION_JSON_VALUE)
    public VentasPorCategoriaReporteDTO ventasCategoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.ventasPorCategoria(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/ventas/top-productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public TopProductosReporteDTO topProductos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "15") int limite) {
        return informesVariadosService.topProductosVendidos(defDesde(desde), defHasta(hasta), limite);
    }

    @GetMapping(value = "/ventas/pedidos-logistica", produces = MediaType.APPLICATION_JSON_VALUE)
    public PedidosLogisticaReporteDTO pedidosLogistica() {
        return informesVariadosService.pedidosPendientesLogistica();
    }

    @GetMapping(value = "/taller/reparaciones-por-estado", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReparacionesPorEstadoReporteDTO repEstado(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.reparacionesPorEstado(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/taller/rendimiento-tecnico", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReparacionesPorTecnicoReporteDTO repTecnico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.reparacionesPorTecnico(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/taller/repuestos", produces = MediaType.APPLICATION_JSON_VALUE)
    public RepuestosTallerReporteDTO repuestos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.repuestosTaller(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/postventa/pqrs", produces = MediaType.APPLICATION_JSON_VALUE)
    public PqrsResumenReporteDTO pqrs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.pqrsResumen(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/postventa/garantias-servicio", produces = MediaType.APPLICATION_JSON_VALUE)
    public GarantiasServicioReporteDTO garantias(
            @RequestParam(defaultValue = "45") int diasVentana) {
        return informesVariadosService.garantiasServicio(diasVentana);
    }

    @GetMapping(value = "/administracion/clientes-compras", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClientesComprasReporteDTO clientes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.clientesCompras(defDesde(desde), defHasta(hasta));
    }

    @GetMapping(value = "/administracion/usuarios-por-rol", produces = MediaType.APPLICATION_JSON_VALUE)
    public UsuariosPorRolReporteDTO usuarios() {
        return informesVariadosService.usuariosPorRol();
    }

    @GetMapping(value = "/administracion/financiero-snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public FinancieroSnapshotDTO financiero(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return informesVariadosService.financieroSnapshot(defDesde(desde), defHasta(hasta));
    }
}
