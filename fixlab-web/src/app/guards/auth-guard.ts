import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  // En el servidor (SSR) no hay localStorage: permitir y dejar que el cliente valide tras hidratar
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  const token = authService.getToken();
  if (token) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};