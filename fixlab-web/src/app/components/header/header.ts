import { Component, inject, effect, signal, HostListener } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { CartService } from '../../services/cart.service';
import { UsuarioService } from '../../services/usuario.service';
import { environment } from '../../../environments/environment';

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

  /** Proceso 4 (PQRS): desactivado en environment.prod hasta lanzamiento. */
  readonly enablePostventaModule = environment.enablePostventaModule;

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

  /** Pestaña Taller (admin): rutas bajo /admin/taller. */
  tallerNavActivo(): boolean {
    const path = (this.router.url.split('?')[0] ?? '').trim();
    return path.startsWith('/admin/taller');
  }

  /** Menú Catálogo y Ventas (admin): pedidos, catálogo y maestros relacionados. */
  catalogoVentasNavActivo(): boolean {
    const path = (this.router.url.split('?')[0] ?? '').trim();
    return (
      path.startsWith('/admin/pedidos') ||
      path.startsWith('/admin/productos') ||
      path.startsWith('/admin/categorias') ||
      path.startsWith('/admin/tipos-producto')
    );
  }

  /** Menú Reportes (admin): rutas bajo /admin/reportes. */
  reportesNavActivo(): boolean {
    const path = (this.router.url.split('?')[0] ?? '').trim();
    return path.startsWith('/admin/reportes');
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