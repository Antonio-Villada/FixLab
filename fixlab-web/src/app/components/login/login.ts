import { Component, OnInit, inject } from '@angular/core'; // Usamos inject
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router'; // Importamos RouterModule
import { AuthService } from '../../services/auth'; // Verifica que la ruta termine en .service
import { LoginReqDTO } from '../../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  // IMPORTANTE: Añadimos RouterModule para que funcione el routerLink del HTML
  imports: [CommonModule, ReactiveFormsModule, RouterModule], 
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login implements OnInit {
  // Usamos inyección de dependencias moderna
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm!: FormGroup;

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)])
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      const credentials: LoginReqDTO = this.loginForm.getRawValue(); // Mejor que .value para evitar nulos
      
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
          const errorMsg = err.error?.mensaje || 'Credenciales incorrectas. Intente de nuevo.';
          alert(errorMsg);
        }
      });
    } else {
      this.loginForm.markAllAsTouched();
    }
  }
}