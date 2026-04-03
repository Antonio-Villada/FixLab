import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

/**
 * Recepción dentro de /admin/taller: solo administrador.
 * El técnico se redirige a la lista (no debe usar esa pantalla).
 */
export const tallerRecepcionShellGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (!authService.getToken()) {
    router.navigate(['/login']);
    return false;
  }
  if (authService.isAdmin()) {
    return true;
  }
  if (authService.isTecnico()) {
    router.navigate(['/admin/taller/lista'], { replaceUrl: true });
    return false;
  }
  router.navigate(['/login']);
  return false;
};
