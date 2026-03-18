import {
  AbstractControl,
  AsyncValidatorFn,
  ValidationErrors,
} from '@angular/forms';
import { AuthService } from '../services/auth';
import { Observable, of } from 'rxjs';
import { debounceTime, map, switchMap, catchError } from 'rxjs/operators';

const DEBOUNCE_MS = 400;
const EMAIL_REGEX = /^[^@]+@[^@]+$/;

/**
 * Validador asíncrono que consulta al backend si el correo es temporal/desechable.
 * El backend usa la lista de https://github.com/disposable/disposable-email-domains
 */
export function disposableEmailAsyncValidator(authService: AuthService): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const value = control.value;
    if (!value || typeof value !== 'string') return of(null);
    const email = value.trim();
    if (!email || !EMAIL_REGEX.test(email)) return of(null);

    return of(email).pipe(
      debounceTime(DEBOUNCE_MS),
      switchMap((e) => authService.checkDisposable(e)),
      map((res) =>
        res.disposable
          ? {
              disposableEmail: {
                message:
                  'No se permite el registro con correos temporales. Utiliza un correo permanente.',
              },
            }
          : null
      ),
      catchError(() => of(null)) // Si el API falla, no bloquear; el backend validará al enviar
    );
  };
}
