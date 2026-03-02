import { HttpClient } from '@angular/common/http';
import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginReqDTO, TokenRespDTO, RegistroReqDTO, MensajeRespDTO } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  private readonly URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'fixlab_auth_token';

  /**
   * Registers a new user (Client by default from component logic)
   * @param registerData Data matching RegistroReqDTO
   * @returns Observable with success message
   */
  register(registerData: RegistroReqDTO): Observable<MensajeRespDTO> {
    return this.http.post<MensajeRespDTO>(`${this.URL}/register`, registerData);
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
   * Clears session data and redirects to login
   */
  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
    this.router.navigate(['/login']);
  }
}