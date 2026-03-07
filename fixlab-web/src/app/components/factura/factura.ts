import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CheckoutService } from '../../services/checkout.service';
import { PedidoRespDTO } from '../../models/checkout.model';

@Component({
  selector: 'app-factura',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './factura.html',
  styleUrl: './factura.css',
})
export class FacturaComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private checkoutService = inject(CheckoutService);

  pedido = signal<PedidoRespDTO | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  pedidoId = computed(() => {
    const id = this.route.snapshot.paramMap.get('id');
    return id ? parseInt(id, 10) : null;
  });

  ngOnInit(): void {
    const id = this.pedidoId();
    if (id == null || isNaN(id)) {
      this.error.set('Pedido no válido');
      this.loading.set(false);
      return;
    }
    this.checkoutService.getPedidoPorId(id).subscribe({
      next: (data) => {
        this.pedido.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje || 'No se pudo cargar la factura');
        this.loading.set(false);
      },
    });
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '-';
    return new Date(fecha).toLocaleDateString('es-CO', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  imprimir(): void {
    window.print();
  }
}
