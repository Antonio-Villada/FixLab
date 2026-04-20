import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

const RUTA_PRIMER_CAMBIO = '/primer-cambio-password';

export const authGuard: CanActivateFn = (_route, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  // En el servidor (SSR) no hay localStorage: permitir y dejar que el cliente valide tras hidratar
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  const token = authService.getToken();
  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  const url = (state.url.split('?')[0] ?? '').trim();
  if (
    authService.requiereCambioPasswordPendiente() &&
    url !== RUTA_PRIMER_CAMBIO &&
    !url.startsWith(RUTA_PRIMER_CAMBIO + '/')
  ) {
    return router.parseUrl(RUTA_PRIMER_CAMBIO);
  }

  return true;
};