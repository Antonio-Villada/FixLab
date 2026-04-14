import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { environment } from '../../environments/environment';

/** Bloquea rutas de postventa/PQRS si el módulo está desactivado en el environment (p. ej. producción). */
export const postventaFeatureGuard: CanActivateFn = () => {
  if (environment.enablePostventaModule) {
    return true;
  }
  inject(Router).navigate(['/home']);
  return false;
};
