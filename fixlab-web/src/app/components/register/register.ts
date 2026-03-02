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

  // Definición del formulario reactivo con los campos de tu entidad Usuario
  registroForm = new FormGroup({
    nombre: new FormControl('', [
      Validators.required, 
      Validators.minLength(3)
    ]),
    email: new FormControl('', [
      Validators.required, 
      Validators.email
    ]),
    password: new FormControl('', [
      Validators.required, 
      Validators.minLength(6)
    ]),
    telefono: new FormControl('', [
      Validators.required,
      Validators.pattern('^[0-9]+$') // Validación básica para solo números
    ])
  });

  onSubmit() {
    if (this.registroForm.valid) {
      // Extraemos los datos del formulario de forma segura
      const { nombre, email, password, telefono } = this.registroForm.value;

      // Construimos el objeto final forzando el rol CLIENTE
      const datosRegistro: RegistroReqDTO = {
        nombre: nombre!,
        email: email!,
        password: password!,
        telefono: telefono!,
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