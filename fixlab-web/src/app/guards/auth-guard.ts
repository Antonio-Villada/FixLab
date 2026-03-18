import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    return true; // El usuario puede pasar
  }
  router.navigate(['/login']); // Bloqueado, va al login
  return false;
};