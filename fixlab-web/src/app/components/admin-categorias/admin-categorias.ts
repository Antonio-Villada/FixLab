import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CategoriaService } from '../../services/categoria.service';
import { CategoriaRespDTO } from '../../models/product.model';

@Component({
  selector: 'app-admin-categorias',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-categorias.html',
  styleUrl: './admin-categorias.css',
})
export class AdminCategoriasComponent implements OnInit {
  private categoriaService = inject(CategoriaService);
  private fb = inject(FormBuilder);

  list = signal<CategoriaRespDTO[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  modalVisible = signal(false);
  editing = signal<CategoriaRespDTO | null>(null);

  form: FormGroup = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.categoriaService.getAll().subscribe({
      next: (data) => {
        this.list.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al cargar categorías');
        this.loading.set(false);
      },
    });
  }

  openCreate(): void {
    this.editing.set(null);
    this.form.reset({ nombre: '' });
    this.modalVisible.set(true);
  }

  openEdit(c: CategoriaRespDTO): void {
    this.editing.set(c);
    this.form.patchValue({ nombre: c.nombre });
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
    const c = this.editing();
    if (c) {
      this.categoriaService.update(c.id, nombre).subscribe({
        next: () => {
          this.load();
          this.closeModal();
        },
        error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al actualizar'),
      });
    } else {
      this.categoriaService.create(nombre).subscribe({
        next: () => {
          this.load();
          this.closeModal();
        },
        error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al crear (¿nombre duplicado?)'),
      });
    }
  }

  confirmDelete(c: CategoriaRespDTO): void {
    if (!confirm(`¿Eliminar la categoría "${c.nombre}"?`)) return;
    this.categoriaService.delete(c.id).subscribe({
      next: () => this.load(),
      error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al eliminar'),
    });
  }
}
