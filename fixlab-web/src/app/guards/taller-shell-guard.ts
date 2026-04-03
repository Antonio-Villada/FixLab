import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

/** Admin o técnico — módulo Taller con barra Lista / Gestión / Seguimiento (+ Recepción solo admin). */
export const tallerShellGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (authService.getToken() && authService.isTallerRepairStaff()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};
