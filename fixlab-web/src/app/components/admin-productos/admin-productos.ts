import { Component, OnInit, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProductService } from '../../services/product';
import {
  Product,
  ProductoReqDTO,
  CategoriaRespDTO,
  TipoProductoRespDTO,
} from '../../models/product.model';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-productos.html',
  styleUrl: './admin-productos.css',
})
export class AdminProductosComponent implements OnInit {
  private productService = inject(ProductService);
  private fb = inject(FormBuilder);
  private platformId = inject(PLATFORM_ID);

  products = signal<Product[]>([]);
  categorias = signal<CategoriaRespDTO[]>([]);
  tiposProducto = signal<TipoProductoRespDTO[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  modalVisible = signal(false);
  editingProduct = signal<Product | null>(null);
  imagePreviewUrl = signal<string | null>(null);
  selectedFile = signal<File | null>(null);
  uploadingImage = signal(false);

  form: FormGroup = this.fb.group({
    sku: ['', [Validators.required, Validators.maxLength(50)]],
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    categoriaId: [null as number | null, [Validators.required]],
    tipoProductoId: [null as number | null, [Validators.required]],
  });

  isEditing = computed(() => this.editingProduct() !== null);

  ngOnInit(): void {
    // Evitar llamadas al backend durante SSR/prerender (SSR no tiene localStorage).
    if (isPlatformBrowser(this.platformId)) {
      this.loadProducts();
      this.loadCategoriasAndTipos();
    }
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
      },
    });
  }

  loadCategoriasAndTipos(): void {
    this.productService.getCategorias().subscribe({
      next: (list) => this.categorias.set(list),
      error: () => this.categorias.set([]),
    });
    this.productService.getTiposProducto().subscribe({
      next: (list) => this.tiposProducto.set(list),
      error: () => this.tiposProducto.set([]),
    });
  }

  openCreate(): void {
    this.editingProduct.set(null);
    this.form.reset({
      sku: '',
      nombre: '',
      descripcion: '',
      precio: 0,
      stock: 0,
      categoriaId: null,
      tipoProductoId: null,
    });
    this.clearImagePreview();
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
      categoriaId: product.categoria?.id ?? null,
      tipoProductoId: product.tipoProducto?.id ?? null,
    });
    this.selectedFile.set(null);
    this.imagePreviewUrl.set(product.imagenUrl || null);
    this.modalVisible.set(true);
  }

  closeModal(): void {
    this.modalVisible.set(false);
    this.editingProduct.set(null);
    this.clearImagePreview();
  }

  private clearImagePreview(): void {
    const url = this.imagePreviewUrl();
    if (url?.startsWith('blob:')) {
      URL.revokeObjectURL(url);
    }
    this.imagePreviewUrl.set(null);
    this.selectedFile.set(null);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file?.type.startsWith('image/')) return;
    this.clearImagePreview();
    this.selectedFile.set(file);
    this.imagePreviewUrl.set(URL.createObjectURL(file));
    input.value = '';
  }

  removeSelectedImage(): void {
    this.clearImagePreview();
    const product = this.editingProduct();
    if (product?.imagenUrl) {
      this.imagePreviewUrl.set(product.imagenUrl);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const editing = this.editingProduct();
    const imagenUrl =
      editing && !this.selectedFile() ? (editing.imagenUrl ?? '') : '';
    const data: ProductoReqDTO = {
      sku: value.sku,
      nombre: value.nombre,
      descripcion: value.descripcion || '',
      precio: Number(value.precio),
      stock: Number(value.stock),
      imagenUrl,
      categoriaId: Number(value.categoriaId),
      tipoProductoId: Number(value.tipoProductoId),
    };

    if (editing?.id != null) {
      this.uploadingImage.set(true);
      this.productService
        .updateWithMultipart(editing.id, data, this.selectedFile())
        .subscribe({
          next: () => {
            this.uploadingImage.set(false);
            this.loadProducts();
            this.closeModal();
          },
          error: (err) => {
            this.uploadingImage.set(false);
            this.errorMessage.set(err.error?.mensaje || 'Error al actualizar');
          },
        });
    } else {
      const file = this.selectedFile();
      if (!file) {
        this.errorMessage.set('Selecciona una imagen para el producto.');
        return;
      }
      this.uploadingImage.set(true);
      this.productService.createWithMultipart(data, file).subscribe({
        next: () => {
          this.uploadingImage.set(false);
          this.loadProducts();
          this.closeModal();
        },
        error: (err) => {
          this.uploadingImage.set(false);
          this.errorMessage.set(err.error?.mensaje || 'Error al crear (¿SKU duplicado?)');
        },
      });
    }
  }

  confirmDelete(product: Product): void {
    if (!product.id) return;
    if (!confirm(`¿Desactivar el producto "${product.nombre}"?`)) return;
    this.productService.delete(product.id).subscribe({
      next: () => this.loadProducts(),
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'Error al eliminar');
      },
    });
  }
}
