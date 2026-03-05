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

  readonly ROLES = [RolUsuario.ADMIN, RolUsuario.TECNICO, RolUsuario.CLIENTE];

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
  });

  formNewEmployee: FormGroup = this.fb.group({
    cedula: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(20)]],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
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
    if (this.formEdit.invalid) {
      this.formEdit.markAllAsTouched();
      return;
    }
    const u = this.editing();
    if (!u) return;
    const dto: UsuarioUpdateReqDTO = this.formEdit.getRawValue();
    this.usuarioService.update(u.cedula, dto).subscribe({
      next: () => {
        this.load();
        this.closeModal();
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
    return rol === RolUsuario.ADMIN ? 'Administrador' : rol === RolUsuario.TECNICO ? 'Técnico' : 'Cliente';
  }
}
