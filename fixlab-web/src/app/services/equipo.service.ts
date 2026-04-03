import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { EquipoReqDTO, EquipoRespDTO } from '../models/reparacion.model';

@Injectable({ providedIn: 'root' })
export class EquipoService {
  private http = inject(HttpClient);

  private readonly base = environment.apiBaseUrl
    ? `${environment.apiBaseUrl.replace(/\/$/, '')}/api/equipos`
    : '/api/equipos';

  crear(dto: EquipoReqDTO): Observable<EquipoRespDTO> {
    return this.http.post<EquipoRespDTO>(this.base, dto);
  }

  listar(): Observable<EquipoRespDTO[]> {
    return this.http.get<EquipoRespDTO[]>(this.base);
  }

  obtenerPorId(id: number): Observable<EquipoRespDTO> {
    return this.http.get<EquipoRespDTO>(`${this.base}/${id}`);
  }
}
