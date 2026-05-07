import { Component, OnInit, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { UsuarioService } from '../../services/usuario.service';
import { CheckoutService } from '../../services/checkout.service';
import { AuthService } from '../../services/auth';
import { DetallePedidoRespDTO, PedidoRespDTO } from '../../models/checkout.model';
import { environment } from '../../../environments/environment';

function confirmNewPasswordMatch(group: AbstractControl): ValidationErrors | null {
  const nueva = group.get('nuevaPassword')?.value;
  const confirm = group.get('confirmarNuevaPassword')?.value;
  if (!nueva) return null;
  return nueva === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  public usuarioService = inject(UsuarioService);
  private checkoutService = inject(CheckoutService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  readonly enablePostventaModule = environment.enablePostventaModule;

  pedidos = signal<PedidoRespDTO[]>([]);
  loadingPerfil = signal(true);
  loadingPedidos = signal(true);
  errorPerfil = signal<string | null>(null);
  errorPedidos = signal<string | null>(null);
  editing = signal(false);
  saving = signal(false);
  uploadingFoto = signal(false);
  /** Vista previa de la foto (archivo seleccionado antes de subir). */
  fotoPreviewUrl = signal<string | null>(null);
  @ViewChild('fotoPerfilInput') fotoPerfilInputRef: ElementRef<HTMLInputElement> | null = null;

  formPerfil: FormGroup = this.fb.group(
    {
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      apellido: ['', [Validators.required, Validators.maxLength(100)]],
      telefono: ['', [Validators.maxLength(20)]],
      contraseñaActual: [''],
      nuevaPassword: [
        '',
        [
          Validators.minLength(8),
          Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/),
        ],
      ],
      confirmarNuevaPassword: [''],
    },
    { validators: confirmNewPasswordMatch }
  );

  readonly showEliminarCuenta = signal(false);
  readonly deletingCuenta = signal(false);
  readonly errorEliminarCuenta = signal<string | null>(null);

  formEliminarCuenta: FormGroup = this.fb.group({
    passwordEliminar: ['', [Validators.required]],
    confirmoEliminacion: [false, [Validators.requiredTrue]],
  });

  ngOnInit(): void {
    if (this.usuarioService.currentUser()) {
      this.loadingPerfil.set(false);
    } else {
      this.usuarioService.loadCurrentUser().subscribe({
        next: () => this.loadingPerfil.set(false),
        error: (err) => {
          this.errorPerfil.set(err.error?.mensaje || 'Error al cargar tu perfil');
          this.loadingPerfil.set(false);
        },
      });
    }

    if (this.authService.isCliente()) {
      this.checkoutService.getMisPedidos().subscribe({
        next: (data) => {
          this.pedidos.set(data || []);
          this.loadingPedidos.set(false);
        },
        error: (err) => {
          this.errorPedidos.set(err.error?.mensaje || 'Error al cargar tus compras');
          this.loadingPedidos.set(false);
        },
      });
    } else {
      this.loadingPedidos.set(false);
    }
  }

  isCliente(): boolean {
    return this.authService.isCliente();
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '-';
    const d = new Date(fecha);
    return d.toLocaleDateString('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  /** Clases Bootstrap legacy (factura u otros); el panel usa estilos suaves vía estadoVisualClass. */
  estadoBadgeClass(estado: string): string {
    if (!estado) return 'bg-secondary';
    const e = estado.toUpperCase();
    if (e === 'PAGADO') return 'bg-success';
    if (e === 'ENVIADO' || e === 'ENTREGADO') return 'bg-info';
    if (e === 'CANCELADO') return 'bg-danger';
    return 'bg-secondary';
  }

  /** Badge de estado con paleta contenida (SCSS `.dashboard-estado-badge.*`). */
  estadoVisualClass(estado: string | undefined | null): string {
    if (!estado) return 'estado-muted';
    const e = estado.toUpperCase();
    if (e === 'PAGADO') return 'estado-ok';
    if (e === 'ENVIADO' || e === 'ENTREGADO') return 'estado-info';
    if (e === 'CANCELADO') return 'estado-danger';
    return 'estado-muted';
  }

  primerDetalle(ped: PedidoRespDTO): DetallePedidoRespDTO | undefined {
    return ped.detalles?.[0];
  }

  pedidoTituloPrincipal(ped: PedidoRespDTO): string {
    const d = this.primerDetalle(ped);
    return d?.nombreProducto?.trim() || `Pedido #${ped.id}`;
  }

  pedidoMasItemsCount(ped: PedidoRespDTO): number {
    const n = ped.detalles?.length ?? 0;
    return n > 1 ? n - 1 : 0;
  }

  thumbHue(productoId: number | undefined): number {
    return Math.abs(((productoId ?? 0) * 47) % 360);
  }

  inicialesUsuario(nombre?: string | null, apellido?: string | null): string {
    const a = (nombre?.trim().charAt(0) || '').toUpperCase();
    const b = (apellido?.trim().charAt(0) || '').toUpperCase();
    return (a + b) || '?';
  }

  startEdit(): void {
    const p = this.usuarioService.currentUser();
    if (p) {
      this.formPerfil.patchValue({
        nombre: p.nombre ?? '',
        apellido: p.apellido ?? '',
        telefono: p.telefono ?? '',
        contraseñaActual: '',
        nuevaPassword: '',
        confirmarNuevaPassword: '',
      });
      this.fotoPreviewUrl.set(null);
      this.editing.set(true);
      this.errorPerfil.set(null);
    }
  }

  cancelEdit(): void {
    this.editing.set(false);
    this.clearFotoPreview();
  }

  savePerfil(): void {
    const value = this.formPerfil.getRawValue();
    const cambiarPass = value.nuevaPassword?.trim();
    if (cambiarPass) {
      if (!value.contraseñaActual?.trim()) {
        this.errorPerfil.set('Ingresa tu contraseña actual para cambiar la contraseña.');
        this.formPerfil.get('contraseñaActual')?.markAsTouched();
        return;
      }
      if (this.formPerfil.errors?.['passwordMismatch']) {
        this.errorPerfil.set('La nueva contraseña y la confirmación no coinciden.');
        this.formPerfil.get('confirmarNuevaPassword')?.markAsTouched();
        return;
      }
    }
    if (this.formPerfil.invalid) {
      this.formPerfil.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorPerfil.set(null);
    const doUpdate = () => {
      const dto = {
        nombre: value.nombre?.trim() ?? '',
        apellido: value.apellido?.trim() ?? '',
        telefono: value.telefono?.trim() ?? '',
        fotoUrl: this.usuarioService.currentUser()?.fotoUrl ?? null,
      };
      this.usuarioService.updateMe(dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.editing.set(false);
        },
        error: (err) => {
          this.errorPerfil.set(err.error?.mensaje || 'Error al guardar. Intenta de nuevo.');
          this.saving.set(false);
        },
      });
    };
    if (cambiarPass) {
      this.authService.cambiarPassword(value.contraseñaActual!.trim(), cambiarPass).subscribe({
        next: () => doUpdate(),
        error: (err) => {
          this.errorPerfil.set(err.error?.mensaje || 'Error al cambiar contraseña.');
          this.saving.set(false);
        },
      });
    } else {
      doUpdate();
    }
  }

  onFotoPerfilChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !file.type.startsWith('image/')) return;
    if (this.fotoPreviewUrl()) URL.revokeObjectURL(this.fotoPreviewUrl()!);
    this.fotoPreviewUrl.set(URL.createObjectURL(file));
    this.uploadingFoto.set(true);
    this.errorPerfil.set(null);
    this.usuarioService.uploadMiFoto(file).subscribe({
      next: () => {
        this.uploadingFoto.set(false);
        input.value = '';
      },
      error: (err) => {
        this.errorPerfil.set(err.error?.mensaje || 'Error al subir la foto.');
        this.uploadingFoto.set(false);
        input.value = '';
        this.clearFotoPreview();
      },
    });
  }

  clearFotoPreview(): void {
    if (this.fotoPreviewUrl()) {
      URL.revokeObjectURL(this.fotoPreviewUrl()!);
      this.fotoPreviewUrl.set(null);
    }
    if (this.fotoPerfilInputRef?.nativeElement) {
      this.fotoPerfilInputRef.nativeElement.value = '';
    }
  }

  abrirEliminarCuenta(): void {
    this.errorEliminarCuenta.set(null);
    this.formEliminarCuenta.reset({ passwordEliminar: '', confirmoEliminacion: false });
    this.showEliminarCuenta.set(true);
  }

  cerrarEliminarCuenta(): void {
    if (this.deletingCuenta()) return;
    this.showEliminarCuenta.set(false);
    this.errorEliminarCuenta.set(null);
    this.formEliminarCuenta.reset({ passwordEliminar: '', confirmoEliminacion: false });
  }

  enviarEliminarCuenta(): void {
    if (this.formEliminarCuenta.invalid) {
      this.formEliminarCuenta.markAllAsTouched();
      return;
    }
    const pwd = (this.formEliminarCuenta.get('passwordEliminar')?.value as string)?.trim() ?? '';
    this.deletingCuenta.set(true);
    this.errorEliminarCuenta.set(null);
    this.usuarioService.eliminarMiCuenta({ password: pwd }).subscribe({
      next: () => {
        this.deletingCuenta.set(false);
        this.usuarioService.clearCurrentUser();
        this.authService.logout();
      },
      error: (err) => {
        this.errorEliminarCuenta.set(err.error?.mensaje || 'No se pudo eliminar la cuenta. Intenta de nuevo.');
        this.deletingCuenta.set(false);
      },
    });
  }
}
