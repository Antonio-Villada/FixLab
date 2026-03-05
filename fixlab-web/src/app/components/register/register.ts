import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { RegistroReqDTO } from '../../models/auth.model';

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
  private authService = inject(AuthService);
  private router = inject(Router);

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
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/),
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
    this.authService.register(datosRegistro).subscribe({
      next: (res) => {
        this.submitting = false;
        alert(res.mensaje || 'Cuenta de cliente creada. Revisa tu correo para verificar.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.submitting = false;
        const msgError =
          err.error?.mensaje || 'No se pudo completar el registro. Intenta de nuevo.';
        alert('Error: ' + msgError);
      },
    });
  }
}
