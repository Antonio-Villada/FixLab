import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ReparacionService } from '../../services/reparacion.service';
import { EquipoService } from '../../services/equipo.service';
import { AuthService } from '../../services/auth';
import {
  EquipoRespDTO,
  ReparacionEvidenciaReqDTO,
  ReparacionRespDTO,
  TIPOS_EVIDENCIA_REPARACION,
} from '../../models/reparacion.model';
import { ReparacionProgresoEstadoComponent } from '../reparacion-progreso-estado/reparacion-progreso-estado';

@Component({
  selector: 'app-reparaciones-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ReparacionProgresoEstadoComponent],
  templateUrl: './reparaciones-page.html',
  styleUrl: './reparaciones-page.css',
})
export class ReparacionesPageComponent implements OnInit {
  private reparacionService = inject(ReparacionService);
  private equipoService = inject(EquipoService);
  protected authService = inject(AuthService);

  readonly tiposEvidencia = TIPOS_EVIDENCIA_REPARACION;

  numeroTicket = '';
  lista = signal<ReparacionRespDTO[]>([]);
  /** Detalle completo (búsqueda o fila del listado). */
  detalleActivo = signal<ReparacionRespDTO | null>(null);
  equiposCliente = signal<EquipoRespDTO[]>([]);
  loadingLista = signal(false);
  loadingBusqueda = signal(false);
  loadingDetalle = signal(false);
  loadingEquipos = signal(false);
  loadingAprobar = signal(false);
  loadingEvidencia = signal(false);
  errorLista = signal<string | null>(null);
  errorBusqueda = signal<string | null>(null);
  errorDetalle = signal<string | null>(null);
  errorEquipos = signal<string | null>(null);
  accionMsg = signal<string | null>(null);
  accionErr = signal<string | null>(null);

  evidenciaUrl = '';
  evidenciaTipo: string = 'RECEPCION';
  evidenciaOrden: number | null = null;

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.cargarLista();
      if (!this.authService.isTallerStaff()) {
        this.cargarMisEquipos();
      }
    }
  }

  esCliente(): boolean {
    return this.authService.getRol() === 'CLIENTE';
  }

  puedeAprobarCotizacion(r: ReparacionRespDTO): boolean {
    return this.esCliente() && r.estado === 'COTIZADO_PENDIENTE_APROBACION';
  }

  cargarMisEquipos(): void {
    this.loadingEquipos.set(true);
    this.errorEquipos.set(null);
    this.equipoService.listar().subscribe({
      next: (data) => {
        this.equiposCliente.set(data ?? []);
        this.loadingEquipos.set(false);
      },
      error: (err) => {
        this.errorEquipos.set(err.error?.mensaje || 'No se pudieron cargar tus equipos');
        this.loadingEquipos.set(false);
      },
    });
  }

  cargarLista(): void {
    this.loadingLista.set(true);
    this.errorLista.set(null);
    this.reparacionService.listar().subscribe({
      next: (data) => {
        this.lista.set(data ?? []);
        this.loadingLista.set(false);
      },
      error: (err) => {
        this.errorLista.set(err.error?.mensaje || 'No se pudo cargar el listado');
        this.loadingLista.set(false);
      },
    });
  }

  buscarPorTicket(): void {
    const n = this.numeroTicket?.trim();
    if (!n) {
      this.errorBusqueda.set('Escribe un número de ticket (ej. FL-2026-00001)');
      return;
    }
    if (!this.authService.isLoggedIn()) {
      this.errorBusqueda.set('Inicia sesión para consultar el estado de tu reparación.');
      return;
    }
    this.loadingBusqueda.set(true);
    this.errorBusqueda.set(null);
    this.detalleActivo.set(null);
    this.accionMsg.set(null);
    this.accionErr.set(null);
    this.reparacionService.obtenerPorNumeroTicket(n).subscribe({
      next: (r) => {
        this.detalleActivo.set(r);
        this.loadingBusqueda.set(false);
      },
      error: (err) => {
        this.errorBusqueda.set(err.error?.mensaje || 'Ticket no encontrado o sin acceso');
        this.loadingBusqueda.set(false);
      },
    });
  }

  verDetalleDesdeLista(item: ReparacionRespDTO): void {
    this.loadingDetalle.set(true);
    this.errorDetalle.set(null);
    this.accionMsg.set(null);
    this.accionErr.set(null);
    this.reparacionService.obtenerPorId(item.id).subscribe({
      next: (r) => {
        this.detalleActivo.set(r);
        this.loadingDetalle.set(false);
      },
      error: (err) => {
        this.errorDetalle.set(err.error?.mensaje || 'No se pudo cargar el detalle');
        this.loadingDetalle.set(false);
      },
    });
  }

  cerrarDetalle(): void {
    this.detalleActivo.set(null);
    this.accionMsg.set(null);
    this.accionErr.set(null);
  }

  aprobarCotizacion(): void {
    const r = this.detalleActivo();
    if (!r || !this.puedeAprobarCotizacion(r)) return;
    this.loadingAprobar.set(true);
    this.accionErr.set(null);
    this.accionMsg.set(null);
    this.reparacionService.aprobarCotizacion(r.id).subscribe({
      next: (actualizado) => {
        this.detalleActivo.set(actualizado);
        this.loadingAprobar.set(false);
        this.accionMsg.set('Cotización aceptada. El taller puede continuar con la reparación.');
        this.cargarLista();
      },
      error: (err) => {
        this.loadingAprobar.set(false);
        this.accionErr.set(err.error?.mensaje || 'No se pudo aprobar la cotización');
      },
    });
  }

  enviarEvidencia(): void {
    const r = this.detalleActivo();
    if (!r) return;
    const url = this.evidenciaUrl?.trim();
    if (!url) {
      this.accionErr.set('Indica la URL de la evidencia (p. ej. enlace de Cloudinary).');
      return;
    }
    const dto: ReparacionEvidenciaReqDTO = {
      url,
      tipo: this.evidenciaTipo,
      orden: this.evidenciaOrden ?? undefined,
    };
    this.loadingEvidencia.set(true);
    this.accionErr.set(null);
    this.reparacionService.agregarEvidencia(r.id, dto).subscribe({
      next: (actualizado) => {
        this.detalleActivo.set(actualizado);
        this.loadingEvidencia.set(false);
        this.accionMsg.set('Evidencia registrada.');
        this.evidenciaUrl = '';
        this.evidenciaOrden = null;
      },
      error: (err) => {
        this.loadingEvidencia.set(false);
        this.accionErr.set(err.error?.mensaje || 'No se pudo registrar la evidencia');
      },
    });
  }

  /** Suma de subtotales de repuestos en la cotización (alineado con el cálculo del backend). */
  totalRepuestosCotizacion(r: ReparacionRespDTO): number {
    return (r.lineasProducto ?? []).reduce((s, ln) => s + (Number(ln.subtotal) || 0), 0);
  }

  /** Mano de obra = total cotizado menos repuestos (no puede ser negativa). */
  manoDeObraCotizacion(r: ReparacionRespDTO): number {
    if (r.cotizacionTotal == null) return 0;
    return Math.max(0, r.cotizacionTotal - this.totalRepuestosCotizacion(r));
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
