import { Component, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormGroup,
  FormControl,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import {
  getPasswordRequirements,
  getPasswordStrength,
  getStrengthLabel,
} from '../../utils/password.utils';
import { VerificationCodeInputComponent } from '../verification-code-input/verification-code-input';

function confirmPasswordMatch(group: AbstractControl): ValidationErrors | null {
  const pass = group.get('nuevaPassword')?.value;
  const confirm = group.get('confirmarPassword')?.value;
  if (!pass || !confirm) return null;
  return pass === confirm ? null : { passwordMismatch: true };
}

type Paso = 1 | 2 | 3;

@Component({
  selector: 'app-recuperar-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, VerificationCodeInputComponent],
  templateUrl: './recuperar-password.html',
  styleUrl: './recuperar-password.css',
})
export class RecuperarPasswordComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  paso = signal<Paso>(1);
  emailParaVerificar = signal<string>('');

  formEmail = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
  });
  formCodigo = new FormGroup({
    codigo: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\d{6}$/),
    ]),
  });
  formPassword = new FormGroup(
    {
      nuevaPassword: new FormControl('', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/),
      ]),
      confirmarPassword: new FormControl('', [Validators.required]),
    },
    { validators: confirmPasswordMatch }
  );

  submitting = false;
  showPassword = false;
  showConfirm = false;
  errorMessage = '';
  success = false;

  getPasswordRequirements = getPasswordRequirements;
  getPasswordStrength = getPasswordStrength;
  getStrengthLabel = getStrengthLabel;

  onSubmitEmail(): void {
    if (this.formEmail.invalid) {
      this.formEmail.markAllAsTouched();
      return;
    }
    const value = this.formEmail.get('email')?.value?.trim();
    if (!value) return;

    this.submitting = true;
    this.errorMessage = '';

    this.authService.solicitarRecuperacionPassword(value).subscribe({
      next: () => {
        this.submitting = false;
        this.errorMessage = '';
        this.emailParaVerificar.set(value);
        this.paso.set(2);
        this.formCodigo.reset({ codigo: '' });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.submitting = false;
        const msg = err.error?.mensaje ?? err.error?.message ?? err.message;
        this.errorMessage = msg || 'No se pudo enviar el código. Verifica que el backend esté en ejecución (puerto 8081) e intenta de nuevo.';
        console.error('Error recuperar contraseña:', err);
      },
    });
  }

  onSubmitCodigo(): void {
    if (this.formCodigo.invalid) {
      this.formCodigo.markAllAsTouched();
      return;
    }
    const email = this.emailParaVerificar();
    const codigoVal = this.formCodigo.get('codigo')?.value?.trim();
    if (!email || !codigoVal) return;

    this.submitting = true;
    this.errorMessage = '';

    this.authService.verificarCodigoRecuperacion({ email, codigo: codigoVal }).subscribe({
      next: (res) => {
        this.submitting = false;
        this.errorMessage = '';
        this.tokenRecuperacion = res.token;
        this.paso.set(3);
        this.formPassword.reset({ nuevaPassword: '', confirmarPassword: '' });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.submitting = false;
        const body = err.error;
        const msg =
          (typeof body === 'object' && body?.mensaje) ||
          (typeof body === 'object' && body?.message) ||
          (typeof body === 'string' ? body : null);
        this.errorMessage = msg || 'Código incorrecto o expirado. Solicita uno nuevo.';
        this.cdr.detectChanges();
      },
    });
  }

  private tokenRecuperacion = '';

  onSubmitPassword(): void {
    if (this.formPassword.invalid || !this.tokenRecuperacion) {
      this.formPassword.markAllAsTouched();
      return;
    }
    const nuevaPassword = this.formPassword.get('nuevaPassword')?.value;
    if (!nuevaPassword) return;

    this.submitting = true;
    this.errorMessage = '';

    this.authService
      .resetPassword({ token: this.tokenRecuperacion, nuevaPassword })
      .subscribe({
        next: () => {
          this.submitting = false;
          this.success = true;
          setTimeout(() => this.router.navigate(['/login']), 2500);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage =
            err.error?.mensaje ||
            'El token pudo haber expirado. Solicita un nuevo código desde recuperar contraseña.';
        },
      });
  }

  volverAtras(): void {
    if (this.paso() === 2) {
      this.paso.set(1);
      this.formCodigo.reset({ codigo: '' });
      this.errorMessage = '';
    } else if (this.paso() === 3) {
      this.paso.set(2);
      this.formPassword.reset();
      this.errorMessage = '';
    }
  }
}
