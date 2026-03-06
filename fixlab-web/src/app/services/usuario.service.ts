import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UsuarioRespDTO, UsuarioUpdateReqDTO } from '../models/auth.model';

const base = environment.apiBaseUrl?.replace(/\/$/, '') ?? '';
const url = base ? `${base}/api/usuarios` : '/api/usuarios';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private http = inject(HttpClient);

  getAll(): Observable<UsuarioRespDTO[]> {
    return this.http.get<UsuarioRespDTO[]>(url);
  }

  getByCedula(cedula: string): Observable<UsuarioRespDTO> {
    return this.http.get<UsuarioRespDTO>(`${url}/${cedula}`);
  }

  update(cedula: string, dto: UsuarioUpdateReqDTO): Observable<UsuarioRespDTO> {
    return this.http.put<UsuarioRespDTO>(`${url}/${cedula}`, dto);
  }

  delete(cedula: string): Observable<unknown> {
    return this.http.delete(`${url}/${cedula}`);
  }
}
