import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

/** ADMIN o TECNICO — gestión de reparaciones del taller (no recepcionista). */
export const staffGuard: CanActivateFn = () => {
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
