import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportesService } from '../../services/reportes.service';
import {
  MovimientosStockReporte,
  ProductosSinVentasReporte,
  RotacionProductosReporte,
} from '../../models/informes-variados.model';

type TabInv = 'mov' | 'sin' | 'rot';

@Component({
  selector: 'app-admin-reporte-inventario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reporte-inventario.html',
  styleUrl: './admin-reporte-inventario.css',
})
export class AdminReporteInventarioComponent implements OnInit {
  private reportes = inject(ReportesService);

  tab = signal<TabInv>('mov');
  desde = signal('');
  hasta = signal('');
  loading = signal(false);
  error = signal<string | null>(null);

  mov = signal<MovimientosStockReporte | null>(null);
  sin = signal<ProductosSinVentasReporte | null>(null);
  rot = signal<RotacionProductosReporte | null>(null);

  ngOnInit(): void {
    const r = this.defaultRango();
    this.desde.set(r.desde);
    this.hasta.set(r.hasta);
    this.cargarTabActivo();
  }

  private defaultRango(): { desde: string; hasta: string } {
    const hasta = new Date();
    const desde = new Date();
    desde.setDate(desde.getDate() - 30);
    return { desde: desde.toISOString().slice(0, 10), hasta: hasta.toISOString().slice(0, 10) };
  }

  setTab(t: TabInv): void {
    this.tab.set(t);
    this.cargarTabActivo();
  }

  actualizar(): void {
    this.cargarTabActivo();
  }

  private cargarTabActivo(): void {
    const d = this.desde();
    const h = this.hasta();
    if (!d || !h) return;
    this.loading.set(true);
    this.error.set(null);
    const t = this.tab();
    if (t === 'mov') {
      this.reportes.getMovimientosStock(d, h).subscribe({
        next: (x) => {
          this.mov.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar movimientos de stock.');
          this.loading.set(false);
        },
      });
    } else if (t === 'sin') {
      this.reportes.getProductosSinVentas(d, h).subscribe({
        next: (x) => {
          this.sin.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar productos sin ventas.');
          this.loading.set(false);
        },
      });
    } else {
      this.reportes.getRotacionProductos(d, h).subscribe({
        next: (x) => {
          this.rot.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar rotación de productos.');
          this.loading.set(false);
        },
      });
    }
  }
}
