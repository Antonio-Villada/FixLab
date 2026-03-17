import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutService } from '../../services/checkout.service';
import { ProductService } from '../../services/product';
import { PedidoRespDTO } from '../../models/checkout.model';
import { Product, CategoriaRespDTO } from '../../models/product.model';

@Component({
  selector: 'app-admin-pedidos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-pedidos.html',
  styleUrl: './admin-pedidos.css',
})
export class AdminPedidosComponent implements OnInit {
  private checkoutService = inject(CheckoutService);
  private productService = inject(ProductService);

  pedidos = signal<PedidoRespDTO[]>([]);
  productosMasVendidos = signal<Product[]>([]);
  categorias = signal<CategoriaRespDTO[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  confirmingId = signal<number | null>(null);

  /** Filtro principal: '' = todos pedidos, 'mas_vendidos' = reporte productos, o estado (PENDIENTE, PAGADO, etc.). */
  filtroActual = signal<string>('');
  /** Categoría seleccionada (0 = todas). */
  categoriaIdActual = signal<number>(0);

  readonly OPCIONES_FILTRO: { valor: string; etiqueta: string }[] = [
    { valor: '', etiqueta: 'Todos los pedidos' },
    { valor: 'mas_vendidos', etiqueta: 'Más vendidos (productos)' },
    { valor: 'PENDIENTE', etiqueta: 'Pendiente' },
    { valor: 'PAGADO', etiqueta: 'Pagado' },
    { valor: 'EN_PREPARACION', etiqueta: 'En preparación' },
    { valor: 'DESPACHADO', etiqueta: 'Despachado' },
    { valor: 'ENTREGADO', etiqueta: 'Entregado' },
  ];

  ngOnInit(): void {
    this.loadCategorias();
    this.loadData();
  }

  loadCategorias(): void {
    this.productService.getCategorias().subscribe({
      next: (list) => this.categorias.set(list || []),
      error: () => this.categorias.set([]),
    });
  }

  loadData(): void {
    const filtro = this.filtroActual();
    if (filtro === 'mas_vendidos') {
      this.loadMasVendidos();
    } else {
      this.loadPedidos();
    }
  }

  loadPedidos(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    const estado = this.filtroActual() || undefined;
    const catId = this.categoriaIdActual();
    const categoriaId = catId > 0 ? catId : undefined;
    this.checkoutService.getTodosPedidos(estado, categoriaId).subscribe({
      next: (data) => {
        this.pedidos.set(data || []);
        this.productosMasVendidos.set([]);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al cargar pedidos');
        this.loading.set(false);
      },
    });
  }

  loadMasVendidos(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    const catId = this.categoriaIdActual();
    const categoriaId = catId > 0 ? catId : undefined;
    this.productService.getProducts('mas_vendidos', categoriaId).subscribe({
      next: (data) => {
        this.productosMasVendidos.set(data || []);
        this.pedidos.set([]);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al cargar más vendidos');
        this.loading.set(false);
      },
    });
  }

  onFiltroChange(valor: string): void {
    this.filtroActual.set(valor);
    this.loadData();
  }

  onCategoriaChange(valor: string): void {
    const id = valor === '' || valor === '0' ? 0 : Number(valor);
    this.categoriaIdActual.set(id);
    this.loadData();
  }

  isVistaMasVendidos(): boolean {
    return this.filtroActual() === 'mas_vendidos';
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
