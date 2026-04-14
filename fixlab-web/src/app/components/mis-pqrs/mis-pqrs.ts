import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PqrService } from '../../services/pqr.service';
import { AuthService } from '../../services/auth';
import {
  ESTADOS_PQR,
  OrigenDocumentoPqr,
  SolicitudPqrCreateReqDTO,
  SolicitudPqrRespDTO,
  TIPOS_PQR,
  TipoSolicitudPqr,
} from '../../models/pqr.model';

@Component({
  selector: 'app-mis-pqrs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './mis-pqrs.html',
  styleUrl: './mis-pqrs.css',
})
export class MisPqrsComponent implements OnInit {
  private pqrService = inject(PqrService);
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly tipos = TIPOS_PQR;
  readonly estadosInfo = ESTADOS_PQR;

  lista = signal<SolicitudPqrRespDTO[]>([]);
  loadingLista = signal(false);
  errorLista = signal<string | null>(null);

  tipo: TipoSolicitudPqr = 'PETICION';
  origenDocumento: OrigenDocumentoPqr = 'FACTURA_PEDIDO';
  pedidoId: number | null = null;
  reparacionId: number | null = null;
  descripcion = '';
  evidenciasUrls = signal<string[]>([]);
  consentimiento = false;

  enviando = signal(false);
  errorForm = signal<string | null>(null);
  exitoMsg = signal<string | null>(null);
  subiendoArchivos = signal(false);
  errorUpload = signal<string | null>(null);

  ngOnInit(): void {
    if (!this.authService.isLoggedIn() || !this.authService.isCliente()) {
      this.router.navigate(['/login']);
      return;
    }
    this.cargarLista();
  }

  labelEstado(codigo: string): string {
    return this.estadosInfo.find((e) => e.value === codigo)?.label ?? codigo;
  }

  labelTipo(codigo: string): string {
    return this.tipos.find((t) => t.value === codigo)?.label ?? codigo;
  }

  cargarLista(): void {
    this.loadingLista.set(true);
    this.errorLista.set(null);
    this.pqrService.misSolicitudes().subscribe({
      next: (data) => {
        this.lista.set(data ?? []);
        this.loadingLista.set(false);
      },
      error: (err) => {
        this.errorLista.set(err.error?.mensaje || 'No se pudieron cargar tus solicitudes');
        this.loadingLista.set(false);
      },
    });
  }

  onArchivosSeleccionados(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const files = input.files;
    if (!files?.length) return;
    this.errorUpload.set(null);
    this.subiendoArchivos.set(true);
    const nuevas: string[] = [...this.evidenciasUrls()];
    let i = 0;
    const siguiente = () => {
      if (i >= files.length) {
        this.evidenciasUrls.set(nuevas);
        this.subiendoArchivos.set(false);
        input.value = '';
        return;
      }
      this.pqrService.uploadEvidencia(files[i]).subscribe({
        next: (r) => {
          if (r?.url) nuevas.push(r.url);
          i++;
          siguiente();
        },
        error: (err) => {
          this.errorUpload.set(err.error?.mensaje || 'Error al subir un archivo');
          this.subiendoArchivos.set(false);
          input.value = '';
        },
      });
    };
    siguiente();
  }

  quitarEvidencia(url: string): void {
    this.evidenciasUrls.set(this.evidenciasUrls().filter((u) => u !== url));
  }

  radicar(): void {
    this.errorForm.set(null);
    this.exitoMsg.set(null);
    if (!this.descripcion.trim()) {
      this.errorForm.set('Describe tu solicitud');
      return;
    }
    if (!this.consentimiento) {
      this.errorForm.set('Debes autorizar el tratamiento de datos personales');
      return;
    }
    const dto: SolicitudPqrCreateReqDTO = {
      tipo: this.tipo,
      origenDocumento: this.origenDocumento,
      descripcion: this.descripcion.trim(),
      evidenciasUrls: this.evidenciasUrls(),
      consentimientoTratamientoDatos: true,
    };
    if (this.origenDocumento === 'FACTURA_PEDIDO') {
      dto.pedidoId = this.pedidoId ?? undefined;
      dto.reparacionId = null;
    } else {
      dto.reparacionId = this.reparacionId ?? undefined;
      dto.pedidoId = null;
    }
    this.enviando.set(true);
    this.pqrService.crear(dto).subscribe({
      next: (creado) => {
        this.exitoMsg.set(`Radicado exitoso: ${creado.radicado}. Recibirás actualizaciones por correo.`);
        this.descripcion = '';
        this.pedidoId = null;
        this.reparacionId = null;
        this.evidenciasUrls.set([]);
        this.consentimiento = false;
        this.enviando.set(false);
        this.cargarLista();
      },
      error: (err) => {
        const msg =
          typeof err.error === 'object' && err.error && !err.error.mensaje
            ? JSON.stringify(err.error)
            : err.error?.mensaje || 'No se pudo radicar';
        this.errorForm.set(msg);
        this.enviando.set(false);
      },
    });
  }
}
