import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

/** Solo permite acceso a usuarios con rol CLIENTE. */
export const clienteGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  // En SSR no hay localStorage: permitir y validar en el cliente tras hidratar
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (authService.getToken() && authService.isCliente()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};

