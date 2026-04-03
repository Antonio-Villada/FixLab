import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ReparacionService } from '../../services/reparacion.service';
import { ReparacionRespDTO } from '../../models/reparacion.model';
import { ReparacionProgresoEstadoComponent } from '../reparacion-progreso-estado/reparacion-progreso-estado';

@Component({
  selector: 'app-admin-taller-lista',
  standalone: true,
  imports: [CommonModule, RouterModule, ReparacionProgresoEstadoComponent],
  templateUrl: './admin-taller-lista.html',
  styleUrl: './admin-taller-lista.css',
})
export class AdminTallerListaComponent implements OnInit {
  private reparacionService = inject(ReparacionService);
  private router = inject(Router);

  lista = signal<ReparacionRespDTO[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.reparacionService.listar().subscribe({
      next: (data) => {
        this.lista.set(data ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje || 'Error al cargar la lista del taller');
        this.loading.set(false);
      },
    });
  }

  abrirEnGestion(id: number): void {
    this.router.navigate(['/admin/taller/gestion'], { queryParams: { id } });
  }

  badgeClass(estado: string): string {
    const e = (estado || '').toUpperCase();
    if (e.includes('ENTREGADO')) return 'bg-secondary';
    if (e.includes('CANCELADO')) return 'bg-dark';
    if (e.includes('LISTO') || e.includes('PRUEBAS')) return 'bg-info text-dark';
    if (e.includes('REPARACION') || e.includes('APROBADO')) return 'bg-primary';
    if (e.includes('COTIZADO') || e.includes('DIAGNOSTICO')) return 'bg-warning text-dark';
    return 'bg-success';
  }
}
