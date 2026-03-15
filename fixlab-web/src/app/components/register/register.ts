import { Component, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { RegistroReqDTO } from '../../models/auth.model';
import { disposableEmailAsyncValidator } from '../../validators/disposable-email.validator';

function confirmPasswordMatch(group: AbstractControl): ValidationErrors | null {
  const pass = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  if (!pass || !confirm) return null;
  return pass === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css'],
})
export class RegisterComponent {
  protected authService = inject(AuthService);
  private router = inject(Router);

  showPassword = false;
  showConfirmPassword = false;

  registroForm = new FormGroup(
    {
      cedula: new FormControl('', [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(20),
      ]),
      nombre: new FormControl('', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
      ]),
      apellido: new FormControl('', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
      ]),
      email: new FormControl(
        '',
        [Validators.required, Validators.email],
        [disposableEmailAsyncValidator(this.authService)]
      ),
      password: new FormControl('', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/),
      ]),
      confirmPassword: new FormControl('', [Validators.required]),
      telefono: new FormControl('', [
        Validators.required,
        Validators.pattern(/^[0-9+\-\s]+$/),
        Validators.minLength(7),
        Validators.maxLength(20),
      ]),
      acceptTerms: new FormControl(false, [Validators.requiredTrue]),
    },
    { validators: confirmPasswordMatch }
  );

  submitting = false;
  /** Foto de perfil seleccionada (opcional). */
  selectedFoto: File | null = null;
  /** URL de vista previa de la foto (para mostrar en el formulario). */
  fotoPreviewUrl = signal<string | null>(null);
  @ViewChild('fotoInput') fotoInputRef: ElementRef<HTMLInputElement> | null = null;
  /** Después del registro exitoso se muestra el paso de verificación. */
  emailParaVerificar = signal<string | null>(null);
  codigoVerificacion = new FormControl<string>('', [
    Validators.required,
    Validators.pattern(/^\d{6}$/),
  ]);
  verifying = false;

  onSubmit(): void {
    if (this.registroForm.invalid) {
      this.registroForm.markAllAsTouched();
      return;
    }

    const v = this.registroForm.getRawValue();
    const datosRegistro: RegistroReqDTO = {
      cedula: v.cedula!.trim(),
      nombre: v.nombre!.trim(),
      apellido: v.apellido!.trim(),
      email: v.email!.trim(),
      password: v.password!,
      telefono: v.telefono!.trim(),
    };

    this.submitting = true;
    this.authService.registerWithPhoto(datosRegistro, this.selectedFoto).subscribe({
      next: (res) => {
        this.submitting = false;
        this.emailParaVerificar.set(datosRegistro.email);
        this.codigoVerificacion.reset('');
        this.clearFoto();
      },
      error: (err) => {
        this.submitting = false;
        const msgError =
          err.error?.mensaje || 'No se pudo completar el registro. Intenta de nuevo.';
        alert('Error: ' + msgError);
      },
    });
  }

  onFotoChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.selectedFoto = file ?? null;
    if (this.fotoPreviewUrl()) {
      URL.revokeObjectURL(this.fotoPreviewUrl()!);
    }
    if (file) {
      this.fotoPreviewUrl.set(URL.createObjectURL(file));
    } else {
      this.fotoPreviewUrl.set(null);
    }
  }

  clearFoto(): void {
    this.selectedFoto = null;
    if (this.fotoPreviewUrl()) {
      URL.revokeObjectURL(this.fotoPreviewUrl()!);
      this.fotoPreviewUrl.set(null);
    }
    if (this.fotoInputRef?.nativeElement) {
      this.fotoInputRef.nativeElement.value = '';
    }
  }

  onVerificar(): void {
    const email = this.emailParaVerificar();
    const codigo = this.codigoVerificacion.value?.trim();
    if (!email || !codigo || this.codigoVerificacion.invalid) {
      this.codigoVerificacion.markAsTouched();
      return;
    }
    this.verifying = true;
    this.authService.verificarCorreo({ email, codigo }).subscribe({
      next: (res) => {
        this.verifying = false;
        alert(res.mensaje || 'Cuenta verificada. Ya puedes iniciar sesión.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.verifying = false;
        alert(err.error?.mensaje || 'Código incorrecto o expirado. Revisa tu correo e intenta de nuevo.');
      },
    });
  }
}
