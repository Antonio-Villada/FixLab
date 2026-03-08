import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutService } from '../../services/checkout.service';
import { PedidoRespDTO } from '../../models/checkout.model';

@Component({
  selector: 'app-admin-pedidos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-pedidos.html',
  styleUrl: './admin-pedidos.css',
})
export class AdminPedidosComponent implements OnInit {
  private checkoutService = inject(CheckoutService);

  pedidos = signal<PedidoRespDTO[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  confirmingId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadPedidos();
  }

  loadPedidos(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.checkoutService.getTodosPedidos().subscribe({
      next: (data) => {
        this.pedidos.set(data || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al cargar pedidos');
        this.loading.set(false);
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
    return 'bg-warning text-dark';
  }

  puedeConfirmarPago(estado: string): boolean {
    const e = (estado || '').toUpperCase();
    return e === 'PENDIENTE' || e === 'PROCESANDO_PAGO' || e === '';
  }

  confirmarPago(p: PedidoRespDTO): void {
    if (!p.id || !this.puedeConfirmarPago(p.estado)) return;
    if (!confirm(`¿Confirmar pago del pedido #${p.id}?`)) return;
    this.confirmingId.set(p.id);
    this.errorMessage.set(null);
    this.checkoutService.confirmarPago(p.id).subscribe({
      next: () => {
        this.confirmingId.set(null);
        this.loadPedidos();
      },
      error: (err) => {
        this.confirmingId.set(null);
        this.errorMessage.set(err.error?.mensaje || 'Error al confirmar el pago');
      },
    });
  }
}
