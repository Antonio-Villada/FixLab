import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

/** Solo permite entrar a /primer-cambio-password si hay sesión y cambio pendiente. */
export const primerCambioPasswordGuard: CanActivateFn = () => {
  const router = inject(Router);
  const auth = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }
  if (!auth.getToken()) {
    router.navigate(['/login']);
    return false;
  }
  if (!auth.requiereCambioPasswordPendiente()) {
    router.navigate(['/home']);
    return false;
  }
  return true;
};
