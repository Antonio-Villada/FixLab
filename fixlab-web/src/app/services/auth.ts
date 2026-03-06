import { HttpClient } from '@angular/common/http';
import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import {
  LoginReqDTO,
  TokenRespDTO,
  RegistroReqDTO,
  MensajeRespDTO,
  RegistroEmpleadoReqDTO,
  CambioRolReqDTO,
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

  /** URL del API de auth: apiBaseUrl + /api/auth (si apiBaseUrl vacío, usa mismo origen) */
  private readonly URL = environment.apiBaseUrl
    ? `${environment.apiBaseUrl.replace(/\/$/, '')}/api/auth`
    : '/api/auth';
  private readonly TOKEN_KEY = 'fixlab_auth_token';
  private readonly ROL_KEY = 'fixlab_user_rol';

  /**
   * Registers a new user (Client by default from component logic)
   * @param registerData Data matching RegistroReqDTO
   * @returns Observable with success message
   */
  register(registerData: RegistroReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/registro`, registerData);
  }

  registrarEmpleado(data: RegistroEmpleadoReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/registro-empleado`, data);
  }

  cambiarRol(data: CambioRolReqDTO): Observable<MensajeRespDTO> {
    return this.http.put<MensajeRespDTO>(`${this.URL}/cambiar-rol`, data);
  }

  /**
   * Authenticates a user and stores the JWT token
   * @param loginData Email and Password
   */
  login(loginData: LoginReqDTO): Observable<TokenRespDTO> {
    return this.http.post<TokenRespDTO>(`${this.URL}/login`, loginData).pipe(
      tap(res => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem(this.TOKEN_KEY, res.token);
          if (res.rol) {
            localStorage.setItem(this.ROL_KEY, res.rol);
          }
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
    }
    this.router.navigate(['/login']);
  }
}