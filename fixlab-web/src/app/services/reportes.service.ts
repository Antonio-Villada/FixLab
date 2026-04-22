import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ExistenciasReporte } from '../models/existencias-report.model';
import {
  MovimientosStockReporte,
  ProductosSinVentasReporte,
  RotacionProductosReporte,
  VentasResumenReporte,
  VentasPorCategoriaReporte,
  TopProductosReporte,
  PedidosLogisticaReporte,
  ReparacionesPorEstadoReporte,
  ReparacionesPorTecnicoReporte,
  RepuestosTallerReporte,
  PqrsResumenReporte,
  GarantiasServicioReporte,
  ClientesComprasReporte,
  UsuariosPorRolReporte,
  FinancieroSnapshotReporte,
} from '../models/informes-variados.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ReportesService {
  private http = inject(HttpClient);
  private readonly apiBase = environment.apiBaseUrl?.replace(/\/$/, '') ?? '';
  private readonly existenciasUrl = this.apiBase
    ? `${this.apiBase}/api/admin/reportes/existencias`
    : '/api/admin/reportes/existencias';
  private readonly reportesBase = this.apiBase
    ? `${this.apiBase}/api/admin/reportes`
    : '/api/admin/reportes';

  getReporteExistencias(): Observable<ExistenciasReporte> {
    return this.http.get<ExistenciasReporte>(this.existenciasUrl);
  }

  descargarExistenciasCsv(): Observable<Blob> {
    return this.http.get(`${this.existenciasUrl}/csv`, { responseType: 'blob' });
  }

  private rangoParams(desde: string, hasta: string): HttpParams {
    return new HttpParams().set('desde', desde).set('hasta', hasta);
  }

  getMovimientosStock(desde: string, hasta: string): Observable<MovimientosStockReporte> {
    return this.http.get<MovimientosStockReporte>(`${this.reportesBase}/inventario/movimientos-stock`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getProductosSinVentas(desde: string, hasta: string): Observable<ProductosSinVentasReporte> {
    return this.http.get<ProductosSinVentasReporte>(`${this.reportesBase}/inventario/productos-sin-ventas`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getRotacionProductos(desde: string, hasta: string): Observable<RotacionProductosReporte> {
    return this.http.get<RotacionProductosReporte>(`${this.reportesBase}/inventario/rotacion-productos`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getVentasResumen(desde: string, hasta: string): Observable<VentasResumenReporte> {
    return this.http.get<VentasResumenReporte>(`${this.reportesBase}/ventas/resumen`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getVentasPorCategoria(desde: string, hasta: string): Observable<VentasPorCategoriaReporte> {
    return this.http.get<VentasPorCategoriaReporte>(`${this.reportesBase}/ventas/por-categoria`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getTopProductos(desde: string, hasta: string, limite = 15): Observable<TopProductosReporte> {
    const params = this.rangoParams(desde, hasta).set('limite', String(limite));
    return this.http.get<TopProductosReporte>(`${this.reportesBase}/ventas/top-productos`, { params });
  }

  getPedidosLogistica(): Observable<PedidosLogisticaReporte> {
    return this.http.get<PedidosLogisticaReporte>(`${this.reportesBase}/ventas/pedidos-logistica`);
  }

  getReparacionesPorEstado(desde: string, hasta: string): Observable<ReparacionesPorEstadoReporte> {
    return this.http.get<ReparacionesPorEstadoReporte>(`${this.reportesBase}/taller/reparaciones-por-estado`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getRendimientoTecnico(desde: string, hasta: string): Observable<ReparacionesPorTecnicoReporte> {
    return this.http.get<ReparacionesPorTecnicoReporte>(`${this.reportesBase}/taller/rendimiento-tecnico`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getRepuestosTaller(desde: string, hasta: string): Observable<RepuestosTallerReporte> {
    return this.http.get<RepuestosTallerReporte>(`${this.reportesBase}/taller/repuestos`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getPqrsResumen(desde: string, hasta: string): Observable<PqrsResumenReporte> {
    return this.http.get<PqrsResumenReporte>(`${this.reportesBase}/postventa/pqrs`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getGarantiasServicio(diasVentana = 45): Observable<GarantiasServicioReporte> {
    return this.http.get<GarantiasServicioReporte>(`${this.reportesBase}/postventa/garantias-servicio`, {
      params: new HttpParams().set('diasVentana', String(diasVentana)),
    });
  }

  getClientesCompras(desde: string, hasta: string): Observable<ClientesComprasReporte> {
    return this.http.get<ClientesComprasReporte>(`${this.reportesBase}/administracion/clientes-compras`, {
      params: this.rangoParams(desde, hasta),
    });
  }

  getUsuariosPorRol(): Observable<UsuariosPorRolReporte> {
    return this.http.get<UsuariosPorRolReporte>(`${this.reportesBase}/administracion/usuarios-por-rol`);
  }

  getFinancieroSnapshot(desde: string, hasta: string): Observable<FinancieroSnapshotReporte> {
    return this.http.get<FinancieroSnapshotReporte>(`${this.reportesBase}/administracion/financiero-snapshot`, {
      params: this.rangoParams(desde, hasta),
    });
  }
}
