import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportesService } from '../../services/reportes.service';
import {
  VentasResumenReporte,
  VentasPorCategoriaReporte,
  TopProductosReporte,
  PedidosLogisticaReporte,
} from '../../models/informes-variados.model';

type TabV = 'res' | 'cat' | 'top' | 'log';

@Component({
  selector: 'app-admin-reporte-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reporte-ventas.html',
  styleUrl: './admin-reporte-ventas.css',
})
export class AdminReporteVentasComponent implements OnInit {
  private reportes = inject(ReportesService);

  tab = signal<TabV>('res');
  desde = signal('');
  hasta = signal('');
  limiteTop = signal(15);
  loading = signal(false);
  error = signal<string | null>(null);

  res = signal<VentasResumenReporte | null>(null);
  cat = signal<VentasPorCategoriaReporte | null>(null);
  top = signal<TopProductosReporte | null>(null);
  log = signal<PedidosLogisticaReporte | null>(null);

  ngOnInit(): void {
    const r = this.defaultRango();
    this.desde.set(r.desde);
    this.hasta.set(r.hasta);
    this.cargar();
  }

  private defaultRango(): { desde: string; hasta: string } {
    const hasta = new Date();
    const desde = new Date();
    desde.setDate(desde.getDate() - 30);
    return { desde: desde.toISOString().slice(0, 10), hasta: hasta.toISOString().slice(0, 10) };
  }

  setTab(t: TabV): void {
    this.tab.set(t);
    this.cargar();
  }

  actualizar(): void {
    this.cargar();
  }

  estadosKeys(res: VentasResumenReporte): string[] {
    return Object.keys(res.pedidosPorEstado || {}).sort();
  }

  private cargar(): void {
    const d = this.desde();
    const h = this.hasta();
    if (!d || !h) return;
    this.loading.set(true);
    this.error.set(null);
    const t = this.tab();
    if (t === 'res') {
      this.reportes.getVentasResumen(d, h).subscribe({
        next: (x) => {
          this.res.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el resumen de ventas.');
          this.loading.set(false);
        },
      });
    } else if (t === 'cat') {
      this.reportes.getVentasPorCategoria(d, h).subscribe({
        next: (x) => {
          this.cat.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar ventas por categoría.');
          this.loading.set(false);
        },
      });
    } else if (t === 'top') {
      this.reportes.getTopProductos(d, h, this.limiteTop()).subscribe({
        next: (x) => {
          this.top.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el top de productos.');
          this.loading.set(false);
        },
      });
    } else {
      this.reportes.getPedidosLogistica().subscribe({
        next: (x) => {
          this.log.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar pedidos en logística.');
          this.loading.set(false);
        },
      });
    }
  }
}
