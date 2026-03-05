import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CategoriaRespDTO } from '../models/product.model';

const base = environment.apiBaseUrl?.replace(/\/$/, '') ?? '';
const url = base ? `${base}/api/categorias` : '/api/categorias';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private http = inject(HttpClient);

  getAll(): Observable<CategoriaRespDTO[]> {
    return this.http.get<CategoriaRespDTO[]>(url);
  }

  getById(id: number): Observable<CategoriaRespDTO> {
    return this.http.get<CategoriaRespDTO>(`${url}/${id}`);
  }

  create(nombre: string): Observable<CategoriaRespDTO> {
    return this.http.post<CategoriaRespDTO>(url, { nombre });
  }

  update(id: number, nombre: string): Observable<CategoriaRespDTO> {
    return this.http.put<CategoriaRespDTO>(`${url}/${id}`, { nombre });
  }

  delete(id: number): Observable<unknown> {
    return this.http.delete(`${url}/${id}`);
  }
}
