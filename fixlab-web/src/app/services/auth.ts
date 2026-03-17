import { HttpClient } from '@angular/common/http';
import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, tap, timeout, catchError, of } from 'rxjs';
import { signal } from '@angular/core';
import {
  LoginReqDTO,
  TokenRespDTO,
  RegistroReqDTO,
  MensajeRespDTO,
  RegistroEmpleadoReqDTO,
  CambioRolReqDTO,
  VerificarCorreoReqDTO,
  ResetearPasswordDTO,
  AdminAsignarPasswordReqDTO,
} from '../models/auth.model';
import { environment } from '../../environments/environment';
import { CartService } from './cart.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private cartService = inject(CartService);

  /** Estado de sesión reactivo: se actualiza al iniciar (navegador) y al hacer login/logout. Evita verse "no logueado" al volver con Atrás desde Wompi. */
  readonly isLoggedInSignal = signal(false);

  /** URL del API de auth: apiBaseUrl + /api/auth (si apiBaseUrl vacío, usa mismo origen) */
  private readonly URL = environment.apiBaseUrl
    ? `${environment.apiBaseUrl.replace(/\/$/, '')}/api/auth`
    : '/api/auth';
  private readonly TOKEN_KEY = 'fixlab_auth_token';
  private readonly ROL_KEY = 'fixlab_user_rol';

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.isLoggedInSignal.set(!!this.getToken());
    }
  }

  /**
   * Registers a new user (Client by default from component logic)
   * @param registerData Data matching RegistroReqDTO
   * @returns Observable with success message
   */
  register(registerData: RegistroReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/registro`, registerData);
  }

  /**
   * Registro con foto de perfil opcional (multipart). Si no hay foto, usa register().
   */
  registerWithPhoto(registerData: RegistroReqDTO, foto?: File | null): Observable<MensajeRespDTO> {
    if (!foto) {
      return this.register(registerData);
    }
    const form = new FormData();
    form.append('cedula', registerData.cedula);
    form.append('nombre', registerData.nombre);
    form.append('apellido', registerData.apellido);
    form.append('email', registerData.email);
    form.append('password', registerData.password);
    form.append('telefono', registerData.telefono);
    form.append('foto', foto, foto.name);
    return this.http.post<MensajeRespDTO>(`${this.URL}/registro-con-foto`, form);
  }

  registrarEmpleado(data: RegistroEmpleadoReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/registro-empleado`, data);
  }

  /** Comprueba si un correo es temporal/desechable (lista cargada desde GitHub en el backend). */
  checkDisposable(email: string): Observable<{ disposable: boolean }> {
    const params = new URLSearchParams({ email: email.trim() });
    return this.http.get<{ disposable: boolean }>(`${this.URL}/check-disposable?${params}`);
  }

  /** Verificar correo con el código de 6 dígitos enviado al email. */
  verificarCorreo(data: VerificarCorreoReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/verificar-correo`, data);
  }

  cambiarRol(data: CambioRolReqDTO): Observable<MensajeRespDTO> {
    return this.http.put<MensajeRespDTO>(`${this.URL}/cambiar-rol`, data);
  }

  /** Solicitar envío de correo para recuperar contraseña (POST /api/auth/recuperar-password). */
  solicitarRecuperacionPassword(email: string): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/recuperar-password`, { email });
  }

  /** Restablecer contraseña con el token recibido por correo (POST /api/auth/reset-password). */
  resetPassword(data: ResetearPasswordDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/reset-password`, data);
  }

  /** Cambiar contraseña del usuario logueado (contraseña actual + nueva). */
  cambiarPassword(contraseñaActual: string, nuevaPassword: string): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/cambiar-password`, {
      contraseñaActual,
      nuevaPassword,
    });
  }

  /** Asignar nueva contraseña a un usuario (solo ADMIN, p. ej. usuario olvidó contraseña). */
  asignarNuevaPassword(data: AdminAsignarPasswordReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/admin/asignar-password`, data);
  }

  /**
   * Paso 1 del login: envía credenciales y recibe mensaje de que se envió el código al correo.
   * Timeout 20s para evitar que quede cargando si el backend no responde.
   */
  login(loginData: LoginReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/login`, loginData).pipe(
      timeout(20000),
      catchError((err) => {
        if (err.name === 'TimeoutError' || err.error?.name === 'TimeoutError') {
          throw { error: { mensaje: 'El servidor no respondió a tiempo. ¿Está el backend en marcha en localhost:8081?' } };
        }
        throw err;
      })
    );
  }

  /**
   * Paso 2 del login: envía email + código de 6 dígitos y recibe el JWT.
   */
  verificarCodigoLogin(data: VerificarCorreoReqDTO): Observable<TokenRespDTO> {
    return this.http.post<TokenRespDTO>(`${this.URL}/login/verificar-codigo`, data).pipe(
      tap(res => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem(this.TOKEN_KEY, res.token);
          if (res.rol) {
            localStorage.setItem(this.ROL_KEY, res.rol);
          }
          this.isLoggedInSignal.set(true);
        }
      })
    );
  }

  /**
   * Retrieves the stored token from localStorage
   */
  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(this.TOKEN_KEY);
    }
    return null;
  }

  /** Devuelve el email (subject) desde el JWT, si es decodificable. */
  getEmailFromToken(): string | null {
    const token = this.getToken();
    if (!token) return null;
    const payload = this.decodeJwtPayload(token);
    const sub = (payload?.['sub'] ?? payload?.['email'] ?? payload?.['username']) as unknown;
    return typeof sub === 'string' && sub.trim() ? sub.trim() : null;
  }

  private decodeJwtPayload(token: string): Record<string, unknown> | null {
    try {
      const parts = token.split('.');
      if (parts.length < 2) return null;
      const b64Url = parts[1];
      const b64 = b64Url.replace(/-/g, '+').replace(/_/g, '/');
      const padded = b64.padEnd(Math.ceil(b64.length / 4) * 4, '=');
      const json = atob(padded);
      return JSON.parse(json) as Record<string, unknown>;
    } catch {
      return null;
    }
  }

  /**
   * Checks if the user session is active
   */
  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  /**
   * Obtiene el rol del usuario (CLIENTE | ADMIN | TECNICO). Requiere que el backend lo envíe en el login.
   */
  getRol(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(this.ROL_KEY);
    }
    return null;
  }

  /** Indica si el usuario actual es administrador */
  isAdmin(): boolean {
    return this.getRol() === 'ADMIN';
  }

  /**
   * Clears session data and redirects to login
   */
  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.ROL_KEY);
      this.cartService.clear();
      this.isLoggedInSignal.set(false);
    }
    this.router.navigate(['/login']);
  }

  /** Sincroniza el signal con localStorage. Llamar en el navegador al cargar (p. ej. desde app o header) para que al volver con Atrás se vea la sesión. */
  syncLoginStateFromStorage(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.isLoggedInSignal.set(!!this.getToken());
    }
  }
}