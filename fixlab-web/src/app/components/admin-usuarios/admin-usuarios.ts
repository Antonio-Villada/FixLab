import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UsuarioService } from '../../services/usuario.service';
import { AuthService } from '../../services/auth';
import {
  UsuarioRespDTO,
  UsuarioUpdateReqDTO,
  RegistroEmpleadoReqDTO,
  RolUsuario,
} from '../../models/auth.model';
import { disposableEmailAsyncValidator } from '../../validators/disposable-email.validator';
import {
  getPasswordRequirements,
  getPasswordStrength,
  getStrengthLabel,
} from '../../utils/password.utils';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-usuarios.html',
  styleUrl: './admin-usuarios.css',
})
export class AdminUsuariosComponent implements OnInit {
  private usuarioService = inject(UsuarioService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  readonly ROLES = [RolUsuario.ADMIN, RolUsuario.TECNICO, RolUsuario.RECEPCIONISTA, RolUsuario.CLIENTE];

  /** Roles permitidos al registrar empleado (sin cliente). */
  readonly ROLES_EMPLEADO = [RolUsuario.ADMIN, RolUsuario.TECNICO, RolUsuario.RECEPCIONISTA];

  list = signal<UsuarioRespDTO[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  modalVisible = signal(false);
  modalRolVisible = signal(false);
  editing = signal<UsuarioRespDTO | null>(null);

  formEdit: FormGroup = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    telefono: ['', [Validators.maxLength(20)]],
    nuevaPassword: ['', [Validators.minLength(8), Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/)]],
    confirmarPassword: [''],
  });

  formNewEmployee: FormGroup = this.fb.group({
    cedula: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(20)]],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    email: [
      '',
      [Validators.required, Validators.email],
      [disposableEmailAsyncValidator(this.authService)],
    ],
    password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/)]],
    telefono: ['', [Validators.maxLength(20)]],
    rol: [RolUsuario.TECNICO, [Validators.required]],
  });

  formRol: FormGroup = this.fb.group({
    nuevoRol: [null as RolUsuario | null, [Validators.required]],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.usuarioService.getAll().subscribe({
      next: (data) => {
        this.list.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al cargar usuarios');
        this.loading.set(false);
      },
    });
  }

  openCreate(): void {
    this.editing.set(null);
    this.formNewEmployee.reset({
      cedula: '',
      nombre: '',
      apellido: '',
      email: '',
      password: '',
      telefono: '',
      rol: RolUsuario.TECNICO,
    });
    this.modalVisible.set(true);
  }

  openEdit(u: UsuarioRespDTO): void {
    this.editing.set(u);
    this.modalRolVisible.set(false);
    this.formEdit.patchValue({
      nombre: u.nombre,
      apellido: u.apellido,
      telefono: u.telefono ?? '',
      nuevaPassword: '',
      confirmarPassword: '',
    });
    this.modalVisible.set(true);
  }

  openChangeRol(u: UsuarioRespDTO): void {
    this.editing.set(u);
    this.modalVisible.set(false);
    this.formRol.patchValue({ nuevoRol: u.rol });
    this.modalRolVisible.set(true);
  }

  closeModal(): void {
    this.modalVisible.set(false);
    this.modalRolVisible.set(false);
    this.editing.set(null);
  }

  onSubmitEdit(): void {
    const raw = this.formEdit.getRawValue();
    const nuevaPassword = (raw.nuevaPassword as string)?.trim() || '';
    const confirmarPassword = (raw.confirmarPassword as string)?.trim() || '';
    if (nuevaPassword || confirmarPassword) {
      if (nuevaPassword.length < 8) {
        this.errorMessage.set('La nueva contraseña debe tener al menos 8 caracteres.');
        this.formEdit.get('nuevaPassword')?.markAsTouched();
        return;
      }
      if (this.formEdit.get('nuevaPassword')?.invalid) {
        this.errorMessage.set('La contraseña debe incluir letras, números y caracteres especiales.');
        this.formEdit.get('nuevaPassword')?.markAsTouched();
        return;
      }
      if (nuevaPassword !== confirmarPassword) {
        this.errorMessage.set('La contraseña y la confirmación no coinciden.');
        this.formEdit.get('confirmarPassword')?.markAsTouched();
        return;
      }
    }
    if (this.formEdit.get('nombre')?.invalid || this.formEdit.get('apellido')?.invalid || this.formEdit.get('telefono')?.invalid) {
      this.formEdit.markAllAsTouched();
      return;
    }
    const u = this.editing();
    if (!u) return;
    const dto: UsuarioUpdateReqDTO = {
      nombre: raw.nombre,
      apellido: raw.apellido,
      telefono: raw.telefono,
    };
    this.usuarioService.update(u.cedula, dto).subscribe({
      next: () => {
        if (nuevaPassword) {
          this.authService.asignarNuevaPassword({ cedula: u.cedula, nuevaPassword }).subscribe({
            next: () => {
              this.load();
              this.closeModal();
              this.errorMessage.set(null);
            },
            error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al asignar contraseña'),
          });
        } else {
          this.load();
          this.closeModal();
        }
      },
      error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al actualizar'),
    });
  }

  onSubmitNewEmployee(): void {
    if (this.formNewEmployee.invalid) {
      this.formNewEmployee.markAllAsTouched();
      return;
    }
    const dto: RegistroEmpleadoReqDTO = this.formNewEmployee.getRawValue();
    this.authService.registrarEmpleado(dto).subscribe({
      next: () => {
        this.load();
        this.closeModal();
      },
      error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al crear empleado'),
    });
  }

  onSubmitRol(): void {
    if (this.formRol.invalid) return;
    const u = this.editing();
    if (!u) return;
    this.authService.cambiarRol({ cedula: u.cedula, nuevoRol: this.formRol.getRawValue().nuevoRol }).subscribe({
      next: () => {
        this.load();
        this.closeModal();
      },
      error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al cambiar rol'),
    });
  }

  confirmDelete(u: UsuarioRespDTO): void {
    if (!confirm(`¿Eliminar al usuario ${u.nombre} ${u.apellido} (${u.email})?`)) return;
    this.usuarioService.delete(u.cedula).subscribe({
      next: () => this.load(),
      error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al eliminar'),
    });
  }

  rolLabel(rol: RolUsuario): string {
    if (rol === RolUsuario.ADMIN) return 'Administrador';
    if (rol === RolUsuario.TECNICO) return 'Técnico';
    if (rol === RolUsuario.RECEPCIONISTA) return 'Recepcionista';
    return 'Cliente';
  }

  /** Utilidades de contraseña para el template */
  getPasswordRequirements = getPasswordRequirements;
  getPasswordStrength = getPasswordStrength;
  getStrengthLabel = getStrengthLabel;
}
