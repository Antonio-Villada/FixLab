package com.software.fixlab.service.interfaces;

import java.time.LocalDate;

import com.software.fixlab.dto.resp.informes.ClientesComprasReporteDTO;
import com.software.fixlab.dto.resp.informes.FinancieroSnapshotDTO;
import com.software.fixlab.dto.resp.informes.GarantiasServicioReporteDTO;
import com.software.fixlab.dto.resp.informes.MovimientosStockReporteDTO;
import com.software.fixlab.dto.resp.informes.PedidosLogisticaReporteDTO;
import com.software.fixlab.dto.resp.informes.PqrsResumenReporteDTO;
import com.software.fixlab.dto.resp.informes.ProductosSinVentasReporteDTO;
import com.software.fixlab.dto.resp.informes.ReparacionesPorEstadoReporteDTO;
import com.software.fixlab.dto.resp.informes.ReparacionesPorTecnicoReporteDTO;
import com.software.fixlab.dto.resp.informes.RepuestosTallerReporteDTO;
import com.software.fixlab.dto.resp.informes.RotacionProductosReporteDTO;
import com.software.fixlab.dto.resp.informes.TopProductosReporteDTO;
import com.software.fixlab.dto.resp.informes.UsuariosPorRolReporteDTO;
import com.software.fixlab.dto.resp.informes.VentasPorCategoriaReporteDTO;
import com.software.fixlab.dto.resp.informes.VentasResumenDTO;

public interface InformesVariadosService {

    MovimientosStockReporteDTO movimientosStock(LocalDate desde, LocalDate hasta);

    ProductosSinVentasReporteDTO productosSinVentas(LocalDate desde, LocalDate hasta);

    RotacionProductosReporteDTO rotacionProductos(LocalDate desde, LocalDate hasta);

    VentasResumenDTO ventasResumen(LocalDate desde, LocalDate hasta);

    VentasPorCategoriaReporteDTO ventasPorCategoria(LocalDate desde, LocalDate hasta);

    TopProductosReporteDTO topProductosVendidos(LocalDate desde, LocalDate hasta, int limite);

    PedidosLogisticaReporteDTO pedidosPendientesLogistica();

    ReparacionesPorEstadoReporteDTO reparacionesPorEstado(LocalDate desde, LocalDate hasta);

    ReparacionesPorTecnicoReporteDTO reparacionesPorTecnico(LocalDate desde, LocalDate hasta);

    RepuestosTallerReporteDTO repuestosTaller(LocalDate desde, LocalDate hasta);

    PqrsResumenReporteDTO pqrsResumen(LocalDate desde, LocalDate hasta);

    GarantiasServicioReporteDTO garantiasServicio(int diasVentana);

    ClientesComprasReporteDTO clientesCompras(LocalDate desde, LocalDate hasta);

    UsuariosPorRolReporteDTO usuariosPorRol();

    FinancieroSnapshotDTO financieroSnapshot(LocalDate desde, LocalDate hasta);
}
