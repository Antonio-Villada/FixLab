import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('fixlab_auth_token');

  if (token) {
    return true; // El usuario puede pasar
  } else {
    router.navigate(['/login']); // Bloqueado, va al login
    return false;
  }
};