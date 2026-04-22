import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportesService } from '../../services/reportes.service';
import {
  ReparacionesPorEstadoReporte,
  ReparacionesPorTecnicoReporte,
  RepuestosTallerReporte,
} from '../../models/informes-variados.model';

type TabT = 'est' | 'tec' | 'rep';

@Component({
  selector: 'app-admin-reporte-taller',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reporte-taller.html',
  styleUrl: './admin-reporte-taller.css',
})
export class AdminReporteTallerComponent implements OnInit {
  private reportes = inject(ReportesService);

  tab = signal<TabT>('est');
  desde = signal('');
  hasta = signal('');
  loading = signal(false);
  error = signal<string | null>(null);

  est = signal<ReparacionesPorEstadoReporte | null>(null);
  tec = signal<ReparacionesPorTecnicoReporte | null>(null);
  rep = signal<RepuestosTallerReporte | null>(null);

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

  setTab(t: TabT): void {
    this.tab.set(t);
    this.cargar();
  }

  actualizar(): void {
    this.cargar();
  }

  private cargar(): void {
    const d = this.desde();
    const h = this.hasta();
    if (!d || !h) return;
    this.loading.set(true);
    this.error.set(null);
    const t = this.tab();
    if (t === 'est') {
      this.reportes.getReparacionesPorEstado(d, h).subscribe({
        next: (x) => {
          this.est.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar reparaciones por estado.');
          this.loading.set(false);
        },
      });
    } else if (t === 'tec') {
      this.reportes.getRendimientoTecnico(d, h).subscribe({
        next: (x) => {
          this.tec.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar rendimiento por técnico.');
          this.loading.set(false);
        },
      });
    } else {
      this.reportes.getRepuestosTaller(d, h).subscribe({
        next: (x) => {
          this.rep.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar repuestos en taller.');
          this.loading.set(false);
        },
      });
    }
  }
}
