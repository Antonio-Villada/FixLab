import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ReparacionAsignarTecnicoReqDTO,
  ReparacionCambiarEstadoReqDTO,
  ReparacionCreateReqDTO,
  ReparacionDiagnosticoCotizacionReqDTO,
  ReparacionEvidenciaReqDTO,
  ReparacionProductoReqDTO,
  ReparacionRespDTO,
  TallerRespDTO,
  TipoEquipoRespDTO,
  TipoTallerRespDTO,
} from '../models/reparacion.model';

@Injectable({ providedIn: 'root' })
export class ReparacionService {
  private http = inject(HttpClient);

  private readonly base = environment.apiBaseUrl
    ? `${environment.apiBaseUrl.replace(/\/$/, '')}/api/reparaciones`
    : '/api/reparaciones';

  listarTiposEquipo(): Observable<TipoEquipoRespDTO[]> {
    return this.http.get<TipoEquipoRespDTO[]>(`${this.base}/catalogo/tipos-equipo`);
  }

  listarTiposTaller(): Observable<TipoTallerRespDTO[]> {
    return this.http.get<TipoTallerRespDTO[]>(`${this.base}/catalogo/tipos-taller`);
  }

  listarTalleres(): Observable<TallerRespDTO[]> {
    return this.http.get<TallerRespDTO[]>(`${this.base}/catalogo/talleres`);
  }

  /** Cliente: propias; ADMIN/TECNICO: todas. */
  listar(): Observable<ReparacionRespDTO[]> {
    return this.http.get<ReparacionRespDTO[]>(this.base);
  }

  crear(dto: ReparacionCreateReqDTO): Observable<ReparacionRespDTO> {
    return this.http.post<ReparacionRespDTO>(this.base, dto);
  }

  obtenerPorId(id: number): Observable<ReparacionRespDTO> {
    return this.http.get<ReparacionRespDTO>(`${this.base}/${id}`);
  }

  obtenerPorNumeroTicket(numero: string): Observable<ReparacionRespDTO> {
    const n = encodeURIComponent(numero.trim());
    return this.http.get<ReparacionRespDTO>(`${this.base}/por-ticket/${n}`);
  }

  aprobarCotizacion(id: number): Observable<ReparacionRespDTO> {
    return this.http.post<ReparacionRespDTO>(`${this.base}/${id}/aprobar-cotizacion`, {});
  }

  asignarTecnico(id: number, dto: ReparacionAsignarTecnicoReqDTO): Observable<ReparacionRespDTO> {
    return this.http.patch<ReparacionRespDTO>(`${this.base}/${id}/asignar-tecnico`, dto);
  }

  registrarDiagnostico(id: number, dto: ReparacionDiagnosticoCotizacionReqDTO): Observable<ReparacionRespDTO> {
    return this.http.patch<ReparacionRespDTO>(`${this.base}/${id}/diagnostico`, dto);
  }

  cambiarEstado(id: number, dto: ReparacionCambiarEstadoReqDTO): Observable<ReparacionRespDTO> {
    return this.http.patch<ReparacionRespDTO>(`${this.base}/${id}/estado`, dto);
  }

  agregarProducto(id: number, dto: ReparacionProductoReqDTO): Observable<ReparacionRespDTO> {
    return this.http.post<ReparacionRespDTO>(`${this.base}/${id}/productos`, dto);
  }

  agregarEvidencia(id: number, dto: ReparacionEvidenciaReqDTO): Observable<ReparacionRespDTO> {
    return this.http.post<ReparacionRespDTO>(`${this.base}/${id}/evidencias`, dto);
  }
}
