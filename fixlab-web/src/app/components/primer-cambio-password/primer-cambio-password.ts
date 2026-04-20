import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { UsuarioService } from '../../services/usuario.service';
import { AuthService } from '../../services/auth';

function confirmPasswordMatch(group: AbstractControl): ValidationErrors | null {
  const pass = group.get('nuevaPassword')?.value;
  const confirm = group.get('confirmarPassword')?.value;
  if (!pass || !confirm) {
    return null;
  }
  return pass === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-primer-cambio-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './primer-cambio-password.html',
  styleUrl: './primer-cambio-password.css',
})
export class PrimerCambioPasswordComponent {
  private fb = inject(FormBuilder);
  private usuarioService = inject(UsuarioService);
  private authService = inject(AuthService);
  private router = inject(Router);

  enviando = signal(false);
  error = signal<string | null>(null);

  form = this.fb.group(
    {
      nuevaPassword: this.fb.nonNullable.control('', [
        Validators.required,
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/),
      ]),
      confirmarPassword: this.fb.nonNullable.control('', [Validators.required]),
    },
    { validators: confirmPasswordMatch },
  );

  enviar(): void {
    this.error.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const pwd = this.form.controls.nuevaPassword.value;
    this.enviando.set(true);
    this.usuarioService
      .completarPrimerCambioPassword({ nuevaPassword: pwd })
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: () => {
          this.authService.clearRequiereCambioPasswordPendiente();
          this.usuarioService.loadCurrentUser().subscribe({
            next: () => this.router.navigate(['/home']),
            error: () => this.router.navigate(['/home']),
          });
        },
        error: (err) => {
          const m =
            err?.error?.mensaje ||
            (typeof err?.error === 'string' ? err.error : null) ||
            'No se pudo actualizar la contraseña.';
          this.error.set(m);
        },
      });
  }
}
