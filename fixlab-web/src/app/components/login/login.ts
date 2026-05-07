import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { LoginReqDTO } from '../../models/auth.model';
import { VerificationCodeInputComponent } from '../verification-code-input/verification-code-input';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, VerificationCodeInputComponent],
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
})
export class Login implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  loginForm!: FormGroup;
  codigoForm!: FormGroup;
  loginStep: 'credenciales' | 'codigo' = 'credenciales';
  emailMascarado = '';
  showPassword = false;
  /** Evita doble envío y asegura un solo clic efectivo mientras responde el API. */
  iniciandoSesion = false;

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)]),
    });
    this.codigoForm = new FormGroup({
      codigo: new FormControl('', [Validators.required, Validators.pattern(/^\d{6}$/)]),
    });
  }

  onSubmit(): void {
    if (this.iniciandoSesion || this.loginForm.invalid) {
      if (this.loginForm.invalid) {
        this.loginForm.markAllAsTouched();
      }
      return;
    }

    const raw = this.loginForm.getRawValue();
    const credentials: LoginReqDTO = {
      email: (raw.email ?? '').trim(),
      password: raw.password,
    };

    this.iniciandoSesion = true;
    this.authService
      .loginIniciar(credentials)
      .pipe(finalize(() => (this.iniciandoSesion = false)))
      .subscribe({
        next: (response) => {
          const paso = response?.paso;
          if (paso === 'CODIGO_ENVIADO' || paso === 'codigo_enviado') {
            this.emailMascarado = response.emailMascarado ?? '';
            this.loginStep = 'codigo';
            this.cdr.detectChanges();
          }
        },
        error: (err) => {
          console.error('Error en el login', err);
          const errorMsg = err.error?.mensaje || 'Credenciales incorrectas. Intente de nuevo.';
          alert(errorMsg);
        },
      });
  }

  volverACredenciales(): void {
    this.loginStep = 'credenciales';
    this.codigoForm.reset();
  }

  onSubmitCodigo(): void {
    if (this.codigoForm.invalid) {
      this.codigoForm.markAllAsTouched();
      return;
    }
    const email = this.loginForm.get('email')?.value?.trim();
    const codigo = this.codigoForm.get('codigo')?.value?.trim();
    this.authService.loginVerificarCodigo({ email, codigo }).subscribe({
      next: (res) => {
        if (res.requiereCambioPassword) {
          this.router.navigate(['/primer-cambio-password']);
          return;
        }
        const rol = this.authService.getRol();
        if (rol === 'ADMIN') {
          this.router.navigate(['/home']);
        } else if (rol === 'TECNICO') {
          this.router.navigate(['/dashboard']);
        } else if (rol === 'RECEPCIONISTA') {
          this.router.navigate(['/home']);
        } else {
          this.router.navigate(['/home']);
        }
      },
      error: (err) => {
        const errorMsg = err.error?.mensaje || 'Código incorrecto o expirado.';
        alert(errorMsg);
      },
    });
  }
}
