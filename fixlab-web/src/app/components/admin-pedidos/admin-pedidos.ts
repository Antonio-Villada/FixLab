import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CheckoutService } from '../../services/checkout.service';
import { CategoriaService } from '../../services/categoria.service';
import { TipoProductoService } from '../../services/tipo-producto.service';
import { PedidoRespDTO } from '../../models/checkout.model';
import { CategoriaRespDTO, TipoProductoRespDTO } from '../../models/product.model';

const ESTADOS_PEDIDO = [
  { value: '', label: 'Todos los estados' },
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'PROCESANDO_PAGO', label: 'Procesando pago' },
  { value: 'PAGADO', label: 'Pagado' },
  { value: 'ENVIADO', label: 'Enviado' },
  { value: 'ENTREGADO', label: 'Entregado' },
  { value: 'CANCELADO', label: 'Cancelado' },
];

export interface ProductoMasVendido {
  productoId: number;
  nombreProducto: string;
  cantidadTotal: number;
  categoriaNombre?: string;
  tipoProductoNombre?: string;
}

@Component({
  selector: 'app-admin-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-pedidos.html',
  styleUrl: './admin-pedidos.css',
})
export class AdminPedidosComponent implements OnInit {
  private checkoutService = inject(CheckoutService);
  private categoriaService = inject(CategoriaService);
  private tipoProductoService = inject(TipoProductoService);

  readonly ESTADOS = ESTADOS_PEDIDO;

  pedidos = signal<PedidoRespDTO[]>([]);
  categorias = signal<CategoriaRespDTO[]>([]);
  tiposProducto = signal<TipoProductoRespDTO[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  confirmingId = signal<number | null>(null);

  filterEstado = signal<string>('');
  filterCategoriaId = signal<number | ''>('');
  filterTipoProductoId = signal<number | ''>('');
  mostrarProductosMasVendidos = signal(false);

  filteredPedidos = computed(() => {
    const list = this.pedidos();
    const estado = this.filterEstado();
    const catId = this.filterCategoriaId();
    const tipoId = this.filterTipoProductoId();
    if (!estado && catId === '' && tipoId === '') return list;
    return list.filter((p) => {
      if (estado && (p.estado || '').toUpperCase() !== estado) return false;
      if (catId !== '' && !this.pedidoTieneCategoria(p, catId as number)) return false;
      if (tipoId !== '' && !this.pedidoTieneTipoProducto(p, tipoId as number)) return false;
      return true;
    });
  });

  productosMasVendidos = computed(() => {
    const list = this.pedidos();
    const catId = this.filterCategoriaId();
    const tipoId = this.filterTipoProductoId();
    const map = new Map<number, ProductoMasVendido>();
    for (const p of list) {
      for (const d of p.detalles || []) {
        if (catId !== '' && d.categoriaId !== catId) continue;
        if (tipoId !== '' && d.tipoProductoId !== tipoId) continue;
        const existing = map.get(d.productoId);
        if (existing) {
          existing.cantidadTotal += d.cantidad;
        } else {
          map.set(d.productoId, {
            productoId: d.productoId,
            nombreProducto: d.nombreProducto,
            cantidadTotal: d.cantidad,
            categoriaNombre: d.categoriaNombre,
            tipoProductoNombre: d.tipoProductoNombre,
          });
        }
      }
    }
    return Array.from(map.values()).sort((a, b) => b.cantidadTotal - a.cantidadTotal);
  });

  ngOnInit(): void {
    this.loadPedidos();
    this.categoriaService.getAll().subscribe({
      next: (data) => this.categorias.set(data || []),
      error: () => this.categorias.set([]),
    });
    this.tipoProductoService.getAll().subscribe({
      next: (data) => this.tiposProducto.set(data || []),
      error: () => this.tiposProducto.set([]),
    });
  }

  private pedidoTieneCategoria(p: PedidoRespDTO, categoriaId: number): boolean {
    return (p.detalles || []).some((d) => d.categoriaId === categoriaId);
  }

  private pedidoTieneTipoProducto(p: PedidoRespDTO, tipoProductoId: number): boolean {
    return (p.detalles || []).some((d) => d.tipoProductoId === tipoProductoId);
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
