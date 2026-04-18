import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
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
  private route = inject(ActivatedRoute);

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
        queueMicrotask(() => this.aplicarFragmentoUrlSiCorresponde());
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

  /**
   * Centra la tarjeta del pedido en pantalla y la resalta un momento.
   * Sirve con muchas compras en pantalla o al abrir /mis-compras#pedido-123.
   */
  irAPedidoEnLista(pedidoId: number): void {
    if (typeof document === 'undefined' || pedidoId == null) return;
    const el = document.getElementById('pedido-' + pedidoId);
    if (!el) return;
    el.scrollIntoView({ behavior: 'smooth', block: 'center' });
    el.classList.remove('pedido-card-flash');
    void el.offsetWidth;
    el.classList.add('pedido-card-flash');
    window.setTimeout(() => el.classList.remove('pedido-card-flash'), 1800);
  }

  private aplicarFragmentoUrlSiCorresponde(): void {
    const frag = this.route.snapshot.fragment;
    if (!frag?.startsWith('pedido-')) return;
    const id = Number(frag.slice('pedido-'.length));
    if (!Number.isFinite(id)) return;
    this.irAPedidoEnLista(id);
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

