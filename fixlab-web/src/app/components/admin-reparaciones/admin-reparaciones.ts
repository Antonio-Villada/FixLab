import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { ReparacionService } from '../../services/reparacion.service';
import {
  ESTADOS_REPARACION_CAMBIO_TALLER,
  ReparacionRespDTO,
  TIPOS_EVIDENCIA_REPARACION,
} from '../../models/reparacion.model';
import { ReparacionProgresoEstadoComponent } from '../reparacion-progreso-estado/reparacion-progreso-estado';

/** Orden del flujo feliz (mismo criterio que la barra de progreso). Sirve para ocultar estados “ya pasados”. */
const PIPELINE_ORDER: readonly string[] = [
  'RECIBIDO',
  'EN_DIAGNOSTICO',
  'COTIZADO_PENDIENTE_APROBACION',
  'APROBADO',
  'EN_REPARACION',
  'EN_PRUEBAS',
  'LISTO_ENTREGA',
  'ENTREGADO',
];

/** Transiciones permitidas (alineado con {@code ReparacionServiceImpl}). */
const TRANSICIONES_REPARACION: Readonly<Record<string, readonly string[]>> = {
  RECIBIDO: ['EN_DIAGNOSTICO', 'CANCELADO'],
  EN_DIAGNOSTICO: ['COTIZADO_PENDIENTE_APROBACION', 'CANCELADO'],
  COTIZADO_PENDIENTE_APROBACION: ['APROBADO', 'EN_DIAGNOSTICO', 'CANCELADO'],
  APROBADO: ['EN_REPARACION', 'CANCELADO'],
  EN_REPARACION: ['EN_PRUEBAS', 'CANCELADO'],
  EN_PRUEBAS: ['LISTO_ENTREGA', 'EN_REPARACION'],
  LISTO_ENTREGA: ['ENTREGADO'],
  ENTREGADO: [],
  CANCELADO: [],
};

type OpcionEstadoTrabajo = { value: string; label: string; api: string };

@Component({
  selector: 'app-admin-reparaciones',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ReparacionProgresoEstadoComponent],
  templateUrl: './admin-reparaciones.html',
  styleUrl: './admin-reparaciones.css',
})
export class AdminReparacionesComponent implements OnInit {
  private reparacionService = inject(ReparacionService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly estadosDisponibles = ESTADOS_REPARACION_CAMBIO_TALLER;
  readonly tiposEvidencia = TIPOS_EVIDENCIA_REPARACION;

  private static readonly CATALOGO_ESTADOS_TRABAJO: OpcionEstadoTrabajo[] = [
    { value: 'RECIBIDO', label: 'RECIBIDO', api: 'RECIBIDO' },
    { value: 'DIAGNOSTICO_COTIZACION', label: 'DIAGNOSTICO - COTIZACION', api: 'EN_DIAGNOSTICO' },
    {
      value: 'COTIZADO_PENDIENTE_APROBACION',
      label: 'COTIZADO — pendiente aprobación cliente',
      api: 'COTIZADO_PENDIENTE_APROBACION',
    },
    { value: 'APROBADO', label: 'APROBADO', api: 'APROBADO' },
    { value: 'EN_REPARACION', label: 'EN_REPARACION', api: 'EN_REPARACION' },
    { value: 'EN_PRUEBAS', label: 'EN_PRUEBAS', api: 'EN_PRUEBAS' },
    { value: 'LISTO_ENTREGA', label: 'LISTO_ENTREGA', api: 'LISTO_ENTREGA' },
    { value: 'ENTREGADO', label: 'ENTREGADO', api: 'ENTREGADO' },
    { value: 'CANCELADO', label: 'CANCELADO', api: 'CANCELADO' },
  ];

  lista = signal<ReparacionRespDTO[]>([]);
  seleccion = signal<ReparacionRespDTO | null>(null);
  loadingLista = signal(true);
  loadingDetalle = signal(false);
  error = signal<string | null>(null);
  accionMsg = signal<string | null>(null);
  accionErr = signal<string | null>(null);
  busy = signal(false);

  estadoTrabajo = 'DIAGNOSTICO_COTIZACION';
  diagText = '';
  cotizacionStr = '';
  manoObraStr = '0';
  estadoNuevo = 'EN_DIAGNOSTICO';
  comentarioEstado = '';
  productoIdStr = '';
  productoCantStr = '1';
  evUrl = '';
  evTipo: string = 'DIAGNOSTICO';
  evOrden: number | null = null;

  /** Vista shell: búsqueda directa por código de ticket (admin y técnico). */
  ticketBuscar = '';
  buscandoTicket = signal(false);
  busquedaTicketErr = signal<string | null>(null);

  ngOnInit(): void {
    if (this.enShellTallerAdmin()) {
      this.route.queryParamMap.subscribe((params) => {
        const idStr = params.get('id');
        if (!idStr) {
          this.seleccion.set(null);
          return;
        }
        const id = Number(idStr);
        if (Number.isFinite(id) && id > 0) {
          this.verDetallePorId(id);
        }
      });
      return;
    }
    this.cargarLista();
  }

  enShellTallerAdmin(): boolean {
    return this.router.url.split('?')[0].includes('/admin/taller');
  }

  /** Opciones del combobox: estado actual + solo destinos “por delante” en el flujo (y cancelación si aplica). */
  estadosTrabajoOpcionesFiltradas(): Array<{ value: string; label: string }> {
    const sel = this.seleccion();
    if (!sel) {
      return AdminReparacionesComponent.CATALOGO_ESTADOS_TRABAJO.map(({ value, label }) => ({ value, label }));
    }
    const apiCur = sel.estado;
    const actualUi = this.opcionUiDesdeApi(apiCur);
    const alcanzables = this.alcanzablesDesde(apiCur);
    const destinosApi = [...alcanzables].filter((api) => {
      if (api === apiCur) return false;
      if (!this.esDestinoFelizValido(api, apiCur)) return false;
      if (!this.puedeAplicarseConCambiarEstado(api)) return false;
      return true;
    });
    const destinos = destinosApi
      .map((api) => this.opcionUiDesdeApi(api))
      .sort((a, b) => this.ordenPipelineOpcion(a.value, b.value));
    return [actualUi, ...destinos];
  }

  /** True si hay al menos un paso de API hasta el valor elegido en el combobox (shell: primer select). */
  hayCambioEstadoAplicable(): boolean {
    if (!this.habilitarCambioEstado()) return false;
    const sel = this.seleccion();
    if (!sel) return false;
    const hastaApi = this.mapUiTrabajoToApi(this.estadoTrabajo);
    const pasos = this.calcularPasosTransicion(sel.estado, hastaApi);
    return pasos !== null && pasos.length > 0;
  }

  private ordenPipelineOpcion(va: string, vb: string): number {
    const a = this.mapUiTrabajoToApi(va);
    const b = this.mapUiTrabajoToApi(vb);
    if (a === 'CANCELADO') return 1;
    if (b === 'CANCELADO') return -1;
    return PIPELINE_ORDER.indexOf(a) - PIPELINE_ORDER.indexOf(b);
  }

  private mapUiTrabajoToApi(ui: string): string {
    if (ui === 'DIAGNOSTICO_COTIZACION') return 'EN_DIAGNOSTICO';
    const hit = AdminReparacionesComponent.CATALOGO_ESTADOS_TRABAJO.find((c) => c.value === ui);
    return hit?.api ?? ui;
  }

  private opcionUiDesdeApi(api: string): { value: string; label: string } {
    if (api === 'EN_DIAGNOSTICO') {
      return { value: 'DIAGNOSTICO_COTIZACION', label: 'DIAGNOSTICO - COTIZACION' };
    }
    const c = AdminReparacionesComponent.CATALOGO_ESTADOS_TRABAJO.find((x) => x.api === api);
    if (c) return { value: c.value, label: c.label };
    return { value: api, label: api };
  }

  /** Estados alcanzables desde {@code desde} siguiendo el grafo de transiciones (incluye devolver a EN_REPARACION, etc.). */
  private alcanzablesDesde(desde: string): Set<string> {
    const vistos = new Set<string>();
    const cola = [desde];
    while (cola.length) {
      const v = cola.pop()!;
      if (vistos.has(v)) continue;
      vistos.add(v);
      for (const w of TRANSICIONES_REPARACION[v] ?? []) {
        if (!vistos.has(w)) cola.push(w);
      }
    }
    return vistos;
  }

  /** “Lo que falta”: solo índices mayores en el pipeline, o CANCELADO. Excluye vuelta a diagnóstico desde cotizado, etc. */
  private esDestinoFelizValido(apiDest: string, apiCurrent: string): boolean {
    if (apiDest === 'CANCELADO') return true;
    const i = PIPELINE_ORDER.indexOf(apiDest);
    const j = PIPELINE_ORDER.indexOf(apiCurrent);
    if (i === -1 || j === -1) return false;
    return i > j;
  }

  /** La API rechaza pasar a APROBADO por este endpoint (solo aprobación cliente). */
  private puedeAplicarseConCambiarEstado(apiDest: string): boolean {
    return apiDest !== 'APROBADO';
  }

  /**
   * Secuencia de estados API a aplicar con orden (uno por llamada), desde {@code desde} hasta {@code hasta}.
   * Camino más corto en el grafo; permite saltar varios pasos (p. ej. EN_REPARACION → ENTREGADO).
   */
  private calcularPasosTransicion(desde: string, hasta: string): string[] | null {
    if (desde === hasta) return [];
    const cola: string[] = [desde];
    const padre = new Map<string, string | null>([[desde, null]] as [string, string | null][]);
    while (cola.length) {
      const v = cola.shift()!;
      if (v === hasta) {
        const pasos: string[] = [];
        let cur: string | null = hasta;
        while (cur && cur !== desde) {
          pasos.unshift(cur);
          cur = padre.get(cur) ?? null;
        }
        return pasos;
      }
      for (const w of TRANSICIONES_REPARACION[v] ?? []) {
        if (!padre.has(w)) {
          padre.set(w, v);
          cola.push(w);
        }
      }
    }
    return null;
  }

  cargarLista(): void {
    this.loadingLista.set(true);
    this.error.set(null);
    this.reparacionService.listar().subscribe({
      next: (data) => {
        this.lista.set(data ?? []);
        this.loadingLista.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje || 'Error al cargar reparaciones');
        this.loadingLista.set(false);
      },
    });
  }

  private verDetallePorId(id: number): void {
    this.loadingDetalle.set(true);
    this.error.set(null);
    this.accionMsg.set(null);
    this.accionErr.set(null);
    this.seleccion.set(null);
    this.reparacionService.obtenerPorId(id).subscribe({
      next: (full) => {
        const aplicarDetalle = (data: ReparacionRespDTO) => {
          this.seleccion.set(data);
          this.busquedaTicketErr.set(null);
          this.sincronizarEstadoTrabajo(data.estado);
          this.onEstadoTrabajoChange();
          this.loadingDetalle.set(false);
        };
        // Tickets antiguos: quedaban en RECIBIDO aun con técnico; al recargar (p. ej. vuelta desde productos) se perdía el modo diagnóstico.
        if (full.estado === 'RECIBIDO' && full.tecnicoCedula) {
          this.reparacionService
            .cambiarEstado(id, {
              estadoNuevo: 'EN_DIAGNOSTICO',
              comentario: 'Paso a diagnóstico (ticket ya con técnico asignado)',
            })
            .subscribe({
              next: aplicarDetalle,
              error: (err) => {
                this.error.set(err.error?.mensaje || 'Error al cargar el detalle');
                this.loadingDetalle.set(false);
              },
            });
          return;
        }
        aplicarDetalle(full);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje || 'Error al cargar el detalle');
        this.loadingDetalle.set(false);
      },
    });
  }

  verDetalle(r: ReparacionRespDTO): void {
    this.verDetallePorId(r.id);
  }

  cerrarDetalle(): void {
    this.seleccion.set(null);
    this.accionMsg.set(null);
    this.accionErr.set(null);
    if (this.enShellTallerAdmin()) {
      this.router.navigate(['/admin/taller/gestion'], { replaceUrl: true });
    }
  }

  buscarTicketPorNumero(): void {
    if (!this.enShellTallerAdmin()) return;
    const raw = this.ticketBuscar.trim();
    this.busquedaTicketErr.set(null);
    if (!raw) {
      this.busquedaTicketErr.set('Escribe el número de ticket.');
      return;
    }
    this.buscandoTicket.set(true);
    this.reparacionService.obtenerPorNumeroTicket(raw).subscribe({
      next: (r) => {
        this.buscandoTicket.set(false);
        this.ticketBuscar = r.numeroTicket ?? raw;
        this.router.navigate(['/admin/taller/gestion'], {
          queryParams: { id: r.id },
          replaceUrl: true,
        });
      },
      error: (err) => {
        this.buscandoTicket.set(false);
        this.busquedaTicketErr.set(err.error?.mensaje || 'No se encontró un ticket con ese número.');
      },
    });
  }

  private idSel(): number | null {
    return this.seleccion()?.id ?? null;
  }

  private run(mutate: Observable<ReparacionRespDTO>): void {
    const id = this.idSel();
    if (id == null) return;
    this.busy.set(true);
    this.accionErr.set(null);
    this.accionMsg.set(null);
    mutate.subscribe({
      next: (r) => {
        this.seleccion.set(r);
        this.sincronizarEstadoTrabajo(r.estado);
        this.onEstadoTrabajoChange();
        this.busy.set(false);
        this.accionMsg.set('Guardado correctamente.');
        if (!this.enShellTallerAdmin()) {
          this.cargarLista();
        }
      },
      error: (err) => {
        this.busy.set(false);
        this.accionErr.set(err.error?.mensaje || this.mapValidation(err) || 'Error en la operación');
      },
    });
  }

  private mapValidation(err: unknown): string | null {
    const e = err as { error?: Record<string, string> };
    if (e?.error && typeof e.error === 'object' && !('mensaje' in e.error)) {
      const ent = Object.entries(e.error);
      if (ent.length) return ent.map(([k, v]) => `${k}: ${v}`).join(' ');
    }
    return null;
  }

  onEstadoTrabajoChange(): void {
    if (this.esEstadoDiagnosticoCotizacion()) {
      this.estadoNuevo = 'EN_DIAGNOSTICO';
      return;
    }
    if (this.estadoTrabajo === 'COTIZADO_PENDIENTE_APROBACION') {
      this.estadoNuevo = 'COTIZADO_PENDIENTE_APROBACION';
      return;
    }
    this.estadoNuevo = this.estadoTrabajo;
  }

  habilitarDiagnostico(): boolean {
    return this.esEstadoDiagnosticoCotizacion();
  }

  /** Permite aplicar transiciones vía API cuando el destino en el selector no es diagnóstico ni pendiente de cliente; el ticket aún no está cerrado. */
  habilitarCambioEstado(): boolean {
    if (this.esEstadoDiagnosticoCotizacion()) return false;
    const actual = this.seleccion()?.estado ?? '';
    if (['ENTREGADO', 'CANCELADO'].includes(actual)) return false;
    if (this.estadoTrabajo === 'COTIZADO_PENDIENTE_APROBACION') return false;
    return true;
  }

  habilitarRepuestos(): boolean {
    return ['EN_REPARACION', 'EN_PRUEBAS'].includes(this.estadoTrabajo);
  }

  habilitarEvidencia(): boolean {
    return !['ENTREGADO', 'CANCELADO'].includes(this.estadoTrabajo);
  }

  esEstadoDiagnosticoCotizacion(): boolean {
    return this.estadoTrabajo === 'DIAGNOSTICO_COTIZACION';
  }

  repuestosTotalActual(): number {
    const lineas = this.seleccion()?.lineasProducto ?? [];
    return lineas.reduce((acc, ln) => acc + Number(ln.subtotal || 0), 0);
  }

  manoObraValor(): number {
    const n = Number((this.manoObraStr || '0').replace(',', '.'));
    return Number.isFinite(n) && n > 0 ? n : 0;
  }

  cotizacionCalculada(): number {
    return this.repuestosTotalActual() + this.manoObraValor();
  }

  irAProductos(): void {
    const id = this.idSel();
    this.router.navigate(['/admin/productos'], {
      queryParams: {
        returnTo: '/admin/taller/gestion',
        reparacionId: id ?? undefined,
      },
    });
  }

  private sincronizarEstadoTrabajo(estadoActual: string): void {
    this.estadoTrabajo = estadoActual === 'EN_DIAGNOSTICO' ? 'DIAGNOSTICO_COTIZACION' : estadoActual;
  }

  registrarDiagnostico(): void {
    const id = this.idSel();
    if (id == null) return;
    const diagnostico = this.diagText.trim();
    if (!diagnostico) {
      this.accionErr.set('Debes escribir el diagnóstico.');
      return;
    }
    let cot: number | null = null;
    if (this.esEstadoDiagnosticoCotizacion()) {
      cot = this.cotizacionCalculada();
    } else if (this.cotizacionStr.trim()) {
      cot = Number(this.cotizacionStr.replace(',', '.'));
      if (Number.isNaN(cot)) {
        this.accionErr.set('Cotización no válida.');
        return;
      }
    }
    this.run(
      this.reparacionService.registrarDiagnostico(id, {
        diagnostico,
        cotizacionTotal: cot,
      }),
    );
  }

  cambiarEstado(): void {
    const id = this.idSel();
    if (id == null) return;
    const sel = this.seleccion();
    if (!sel) return;
    const hastaApi = this.enShellTallerAdmin()
      ? this.mapUiTrabajoToApi(this.estadoTrabajo)
      : (this.estadoNuevo?.trim() || '');
    if (!hastaApi) return;
    const pasos = this.calcularPasosTransicion(sel.estado, hastaApi);
    if (pasos === null) {
      this.accionErr.set('Transición no permitida.');
      return;
    }
    if (pasos.length === 0) return;
    if (pasos.length === 1) {
      this.run(
        this.reparacionService.cambiarEstado(id, {
          estadoNuevo: pasos[0],
          comentario: this.comentarioEstado.trim() || undefined,
        }),
      );
      return;
    }
    this.runCadenaEstados(id, pasos);
  }

  private runCadenaEstados(id: number, pasosApi: string[]): void {
    this.busy.set(true);
    this.accionErr.set(null);
    this.accionMsg.set(null);
    const comentario = this.comentarioEstado.trim() || undefined;

    const siguiente = (idx: number): void => {
      if (idx >= pasosApi.length) {
        this.busy.set(false);
        this.accionMsg.set('Guardado correctamente.');
        if (!this.enShellTallerAdmin()) {
          this.cargarLista();
        }
        return;
      }
      this.reparacionService
        .cambiarEstado(id, { estadoNuevo: pasosApi[idx], comentario })
        .subscribe({
          next: (r) => {
            this.seleccion.set(r);
            this.sincronizarEstadoTrabajo(r.estado);
            this.onEstadoTrabajoChange();
            siguiente(idx + 1);
          },
          error: (err) => {
            this.busy.set(false);
            this.accionErr.set(err.error?.mensaje || this.mapValidation(err) || 'Error en la operación');
          },
        });
    };
    siguiente(0);
  }

  agregarProducto(): void {
    const id = this.idSel();
    if (id == null) return;
    const pid = parseInt(this.productoIdStr.trim(), 10);
    const cant = parseInt(this.productoCantStr.trim(), 10);
    if (!Number.isFinite(pid) || pid <= 0) {
      this.accionErr.set('ID de producto no válido.');
      return;
    }
    if (!Number.isFinite(cant) || cant <= 0) {
      this.accionErr.set('Cantidad no válida.');
      return;
    }
    this.run(this.reparacionService.agregarProducto(id, { productoId: pid, cantidad: cant }));
  }

  agregarEvidencia(): void {
    const id = this.idSel();
    if (id == null) return;
    const url = this.evUrl.trim();
    if (!url) {
      this.accionErr.set('Indica la URL de la evidencia.');
      return;
    }
    this.run(
      this.reparacionService.agregarEvidencia(id, {
        url,
        tipo: this.evTipo,
        orden: this.evOrden ?? undefined,
      }),
    );
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
