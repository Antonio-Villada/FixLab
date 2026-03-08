import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-recuperar-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './recuperar-password.html',
  styleUrl: './recuperar-password.css',
})
export class RecuperarPasswordComponent {
  private authService = inject(AuthService);

  email = new FormControl('', [Validators.required, Validators.email]);
  submitting = false;
  mensajeExito = false;
  errorMessage = '';

  onSubmit(): void {
    if (this.email.invalid) {
      this.email.markAsTouched();
      return;
    }
    const value = this.email.value?.trim();
    if (!value) return;

    this.submitting = true;
    this.errorMessage = '';
    this.mensajeExito = false;

    this.authService.solicitarRecuperacionPassword(value).subscribe({
      next: () => {
        this.submitting = false;
        this.mensajeExito = true;
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err.error?.mensaje || 'No se pudo enviar el correo. Intenta de nuevo.';
      },
    });
  }
}
