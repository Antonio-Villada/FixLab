import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common'; // Para usar @if
import { AuthService } from '../../services/auth'; // Ajusta la ruta

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.html'
})
export class HeaderComponent {
  public authService = inject(AuthService); // Inyectamos el servicio de autenticación

  // Variable para mostrar la cantidad de productos
  cartCount: number = 0;
}