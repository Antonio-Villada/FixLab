import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductService } from '../../services/product';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-productos.html',
  styleUrl: './admin-productos.css'
})
export class AdminProductosComponent implements OnInit {
  private productService = inject(ProductService);
  private fb = inject(FormBuilder);

  products = signal<Product[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  modalVisible = signal(false);
  editingProduct = signal<Product | null>(null);
  selectedFile = signal<File | null>(null);
  uploadingImage = signal(false);

  form: FormGroup = this.fb.group({
    sku: ['', [Validators.required, Validators.maxLength(50)]],
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    imagenUrl: ['', [Validators.maxLength(500)]],
    activo: [true]
  });

  isEditing = computed(() => this.editingProduct() !== null);

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.productService.getProducts().subscribe({
      next: (data) => {
        this.products.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al cargar productos');
        this.loading.set(false);
      }
    });
  }

  openCreate(): void {
    this.editingProduct.set(null);
    this.selectedFile.set(null);
    this.form.reset({ sku: '', nombre: '', descripcion: '', precio: 0, stock: 0, imagenUrl: '', activo: true });
    this.modalVisible.set(true);
  }

  openEdit(product: Product): void {
    this.editingProduct.set(product);
    this.form.patchValue({
      sku: product.sku,
      nombre: product.nombre,
      descripcion: product.descripcion ?? '',
      precio: product.precio,
      stock: product.stock,
      imagenUrl: product.imagenUrl ?? '',
      activo: product.activo
    });
    this.modalVisible.set(true);
  }

  closeModal(): void {
    this.modalVisible.set(false);
    this.editingProduct.set(null);
    this.selectedFile.set(null);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file?.type.startsWith('image/')) {
      this.selectedFile.set(file);
    }
    input.value = '';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const productPayload = {
      sku: value.sku,
      nombre: value.nombre,
      descripcion: value.descripcion || '',
      precio: Number(value.precio),
      stock: Number(value.stock),
      imagenUrl: value.imagenUrl || '',
      activo: !!value.activo
    };

    const editing = this.editingProduct();
    if (editing?.id != null) {
      this.productService.update(editing.id, productPayload).subscribe({
        next: () => {
          this.loadProducts();
          this.closeModal();
        },
        error: (err) => {
          this.errorMessage.set(err.error?.mensaje || 'Error al actualizar');
        }
      });
    } else {
      const file = this.selectedFile();
      if (!file) {
        this.errorMessage.set('Selecciona una imagen para el producto.');
        return;
      }
      this.uploadingImage.set(true);
      const data = {
        sku: value.sku,
        nombre: value.nombre,
        descripcion: value.descripcion || '',
        precio: Number(value.precio),
        stock: Number(value.stock)
      };
      this.productService.createWithMultipart(data, file).subscribe({
        next: () => {
          this.uploadingImage.set(false);
          this.loadProducts();
          this.closeModal();
        },
        error: (err) => {
          this.uploadingImage.set(false);
          this.errorMessage.set(err.error?.mensaje || 'Error al crear (¿SKU duplicado?)');
        }
      });
    }
  }

  confirmDelete(product: Product): void {
    if (!product.id) return;
    if (!confirm(`¿Eliminar el producto "${product.nombre}"?`)) return;
    this.productService.delete(product.id).subscribe({
      next: () => this.loadProducts(),
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al eliminar');
      }
    });
  }
}
