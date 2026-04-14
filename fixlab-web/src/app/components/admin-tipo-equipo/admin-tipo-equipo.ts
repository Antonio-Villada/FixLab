import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TipoEquipoService } from '../../services/tipo-equipo.service';
import { TipoEquipoRespDTO } from '../../models/reparacion.model';

@Component({
  selector: 'app-admin-tipo-equipo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-tipo-equipo.html',
  styleUrl: './admin-tipo-equipo.css',
})
export class AdminTipoEquipoComponent implements OnInit {
  private tipoEquipoService = inject(TipoEquipoService);
  private fb = inject(FormBuilder);

  list = signal<TipoEquipoRespDTO[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  modalVisible = signal(false);
  editing = signal<TipoEquipoRespDTO | null>(null);

  form: FormGroup = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.tipoEquipoService.getAll().subscribe({
      next: (data) => {
        this.list.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al cargar tipos de equipo');
        this.loading.set(false);
      },
    });
  }

  openCreate(): void {
    this.editing.set(null);
    this.form.reset({ nombre: '' });
    this.modalVisible.set(true);
  }

  openEdit(t: TipoEquipoRespDTO): void {
    this.editing.set(t);
    this.form.patchValue({ nombre: t.nombre });
    this.modalVisible.set(true);
  }

  closeModal(): void {
    this.modalVisible.set(false);
    this.editing.set(null);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const nombre = this.form.getRawValue().nombre.trim();
    const t = this.editing();
    if (t) {
      this.tipoEquipoService.update(t.id, nombre).subscribe({
        next: () => {
          this.load();
          this.closeModal();
        },
        error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al actualizar'),
      });
    } else {
      this.tipoEquipoService.create(nombre).subscribe({
        next: () => {
          this.load();
          this.closeModal();
        },
        error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al crear (¿nombre duplicado?)'),
      });
    }
  }

  confirmDelete(t: TipoEquipoRespDTO): void {
    if (!confirm(`¿Eliminar el tipo de equipo "${t.nombre}"?`)) return;
    this.tipoEquipoService.delete(t.id).subscribe({
      next: () => this.load(),
      error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al eliminar'),
    });
  }
}
