import { Component, OnInit, AfterViewInit, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { LoginReqDTO } from '../../models/auth.model';
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    grecaptcha?: {
      render: (container: string | HTMLElement, options: { sitekey: string }) => number;
      getResponse: (widgetId?: number) => string;
      reset: (widgetId?: number) => void;
      execute: () => void;
    };
    onRecaptchaLoad?: () => void;
  }
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login implements OnInit, AfterViewInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  loginForm!: FormGroup;
  recaptchaSiteKey = environment.recaptchaSiteKey ?? '';
  captchaWidgetId: number | null = null;
  captchaReady = false;
  showPassword = false;

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)]),
    });
  }

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId) || !this.recaptchaSiteKey) return;
    this.loadRecaptchaScript();
  }

  private loadRecaptchaScript(): void {
    if (typeof document === 'undefined') return;
    if (document.querySelector('script[src*="google.com/recaptcha"]')) {
      this.renderCaptcha();
      return;
    }
    if (typeof window !== 'undefined') window.onRecaptchaLoad = () => this.renderCaptcha();
    const script = document.createElement('script');
    script.src = `https://www.google.com/recaptcha/api.js?onload=onRecaptchaLoad&render=explicit`;
    script.async = true;
    script.defer = true;
    document.head.appendChild(script);
  }

  private renderCaptcha(): void {
    if (typeof document === 'undefined' || typeof window === 'undefined') return;
    if (!window.grecaptcha || !this.recaptchaSiteKey) return;
    const container = document.getElementById('recaptcha-container');
    if (!container || container.hasChildNodes()) return;
    try {
      this.captchaWidgetId = window.grecaptcha.render(container, {
        sitekey: this.recaptchaSiteKey,
      });
      this.captchaReady = true;
    } catch (e) {
      console.warn('reCAPTCHA render error', e);
    }
  }

  private getCaptchaResponse(): string {
    if (typeof window === 'undefined' || !window.grecaptcha) return '';
    return window.grecaptcha.getResponse(this.captchaWidgetId ?? undefined);
  }

  private resetCaptcha(): void {
    if (typeof window !== 'undefined' && window.grecaptcha && this.captchaWidgetId != null) {
      window.grecaptcha.reset(this.captchaWidgetId);
    }
  }

  onSubmit(): void {
    if (this.recaptchaSiteKey && !this.getCaptchaResponse()) {
      alert('Por favor, completa el captcha para continuar.');
      return;
    }
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const credentials: LoginReqDTO = this.loginForm.getRawValue();

    this.authService.login(credentials).subscribe({
      next: (response) => {
        const rol = response.rol ?? this.authService.getRol();
        if (rol === 'ADMIN') {
          this.router.navigate(['/productos']);
        } else if (rol === 'TECNICO') {
          this.router.navigate(['/dashboard']);
        } else {
          this.router.navigate(['/home']);
        }
      },
      error: (err) => {
        console.error('Error en el login', err);
        this.resetCaptcha();
        const errorMsg = err.error?.mensaje || 'Credenciales incorrectas. Intente de nuevo.';
        alert(errorMsg);
      },
    });
  }
}
