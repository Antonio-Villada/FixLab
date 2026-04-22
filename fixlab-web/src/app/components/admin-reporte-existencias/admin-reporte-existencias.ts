import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportesService } from '../../services/reportes.service';
import { ExistenciasLinea, ExistenciasReporte } from '../../models/existencias-report.model';

@Component({
  selector: 'app-admin-reporte-existencias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reporte-existencias.html',
  styleUrl: './admin-reporte-existencias.css',
})
export class AdminReporteExistenciasComponent implements OnInit {
  private reportesService = inject(ReportesService);

  reporte = signal<ExistenciasReporte | null>(null);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  csvDownloading = signal(false);
  filtroTexto = signal('');

  lineasFiltradas = computed(() => {
    const rep = this.reporte();
    if (!rep?.lineas?.length) return [];
    const t = this.filtroTexto().trim().toLowerCase();
    if (!t) return rep.lineas;
    return rep.lineas.filter((l) => {
      const blob = [l.sku, l.nombre, l.categoriaNombre, l.tipoProductoNombre, l.estadoExistencia]
        .join(' ')
        .toLowerCase();
      return blob.includes(t);
    });
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.reportesService.getReporteExistencias().subscribe({
      next: (data) => {
        this.reporte.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'No se pudo cargar el reporte de existencias.');
        this.loading.set(false);
      },
    });
  }

  descargarCsv(): void {
    this.csvDownloading.set(true);
    this.errorMessage.set(null);
    this.reportesService.descargarExistenciasCsv().subscribe({
      next: (blob) => {
        this.csvDownloading.set(false);
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `existencias_${new Date().toISOString().slice(0, 10)}.csv`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.csvDownloading.set(false);
        this.errorMessage.set('No se pudo descargar el CSV. ¿Sesión de administrador activa?');
      },
    });
  }

  imprimir(): void {
    window.print();
  }

  filaClase(linea: ExistenciasLinea): Record<string, boolean> {
    return {
      'table-warning': linea.stockBajo === true,
      'table-secondary': linea.activo === false,
    };
  }
}
