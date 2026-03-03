import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

/**
 * Ruta /admin: protegida por authGuard + adminGuard.
 * Redirige a /productos (misma vista de productos con CRUD para admin).
 */
@Component({
  selector: 'app-admin-redirect',
  standalone: true,
  template: `<div class="d-flex justify-content-center align-items-center min-vh-50"><span class="spinner-border text-primary"></span></div>`,
})
export class AdminRedirectComponent {
  private router = inject(Router);

  constructor() {
    this.router.navigate(['/productos'], { replaceUrl: true });
  }
}
