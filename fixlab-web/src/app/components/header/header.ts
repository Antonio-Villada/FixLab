import { Component, inject, effect, signal, HostListener } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { CartService } from '../../services/cart.service';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent {
  public authService = inject(AuthService);
  public cartService = inject(CartService);
  public usuarioService = inject(UsuarioService);
  private router = inject(Router);

  /** Panel de perfil/cuenta (estilo tipo Google) abierto o cerrado. */
  panelCuentaAbierto = signal(false);

  constructor() {
    effect(() => {
      if (!this.authService.isLoggedInSignal()) {
        this.usuarioService.clearCurrentUser();
        return;
      }
      if (!this.usuarioService.currentUser()) {
        this.usuarioService.loadCurrentUser().subscribe({
          error: () => this.usuarioService.clearCurrentUser(),
        });
      }
    });
  }

  goToCart(): void {
    this.router.navigate(['/carrito']);
  }

  /** Iniciales para el avatar (nombre + apellido). */
  getInitials(): string {
    const p = this.usuarioService.currentUser();
    if (!p) return '?';
    const n = (p.nombre?.trim() || '').charAt(0).toUpperCase();
    const a = (p.apellido?.trim() || '').charAt(0).toUpperCase();
    return n && a ? n + a : n || a || '?';
  }

  /** Nombre completo para el saludo (ej. "¡Hola, Jennyfer!"). */
  getNombreParaSaludo(): string {
    const p = this.usuarioService.currentUser();
    if (!p) return '';
    const nombre = (p.nombre?.trim() || '').split(/\s+/)[0];
    return nombre ? nombre.charAt(0).toUpperCase() + nombre.slice(1).toLowerCase() : '';
  }

  openPanelCuenta(): void {
    this.panelCuentaAbierto.set(true);
  }

  closePanelCuenta(): void {
    this.panelCuentaAbierto.set(false);
  }

  /** Cerrar sesión y cerrar el panel. */
  logoutAndClose(): void {
    this.closePanelCuenta();
    this.authService.logout();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.panelCuentaAbierto()) {
      this.closePanelCuenta();
    }
  }
}