import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TipoProductoRespDTO } from '../models/product.model';

const base = environment.apiBaseUrl?.replace(/\/$/, '') ?? '';
const url = base ? `${base}/api/tipos-producto` : '/api/tipos-producto';

@Injectable({ providedIn: 'root' })
export class TipoProductoService {
  private http = inject(HttpClient);

  getAll(): Observable<TipoProductoRespDTO[]> {
    return this.http.get<TipoProductoRespDTO[]>(url);
  }

  getById(id: number): Observable<TipoProductoRespDTO> {
    return this.http.get<TipoProductoRespDTO>(`${url}/${id}`);
  }

  create(nombre: string): Observable<TipoProductoRespDTO> {
    return this.http.post<TipoProductoRespDTO>(url, { nombre });
  }

  update(id: number, nombre: string): Observable<TipoProductoRespDTO> {
    return this.http.put<TipoProductoRespDTO>(`${url}/${id}`, { nombre });
  }

  delete(id: number): Observable<unknown> {
    return this.http.delete(`${url}/${id}`);
  }
}
