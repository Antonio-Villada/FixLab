import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { DecimalPipe, CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductService } from '../../services/product';
import { AuthService } from '../../services/auth';
import { CartService } from '../../services/cart.service';
import {
  Product,
  ProductoReqDTO,
  CategoriaRespDTO,
  TipoProductoRespDTO,
} from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [DecimalPipe, CommonModule, ReactiveFormsModule],
  templateUrl: './product-list.html',
  styleUrl: './product-list.css',
})
export class ProductListComponent implements OnInit {
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private cartService = inject(CartService);
  private fb = inject(FormBuilder);

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

  isAdmin = computed(() => this.authService.isAdmin());
  /** Clientes: solo productos activos. Admin: todos. */
  productsToShow = computed(() => {
    const list = this.products();
    return this.isAdmin() ? list : list.filter((p) => p.activo !== false);
  });
  isEditing = computed(() => this.editingProduct() !== null);

  form: FormGroup = this.fb.group({
    sku: ['', [Validators.required, Validators.maxLength(50)]],
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    imagenUrl: ['', [Validators.maxLength(500)]],
    categoriaId: [null as number | null, [Validators.required]],
    tipoProductoId: [null as number | null, [Validators.required]],
  });

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategoriasAndTipos();
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

  addToCart(product: Product): void {
    this.cartService.addItem(product);
  }

  openCreate(): void {
    this.editingProduct.set(null);
    this.form.reset({
      sku: '',
      nombre: '',
      descripcion: '',
      precio: 0,
      stock: 0,
      imagenUrl: '',
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
      imagenUrl: product.imagenUrl ?? '',
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
    this.form.patchValue({ imagenUrl: '' });
    input.value = '';
  }

  removeSelectedImage(): void {
    this.clearImagePreview();
    this.imagePreviewUrl.set(this.form.get('imagenUrl')?.value || null);
  }

  onImagenUrlChange(): void {
    if (!this.selectedFile()) {
      this.imagePreviewUrl.set(this.form.get('imagenUrl')?.value || null);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const data: ProductoReqDTO = {
      sku: value.sku,
      nombre: value.nombre,
      descripcion: value.descripcion || '',
      precio: Number(value.precio),
      stock: Number(value.stock),
      imagenUrl: value.imagenUrl || '',
      categoriaId: Number(value.categoriaId),
      tipoProductoId: Number(value.tipoProductoId),
    };
    const editing = this.editingProduct();
    const file = this.selectedFile();

    if (editing?.id != null) {
      this.uploadingImage.set(true);
      this.productService.updateWithMultipart(editing.id, data, file).subscribe({
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
      return;
    }

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
        this.errorMessage.set(err.error?.mensaje || err.message || 'Error al crear el producto.');
      },
    });
  }

  confirmDelete(product: Product): void {
    if (!product.id) return;
    if (!confirm(`¿Desactivar el producto "${product.nombre}"?`)) return;
    this.productService.delete(product.id).subscribe({
      next: () => this.loadProducts(),
      error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al eliminar'),
    });
  }
}
