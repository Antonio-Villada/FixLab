import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

/** Administrador o recepcionista — alta de equipos y tickets en recepción. */
export const recepcionGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (authService.getToken() && (authService.isAdmin() || authService.isRecepcionista())) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};
