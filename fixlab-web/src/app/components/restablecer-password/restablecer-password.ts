import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';

function confirmPasswordMatch(group: AbstractControl): ValidationErrors | null {
  const nueva = group.get('nuevaPassword')?.value;
  const confirmar = group.get('confirmarPassword')?.value;
  if (!nueva || !confirmar) return null;
  return nueva === confirmar ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-restablecer-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './restablecer-password.html',
  styleUrl: './restablecer-password.css',
})
export class RestablecerPasswordComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  token = signal<string | null>(null);
  tokenInvalido = computed(() => {
    const t = this.token();
    return t === null || t === '';
  });

  form = new FormGroup(
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
  success = false;
  errorMessage = '';

  ngOnInit(): void {
    const t = this.route.snapshot.queryParamMap.get('token');
    this.token.set(t ?? null);
  }

  onSubmit(): void {
    if (this.tokenInvalido()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const nuevaPassword = this.form.get('nuevaPassword')?.value;
    if (!nuevaPassword || !this.token()) return;

    this.submitting = true;
    this.errorMessage = '';

    this.authService
      .resetPassword({ token: this.token()!, nuevaPassword })
      .subscribe({
        next: () => {
          this.submitting = false;
          this.success = true;
          setTimeout(() => this.router.navigate(['/login']), 2500);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage =
            err.error?.mensaje || 'El enlace pudo haber expirado. Solicita uno nuevo desde recuperar contraseña.';
        },
      });
  }
}
