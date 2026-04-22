import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

/**
 * Contenedor de reportes administrativos: pestañas por tipo de reporte y vista hija.
 */
@Component({
  selector: 'app-admin-reportes-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './admin-reportes-shell.html',
  styleUrl: './admin-reportes-shell.css',
})
export class AdminReportesShellComponent {}
