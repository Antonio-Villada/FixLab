import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  SolicitudPqrCambiarEstadoReqDTO,
  SolicitudPqrCreateReqDTO,
  SolicitudPqrRespDTO,
  SolicitudPqrValidacionGarantiaReqDTO,
} from '../models/pqr.model';

@Injectable({ providedIn: 'root' })
export class PqrService {
  private http = inject(HttpClient);

  private readonly base = environment.apiBaseUrl
    ? `${environment.apiBaseUrl.replace(/\/$/, '')}/api/pqrs`
    : '/api/pqrs';

  crear(dto: SolicitudPqrCreateReqDTO): Observable<SolicitudPqrRespDTO> {
    return this.http.post<SolicitudPqrRespDTO>(this.base, dto);
  }

  misSolicitudes(): Observable<SolicitudPqrRespDTO[]> {
    return this.http.get<SolicitudPqrRespDTO[]>(`${this.base}/mis-solicitudes`);
  }

  listarGestion(): Observable<SolicitudPqrRespDTO[]> {
    return this.http.get<SolicitudPqrRespDTO[]>(`${this.base}/gestion`);
  }

  obtenerPorId(id: number): Observable<SolicitudPqrRespDTO> {
    return this.http.get<SolicitudPqrRespDTO>(`${this.base}/${id}`);
  }

  cambiarEstado(id: number, dto: SolicitudPqrCambiarEstadoReqDTO): Observable<SolicitudPqrRespDTO> {
    return this.http.patch<SolicitudPqrRespDTO>(`${this.base}/${id}/estado`, dto);
  }

  validacionGarantiaFisica(
    id: number,
    dto: SolicitudPqrValidacionGarantiaReqDTO
  ): Observable<SolicitudPqrRespDTO> {
    return this.http.patch<SolicitudPqrRespDTO>(`${this.base}/${id}/validacion-garantia-fisica`, dto);
  }

  uploadEvidencia(archivo: File): Observable<{ url: string }> {
    const form = new FormData();
    form.append('archivo', archivo, archivo.name);
    return this.http.post<{ url: string }>(`${this.base}/evidencias/upload`, form);
  }
}
