import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ClienteMostradorReqDTO,
  ClienteSugerenciaRespDTO,
  EliminarCuentaClienteReqDTO,
  MensajeRespDTO,
  PrimerCambioPasswordReqDTO,
  StaffTallerAsignableRespDTO,
  UsuarioRespDTO,
  UsuarioUpdateReqDTO,
} from '../models/auth.model';
import { AuthService } from './auth';

const base = environment.apiBaseUrl?.replace(/\/$/, '') ?? '';
const url = base ? `${base}/api/usuarios` : '/api/usuarios';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  /** Perfil del usuario logueado (compartido con header y dashboard). */
  readonly currentUser = signal<UsuarioRespDTO | null>(null);

  getAll(): Observable<UsuarioRespDTO[]> {
    return this.http.get<UsuarioRespDTO[]>(url);
  }

  getByCedula(cedula: string): Observable<UsuarioRespDTO> {
    const c = encodeURIComponent(cedula.trim());
    return this.http.get<UsuarioRespDTO>(`${url}/${c}`);
  }

  /** Registro de cliente nuevo desde mostrador (recepción). Requiere JWT de admin, técnico o recepcionista. */
  registrarClienteMostrador(dto: ClienteMostradorReqDTO): Observable<UsuarioRespDTO> {
    return this.http.post<UsuarioRespDTO>(`${url}/cliente-mostrador`, dto);
  }

  /** Búsqueda por fragmento de cédula (mín. 2 caracteres en backend). */
  buscarSugerenciasClientes(q: string): Observable<ClienteSugerenciaRespDTO[]> {
    const params = new HttpParams().set('q', q.trim());
    return this.http.get<ClienteSugerenciaRespDTO[]>(`${url}/sugerencias-clientes`, { params });
  }

  /** Técnicos y admins para mostrador / gestión taller. */
  listarStaffAsignableTaller(): Observable<StaffTallerAsignableRespDTO[]> {
    return this.http.get<StaffTallerAsignableRespDTO[]>(`${url}/catalogo/staff-asignable-taller`);
  }

  /** Perfil del usuario actual (requiere estar autenticado). */
  getMe(): Observable<UsuarioRespDTO> {
    return this.http.get<UsuarioRespDTO>(`${url}/me`);
  }

  /** Carga el perfil actual y lo guarda en currentUser. Retorna el observable por si se necesita manejar loading/error. */
  loadCurrentUser(): Observable<UsuarioRespDTO> {
    return this.getMe().pipe(
      tap((u) => {
        this.currentUser.set(u);
        this.authService.syncRequiereCambioPasswordFromProfile(u);
      })
    );
  }

  /** Tras primer acceso con contraseña temporal (registro en mostrador). */
  completarPrimerCambioPassword(dto: PrimerCambioPasswordReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${url}/me/primer-cambio-password`, dto);
  }

  /** Limpia currentUser (p. ej. al cerrar sesión). */
  clearCurrentUser(): void {
    this.currentUser.set(null);
  }

  /** Actualiza el perfil del usuario logueado (PUT /me). Actualiza currentUser con la respuesta. */
  updateMe(dto: UsuarioUpdateReqDTO): Observable<UsuarioRespDTO> {
    return this.http.put<UsuarioRespDTO>(`${url}/me`, dto).pipe(
      tap((updated) => this.currentUser.set(updated))
    );
  }

  /** Sube la foto de perfil del usuario logueado (POST /me/foto). Actualiza currentUser. */
  uploadMiFoto(file: File): Observable<UsuarioRespDTO> {
    const form = new FormData();
    form.append('foto', file, file.name);
    return this.http.post<UsuarioRespDTO>(`${url}/me/foto`, form).pipe(
      tap((updated) => this.currentUser.set(updated))
    );
  }

  update(cedula: string, dto: UsuarioUpdateReqDTO): Observable<UsuarioRespDTO> {
    return this.http.put<UsuarioRespDTO>(`${url}/${cedula}`, dto);
  }

  delete(cedula: string): Observable<unknown> {
    return this.http.delete(`${url}/${cedula}`);
  }

  /** Eliminación lógica de la cuenta del cliente autenticado (requiere contraseña). */
  eliminarMiCuenta(dto: EliminarCuentaClienteReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${url}/me/eliminar-cuenta`, dto);
  }
}
