import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TipoProductoService } from '../../services/tipo-producto.service';
import { TipoProductoRespDTO } from '../../models/product.model';

@Component({
  selector: 'app-admin-tipos-producto',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-tipos-producto.html',
  styleUrl: './admin-tipos-producto.css',
})
export class AdminTiposProductoComponent implements OnInit {
  private tipoProductoService = inject(TipoProductoService);
  private fb = inject(FormBuilder);

  list = signal<TipoProductoRespDTO[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  modalVisible = signal(false);
  editing = signal<TipoProductoRespDTO | null>(null);

  form: FormGroup = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.tipoProductoService.getAll().subscribe({
      next: (data) => {
        this.list.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al cargar tipos de producto');
        this.loading.set(false);
      },
    });
  }

  openCreate(): void {
    this.editing.set(null);
    this.form.reset({ nombre: '' });
    this.modalVisible.set(true);
  }

  openEdit(t: TipoProductoRespDTO): void {
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
      this.tipoProductoService.update(t.id, nombre).subscribe({
        next: () => {
          this.load();
          this.closeModal();
        },
        error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al actualizar'),
      });
    } else {
      this.tipoProductoService.create(nombre).subscribe({
        next: () => {
          this.load();
          this.closeModal();
        },
        error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al crear (¿nombre duplicado?)'),
      });
    }
  }

  confirmDelete(t: TipoProductoRespDTO): void {
    if (!confirm(`¿Eliminar el tipo "${t.nombre}"?`)) return;
    this.tipoProductoService.delete(t.id).subscribe({
      next: () => this.load(),
      error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al eliminar'),
    });
  }
}
