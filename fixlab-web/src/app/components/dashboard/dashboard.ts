import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UsuarioService } from '../../services/usuario.service';
import { CheckoutService } from '../../services/checkout.service';
import { UsuarioRespDTO } from '../../models/auth.model';
import { PedidoRespDTO } from '../../models/checkout.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private usuarioService = inject(UsuarioService);
  private checkoutService = inject(CheckoutService);

  perfil = signal<UsuarioRespDTO | null>(null);
  pedidos = signal<PedidoRespDTO[]>([]);
  loadingPerfil = signal(true);
  loadingPedidos = signal(true);
  errorPerfil = signal<string | null>(null);
  errorPedidos = signal<string | null>(null);

  ngOnInit(): void {
    this.usuarioService.getMe().subscribe({
      next: (data) => {
        this.perfil.set(data);
        this.loadingPerfil.set(false);
      },
      error: (err) => {
        this.errorPerfil.set(err.error?.mensaje || 'Error al cargar tu perfil');
        this.loadingPerfil.set(false);
      },
    });

    this.checkoutService.getMisPedidos().subscribe({
      next: (data) => {
        this.pedidos.set(data || []);
        this.loadingPedidos.set(false);
      },
      error: (err) => {
        this.errorPedidos.set(err.error?.mensaje || 'Error al cargar tus compras');
        this.loadingPedidos.set(false);
      },
    });
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '-';
    const d = new Date(fecha);
    return d.toLocaleDateString('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  estadoBadgeClass(estado: string): string {
    if (!estado) return 'bg-secondary';
    const e = estado.toUpperCase();
    if (e === 'PAGADO') return 'bg-success';
    if (e === 'ENVIADO' || e === 'ENTREGADO') return 'bg-info';
    if (e === 'CANCELADO') return 'bg-danger';
    return 'bg-secondary';
  }
}
