import { HttpClient } from '@angular/common/http';
import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common'; // Importante
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginReqDTO, TokenRespDTO } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  // Inyectamos el ID de la plataforma para saber si estamos en el navegador o servidor
  private platformId = inject(PLATFORM_ID);

  private readonly URL = 'http://localhost:8081/api/auth';
  private readonly TOKEN_KEY = 'fixlab_auth_token';

  login(loginData: LoginReqDTO): Observable<TokenRespDTO> {
    return this.http.post<TokenRespDTO>(`${this.URL}/login`, loginData).pipe(
      tap(res => {
        // Guardamos solo si estamos en el navegador
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem(this.TOKEN_KEY, res.token);
        }
      })
    );
  }

  getToken(): string | null {
    // Si estamos en el servidor (Node.js), localStorage no existe.
    // Retornamos null para evitar el error ReferenceError.
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(this.TOKEN_KEY);
    }
    return null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
    this.router.navigate(['/login']);
  }
}