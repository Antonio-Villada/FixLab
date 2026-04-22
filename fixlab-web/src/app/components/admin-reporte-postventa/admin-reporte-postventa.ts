import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportesService } from '../../services/reportes.service';
import { PqrsResumenReporte, GarantiasServicioReporte } from '../../models/informes-variados.model';

type TabP = 'pqrs' | 'gar';

@Component({
  selector: 'app-admin-reporte-postventa',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reporte-postventa.html',
  styleUrl: './admin-reporte-postventa.css',
})
export class AdminReportePostventaComponent implements OnInit {
  private reportes = inject(ReportesService);

  tab = signal<TabP>('pqrs');
  desde = signal('');
  hasta = signal('');
  diasGarantia = signal(45);
  loading = signal(false);
  error = signal<string | null>(null);

  pqrs = signal<PqrsResumenReporte | null>(null);
  gar = signal<GarantiasServicioReporte | null>(null);

  ngOnInit(): void {
    const r = this.defaultRango();
    this.desde.set(r.desde);
    this.hasta.set(r.hasta);
    this.cargar();
  }

  private defaultRango(): { desde: string; hasta: string } {
    const hasta = new Date();
    const desde = new Date();
    desde.setDate(desde.getDate() - 90);
    return { desde: desde.toISOString().slice(0, 10), hasta: hasta.toISOString().slice(0, 10) };
  }

  setTab(t: TabP): void {
    this.tab.set(t);
    this.cargar();
  }

  actualizar(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    if (this.tab() === 'pqrs') {
      const d = this.desde();
      const h = this.hasta();
      if (!d || !h) return;
      this.reportes.getPqrsResumen(d, h).subscribe({
        next: (x) => {
          this.pqrs.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el resumen PQRS.');
          this.loading.set(false);
        },
      });
    } else {
      this.reportes.getGarantiasServicio(this.diasGarantia()).subscribe({
        next: (x) => {
          this.gar.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar garantías de servicio.');
          this.loading.set(false);
        },
      });
    }
  }
}
