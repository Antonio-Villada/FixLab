import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';

/** Solo permite acceso a usuarios con rol ADMIN. */
export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);

  if (authService.getToken() && authService.isAdmin()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};
