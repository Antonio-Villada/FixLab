import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CheckoutService } from '../../services/checkout.service';
import { PedidoRespDTO } from '../../models/checkout.model';

@Component({
  selector: 'app-mis-compras',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mis-compras.html',
  styleUrl: './mis-compras.css',
})
export class MisComprasComponent implements OnInit {
  private checkoutService = inject(CheckoutService);

  pedidos = signal<PedidoRespDTO[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  pedidosOrdenados = computed(() => {
    const list = this.pedidos() || [];
    return [...list].sort((a, b) => {
      const ta = new Date(a.fechaCreacion || 0).getTime();
      const tb = new Date(b.fechaCreacion || 0).getTime();
      return tb - ta;
    });
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.checkoutService.getMisPedidos().subscribe({
      next: (data) => {
        this.pedidos.set(data || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.mensaje || 'No se pudieron cargar tus compras.');
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
    const e = (estado || '').toUpperCase();
    if (e === 'PAGADO') return 'bg-success';
    if (e === 'EN_PREPARACION') return 'bg-primary';
    if (e === 'DESPACHADO' || e === 'ENVIADO') return 'bg-info';
    if (e === 'ENTREGADO') return 'bg-secondary';
    if (e === 'CANCELADO') return 'bg-danger';
    if (e === 'PROCESANDO_PAGO') return 'bg-warning text-dark';
    return 'bg-light text-dark';
  }
}

