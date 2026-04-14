import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PqrService } from '../../services/pqr.service';
import { AuthService } from '../../services/auth';
import {
  ESTADOS_PQR,
  EstadoSolicitudPqr,
  SolicitudPqrCambiarEstadoReqDTO,
  SolicitudPqrRespDTO,
  SolicitudPqrValidacionGarantiaReqDTO,
  TIPOS_PQR,
} from '../../models/pqr.model';

@Component({
  selector: 'app-admin-postventa',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-postventa.html',
  styleUrl: './admin-postventa.css',
})
export class AdminPostventaComponent implements OnInit {
  private pqrService = inject(PqrService);
  protected authService = inject(AuthService);

  readonly tipos = TIPOS_PQR;
  readonly estados = ESTADOS_PQR;

  lista = signal<SolicitudPqrRespDTO[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);

  /** Borrador de estado por id de solicitud */
  draftEstado: Record<number, EstadoSolicitudPqr> = {};
  mensajeCliente: Record<number, string> = {};
  notasInternas: Record<number, string> = {};
  valNotas: Record<number, string> = {};
  valSi: Record<number, boolean> = {};

  savingEstado = signal<number | null>(null);
  savingVal = signal<number | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.pqrService.listarGestion().subscribe({
      next: (data) => {
        this.lista.set(data ?? []);
        this.draftEstado = {};
        this.mensajeCliente = {};
        this.notasInternas = {};
        this.valNotas = {};
        this.valSi = {};
        for (const s of data ?? []) {
          this.draftEstado[s.id] = s.estado;
          this.valSi[s.id] = s.garantiaFisicaValidada;
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje || 'No se pudo cargar el listado');
        this.loading.set(false);
      },
    });
  }

  labelTipo(codigo: string): string {
    return this.tipos.find((t) => t.value === codigo)?.label ?? codigo;
  }

  labelEstado(codigo: string): string {
    return this.estados.find((e) => e.value === codigo)?.label ?? codigo;
  }

  puedeCambiarEstado(): boolean {
    return this.authService.isAdmin();
  }

  puedeValidarGarantia(): boolean {
    return this.authService.isAdmin() || this.authService.isTecnico();
  }

  aplicarEstado(s: SolicitudPqrRespDTO): void {
    const nuevo = this.draftEstado[s.id];
    if (!nuevo || nuevo === s.estado) {
      return;
    }
    const dto: SolicitudPqrCambiarEstadoReqDTO = {
      nuevoEstado: nuevo,
      mensajeParaCliente: this.mensajeCliente[s.id]?.trim() || null,
      notasInternas: this.notasInternas[s.id]?.trim() || null,
    };
    this.savingEstado.set(s.id);
    this.pqrService.cambiarEstado(s.id, dto).subscribe({
      next: () => {
        this.savingEstado.set(null);
        this.mensajeCliente[s.id] = '';
        this.notasInternas[s.id] = '';
        this.cargar();
      },
      error: (err) => {
        this.error.set(err.error?.mensaje || 'No se pudo actualizar el estado');
        this.savingEstado.set(null);
      },
    });
  }

  aplicarValidacion(s: SolicitudPqrRespDTO): void {
    const dto: SolicitudPqrValidacionGarantiaReqDTO = {
      garantiaFisicaValidada: !!this.valSi[s.id],
      notas: this.valNotas[s.id]?.trim() || null,
    };
    this.savingVal.set(s.id);
    this.pqrService.validacionGarantiaFisica(s.id, dto).subscribe({
      next: () => {
        this.savingVal.set(null);
        this.valNotas[s.id] = '';
        this.cargar();
      },
      error: (err) => {
        this.error.set(err.error?.mensaje || 'No se pudo registrar la validación');
        this.savingVal.set(null);
      },
    });
  }
}
