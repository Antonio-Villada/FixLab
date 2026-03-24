import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

/** Solo permite acceso a usuarios con rol ADMIN. */
export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  // En el servidor (SSR) no hay localStorage: permitir y dejar que el cliente valide tras hidratar
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (authService.getToken() && authService.isAdmin()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};
