import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TipoEquipoRespDTO } from '../models/reparacion.model';

const base = environment.apiBaseUrl?.replace(/\/$/, '') ?? '';
const url = base ? `${base}/api/tipos-equipo` : '/api/tipos-equipo';

@Injectable({ providedIn: 'root' })
export class TipoEquipoService {
  private http = inject(HttpClient);

  getAll(): Observable<TipoEquipoRespDTO[]> {
    return this.http.get<TipoEquipoRespDTO[]>(url);
  }

  getById(id: number): Observable<TipoEquipoRespDTO> {
    return this.http.get<TipoEquipoRespDTO>(`${url}/${id}`);
  }

  create(nombre: string): Observable<TipoEquipoRespDTO> {
    return this.http.post<TipoEquipoRespDTO>(url, { nombre });
  }

  update(id: number, nombre: string): Observable<TipoEquipoRespDTO> {
    return this.http.put<TipoEquipoRespDTO>(`${url}/${id}`, { nombre });
  }

  delete(id: number): Observable<unknown> {
    return this.http.delete(`${url}/${id}`);
  }
}
