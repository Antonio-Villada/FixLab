import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportesService } from '../../services/reportes.service';
import {
  ClientesComprasReporte,
  UsuariosPorRolReporte,
  FinancieroSnapshotReporte,
} from '../../models/informes-variados.model';

type TabA = 'cli' | 'usr' | 'fin';

@Component({
  selector: 'app-admin-reporte-administracion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reporte-administracion.html',
  styleUrl: './admin-reporte-administracion.css',
})
export class AdminReporteAdministracionComponent implements OnInit {
  private reportes = inject(ReportesService);

  tab = signal<TabA>('cli');
  desde = signal('');
  hasta = signal('');
  loading = signal(false);
  error = signal<string | null>(null);

  cli = signal<ClientesComprasReporte | null>(null);
  usr = signal<UsuariosPorRolReporte | null>(null);
  fin = signal<FinancieroSnapshotReporte | null>(null);

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

  setTab(t: TabA): void {
    this.tab.set(t);
    this.cargar();
  }

  actualizar(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    const t = this.tab();
    if (t === 'usr') {
      this.reportes.getUsuariosPorRol().subscribe({
        next: (x) => {
          this.usr.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar usuarios por rol.');
          this.loading.set(false);
        },
      });
      return;
    }
    const d = this.desde();
    const h = this.hasta();
    if (!d || !h) return;
    if (t === 'cli') {
      this.reportes.getClientesCompras(d, h).subscribe({
        next: (x) => {
          this.cli.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar clientes con compras.');
          this.loading.set(false);
        },
      });
    } else {
      this.reportes.getFinancieroSnapshot(d, h).subscribe({
        next: (x) => {
          this.fin.set(x);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar snapshot financiero.');
          this.loading.set(false);
        },
      });
    }
  }
}
