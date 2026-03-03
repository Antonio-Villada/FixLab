import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { RegistroReqDTO, RolUsuario } from '../../models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css'] // Asegúrate de tener este archivo o quita la línea
})
export class RegisterComponent {
  // Inyección de servicios modernos con inject()
  private authService = inject(AuthService);
  private router = inject(Router);

  registroForm = new FormGroup({
    cedula: new FormControl('', [
      Validators.required,
      Validators.minLength(5),
      Validators.maxLength(20)
    ]),
    nombre: new FormControl('', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(100)
    ]),
    apellidos: new FormControl('', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(100)
    ]),
    direccion: new FormControl('', [
      Validators.required,
      Validators.maxLength(255)
    ]),
    email: new FormControl('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/)
    ]),
    telefono: new FormControl('', [
      Validators.required,
      Validators.pattern('^[0-9]+$')
    ])
  });

  onSubmit() {
    if (this.registroForm.valid) {
      const v = this.registroForm.getRawValue();
      const datosRegistro: RegistroReqDTO = {
        cedula: v.cedula!.trim(),
        nombre: v.nombre!,
        apellidos: v.apellidos!,
        direccion: v.direccion!,
        email: v.email!,
        password: v.password!,
        telefono: v.telefono!,
        rol: RolUsuario.CLIENTE
      };

      // Llamada al servicio
      this.authService.register(datosRegistro).subscribe({
        next: (res) => {
          alert('¡Bienvenido a FixLab! Cuenta de cliente creada con éxito.');
          this.router.navigate(['/login']);
        },
        error: (err) => {
          console.error('Error en el registro:', err);
          // Si el backend envía un MensajeRespDTO usamos err.error.mensaje
          const msgError = err.error?.mensaje || 'No se pudo completar el registro. Intenta de nuevo.';
          alert('Error: ' + msgError);
        }
      });
    } else {
      // Marcar todos los campos como tocados para mostrar errores visuales
      this.registroForm.markAllAsTouched();
    }
  }
}