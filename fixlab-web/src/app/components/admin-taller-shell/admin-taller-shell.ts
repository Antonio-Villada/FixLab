import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { environment } from '../../../environments/environment';

/** Contenedor del módulo Taller (admin y técnico): barra de secciones + vista hija. */
@Component({
  selector: 'app-admin-taller-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './admin-taller-shell.html',
  styleUrl: './admin-taller-shell.css',
})
export class AdminTallerShellComponent {
  protected authService = inject(AuthService);
  readonly enablePostventaModule = environment.enablePostventaModule;
}