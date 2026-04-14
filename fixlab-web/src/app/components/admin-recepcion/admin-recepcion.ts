import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap, tap } from 'rxjs/operators';
import { EquipoService } from '../../services/equipo.service';
import { ReparacionService } from '../../services/reparacion.service';
import { AuthService } from '../../services/auth';
import { UsuarioService } from '../../services/usuario.service';
import { ClienteSugerenciaRespDTO, RolUsuario, StaffTallerAsignableRespDTO } from '../../models/auth.model';
import { EquipoReqDTO, ReparacionRespDTO, TallerRespDTO, TipoEquipoRespDTO } from '../../models/reparacion.model';

@Component({
  selector: 'app-admin-recepcion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-recepcion.html',
  styleUrl: './admin-recepcion.css',
})
export class AdminRecepcionComponent implements OnInit {
  private fb = inject(FormBuilder);
  private equipoService = inject(EquipoService);
  private reparacionService = inject(ReparacionService);
  private usuarioService = inject(UsuarioService);
  private destroyRef = inject(DestroyRef);
  protected authService = inject(AuthService);
  private router = inject(Router);

  tiposEquipo = signal<TipoEquipoRespDTO[]>([]);
  talleres = signal<TallerRespDTO[]>([]);
  tecnicosAsignables = signal<StaffTallerAsignableRespDTO[]>([]);
  cargandoCatalogo = signal(true);
  guardando = signal(false);
  errorCatalogo = signal<string | null>(null);
  errorEnvio = signal<string | null>(null);
  exito = signal<ReparacionRespDTO | null>(null);

  sugerenciasClientes = signal<ClienteSugerenciaRespDTO[]>([]);
  panelSugerenciasAbierto = signal(false);
  /** Nombre mostrado cuando la cédula está resuelta (lista o validación). */
  nombreClienteMostrar = signal<string | null>(null);
  /** Texto fijo del único taller de recepción (sin selector). */
  tallerRecepcionEtiqueta = signal<string | null>(null);
  private cedulaResuelta: string | null = null;

  form = this.fb.nonNullable.group({
    propietarioCedula: ['', [Validators.required, Validators.maxLength(20)]],
    tipoEquipoId: [null as number | null, Validators.required],
    marca: [''],
    numeroSerie: [''],
    observaciones: [''],
    tallerId: [null as number | null, Validators.required],
    descripcionFalla: ['', [Validators.required, Validators.maxLength(4000)]],
    tecnicoCedula: [null as string | null, Validators.required],
  });

  ngOnInit(): void {
    this.errorCatalogo.set(null);
    forkJoin({
      tipos: this.reparacionService.listarTiposEquipo(),
      talleres: this.reparacionService.listarTalleres(),
      staff: this.usuarioService.listarStaffAsignableTaller().pipe(catchError(() => of([]))),
    }).subscribe({
      next: ({ tipos, talleres, staff }) => {
        this.tiposEquipo.set(tipos ?? []);
        const listTalleres = talleres ?? [];
        this.talleres.set(listTalleres);
        const tallerDefecto = this.idTallerPorDefecto(listTalleres);
        if (tallerDefecto != null) {
          const t = listTalleres.find((x) => x.id === tallerDefecto);
          this.tallerRecepcionEtiqueta.set(
            t ? `${t.nombre} (${t.tipoTallerNombre})` : null,
          );
          this.form.patchValue({ tallerId: tallerDefecto });
        } else {
          this.tallerRecepcionEtiqueta.set(null);
        }
        this.tecnicosAsignables.set(staff ?? []);
        this.cargandoCatalogo.set(false);
        const avisos: string[] = [];
        if (!talleres?.length) {
          avisos.push('No hay talleres configurados. Revisa el API (DataInitializer) o la base de datos.');
        }
        if (!tipos?.length) {
          avisos.push('No hay tipos de equipo en catálogo.');
        }
        if (!staff?.length) {
          avisos.push('No hay técnicos o administradores disponibles para asignar. Registra personal con rol Técnico o Admin.');
        }
        if (avisos.length) {
          this.errorCatalogo.set(avisos.join(' '));
        }
      },
      error: (err) => {
        this.errorCatalogo.set(this.extraerMensaje(err) || 'No se pudo cargar el catálogo');
        this.cargandoCatalogo.set(false);
      },
    });

    this.form.controls.propietarioCedula.valueChanges
      .pipe(
        debounceTime(280),
        distinctUntilChanged(),
        tap((raw) => {
          const v = (raw ?? '').trim();
          if (v !== this.cedulaResuelta) {
            this.nombreClienteMostrar.set(null);
            this.cedulaResuelta = null;
          }
          if (v.length < 2) {
            this.sugerenciasClientes.set([]);
            this.panelSugerenciasAbierto.set(false);
          }
        }),
        switchMap((raw) => {
          const v = (raw ?? '').trim();
          if (v.length < 2) {
            return of([] as ClienteSugerenciaRespDTO[]);
          }
          return this.usuarioService.buscarSugerenciasClientes(v).pipe(catchError(() => of([])));
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((list) => {
        this.sugerenciasClientes.set(list);
        this.panelSugerenciasAbierto.set(list.length > 0);
      });
  }

  onCedulaFocus(): void {
    const list = this.sugerenciasClientes();
    if (list.length > 0) {
      this.panelSugerenciasAbierto.set(true);
    }
  }

  /** Al salir del campo: intentar resolver nombre si escribió la cédula completa sin elegir de la lista. */
  onCedulaBlur(): void {
    setTimeout(() => {
      this.panelSugerenciasAbierto.set(false);
      const c = this.form.controls.propietarioCedula.value?.trim();
      if (!c) {
        return;
      }
      if (this.cedulaResuelta === c && this.nombreClienteMostrar()) {
        return;
      }
      this.usuarioService.getByCedula(c).subscribe({
        next: (u) => {
          if (u.rol === RolUsuario.CLIENTE) {
            this.nombreClienteMostrar.set(`${u.nombre} ${u.apellido}`.trim());
            this.cedulaResuelta = c;
          }
        },
        error: () => {
          /* cédula inexistente o sin permiso: el envío del formulario mostrará el error del API */
        },
      });
    }, 200);
  }

  seleccionarSugerencia(s: ClienteSugerenciaRespDTO): void {
    this.form.controls.propietarioCedula.setValue(s.cedula, { emitEvent: false });
    this.nombreClienteMostrar.set(`${s.nombre} ${s.apellido}`.trim());
    this.cedulaResuelta = s.cedula;
    this.sugerenciasClientes.set([]);
    this.panelSugerenciasAbierto.set(false);
  }

  enviar(): void {
    this.errorEnvio.set(null);
    this.exito.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const equipoDto: EquipoReqDTO = {
      tipoEquipoId: v.tipoEquipoId as number,
      propietarioCedula: v.propietarioCedula.trim(),
      marca: v.marca.trim() || undefined,
      numeroSerie: v.numeroSerie.trim() || undefined,
      observaciones: v.observaciones.trim() || undefined,
    };

    const tecnicoCed = String(v.tecnicoCedula ?? '').trim();

    this.guardando.set(true);
    this.equipoService
      .crear(equipoDto)
      .pipe(
        switchMap((equipo) =>
          this.reparacionService.crear({
            equipoId: equipo.id,
            tallerId: v.tallerId as number,
            descripcionFalla: v.descripcionFalla.trim(),
          }),
        ),
        switchMap((rep) => this.reparacionService.asignarTecnico(rep.id, { tecnicoCedula: tecnicoCed })),
      )
      .subscribe({
        next: (rep) => {
          this.exito.set(rep);
          this.guardando.set(false);
          this.limpiarFormularioRecepcion();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorEnvio.set(this.extraerMensaje(err));
        },
      });
  }

  /** Prioriza el taller llamado «Taller principal»; si no existe, el primero de la lista. */
  private idTallerPorDefecto(talleres: TallerRespDTO[]): number | null {
    if (!talleres.length) {
      return null;
    }
    const norm = (n: string) => n.trim().toLowerCase();
    const exact = talleres.find((t) => norm(t.nombre) === 'taller principal');
    if (exact) {
      return exact.id;
    }
    const partial = talleres.find((t) => /taller\s+principal/i.test(t.nombre));
    return (partial ?? talleres[0]).id;
  }

  private limpiarFormularioRecepcion(): void {
    const tallerDefecto = this.idTallerPorDefecto(this.talleres());
    this.form.reset({
      propietarioCedula: '',
      tipoEquipoId: null,
      marca: '',
      numeroSerie: '',
      observaciones: '',
      tallerId: tallerDefecto,
      descripcionFalla: '',
      tecnicoCedula: null,
    });
    this.nombreClienteMostrar.set(null);
    this.cedulaResuelta = null;
    this.sugerenciasClientes.set([]);
    this.panelSugerenciasAbierto.set(false);
  }

  limpiarManual(): void {
    this.limpiarFormularioRecepcion();
  }

  /** Vista embebida en el shell Taller del admin (evita duplicar enlaces). */
  enShellTallerAdmin(): boolean {
    return this.router.url.split('?')[0].includes('/admin/taller');
  }

  etiquetaTecnicoOpcion(t: StaffTallerAsignableRespDTO): string {
    const rol = t.rol === RolUsuario.ADMIN ? 'Administrador' : 'Técnico';
    return `${t.apellido}, ${t.nombre} (${rol}) · ${t.cedula}`;
  }

  private extraerMensaje(err: unknown): string {
    const e = err as {
      error?: string | { mensaje?: string; message?: string } | Record<string, string>;
      message?: string;
    };
    const body = e?.error;
    if (typeof body === 'string' && body.trim()) {
      return body.trim();
    }
    if (body && typeof body === 'object' && 'mensaje' in body && typeof body.mensaje === 'string') {
      return body.mensaje;
    }
    if (body && typeof body === 'object' && 'message' in body && typeof (body as { message?: string }).message === 'string') {
      return (body as { message: string }).message;
    }
    if (body && typeof body === 'object' && !('mensaje' in body)) {
      const campos = Object.entries(body as Record<string, string>);
      if (campos.length) {
        return campos.map(([k, v]) => `${k}: ${v}`).join('. ');
      }
    }
    return e?.message || 'No se pudo completar la recepción';
  }
}
