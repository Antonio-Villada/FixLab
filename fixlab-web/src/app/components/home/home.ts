import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule], // Mantenemos RouterModule para los enlaces internos
  templateUrl: './home.html', 
  styleUrls: ['./home.css']
})
export class HomeComponent {
  // Ya no necesitas inyectar AuthService aquí, 
  // porque esa lógica ahora vive en HeaderComponent.
}