import {
  Component,
  OnInit,
  inject,
  signal,
  computed,
  PLATFORM_ID,
} from '@angular/core';
import { DecimalPipe, CommonModule, isPlatformBrowser } from '@angular/common';
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
  private platformId = inject(PLATFORM_ID);
  private fb = inject(FormBuilder);

  products = signal<Product[]>([]);
  categorias = signal<CategoriaRespDTO[]>([]);
  tiposProducto = signal<TipoProductoRespDTO[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  modalVisible = signal(false);
  detailModalVisible = signal(false);
  detailProduct = signal<Product | null>(null);
  editingProduct = signal<Product | null>(null);
  imagePreviewUrl = signal<string | null>(null);
  selectedFile = signal<File | null>(null);
  uploadingImage = signal(false);

  /** Filtros para la vista cliente (categoría y/o tipo). null = ver todos. */
  filterCategoriaId = signal<number | null>(null);
  filterTipoProductoId = signal<number | null>(null);

  /** Paginación (cliente). */
  pageSize = signal<number>(9);
  currentPage = signal<number>(1);

  isAdmin = computed(() => this.authService.isAdmin());
  /** Clientes: solo productos activos + filtros. Admin: todos. */
  productsToShow = computed(() => {
    const list = this.products();
    let result = this.isAdmin() ? list : list.filter((p) => p.activo !== false);
    if (!this.isAdmin()) {
      const catId = this.filterCategoriaId();
      const tipoId = this.filterTipoProductoId();
      if (catId != null) result = result.filter((p) => p.categoria?.id === catId);
      if (tipoId != null) result = result.filter((p) => p.tipoProducto?.id === tipoId);
    }
    return result;
  });

  totalPages = computed(() => {
    const total = this.productsToShow().length;
    if (this.isAdmin()) return 1;
    return Math.max(1, Math.ceil(total / this.pageSize()));
  });

  pagedProducts = computed(() => {
    const list = this.productsToShow();
    if (this.isAdmin()) return list;
    const size = this.pageSize();
    const page = this.currentPage();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  visiblePages = computed(() => {
    if (this.isAdmin()) return [];
    const total = this.totalPages();
    const current = this.currentPage();
    const windowSize = 7;
    const half = Math.floor(windowSize / 2);
    let start = Math.max(1, current - half);
    let end = Math.min(total, start + windowSize - 1);
    start = Math.max(1, end - windowSize + 1);
    const pages: number[] = [];
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  });

  private syncPagination(resetToFirstPage: boolean): void {
    if (this.isAdmin()) return;
    const total = this.totalPages();
    const current = resetToFirstPage ? 1 : this.currentPage();
    const clamped = Math.max(1, Math.min(total, current));
    this.currentPage.set(clamped);
  }

  isEditing = computed(() => this.editingProduct() !== null);

  form: FormGroup = this.fb.group({
    sku: ['', [Validators.required, Validators.maxLength(50)]],
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    categoriaId: [null as number | null, [Validators.required]],
    tipoProductoId: [null as number | null, [Validators.required]],
  });

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
        this.syncPagination(false);
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

  /** Cliente: solo si hay stock y el producto sigue activo. */
  puedeAgregarAlCarrito(product: Product): boolean {
    return product.stock > 0 && product.activo !== false;
  }

  addToCart(product: Product): void {
    if (!this.puedeAgregarAlCarrito(product)) {
      return;
    }
    this.cartService.addItem(product);
  }

  setPage(page: number): void {
    if (this.isAdmin()) return;
    const clamped = Math.max(1, Math.min(this.totalPages(), page));
    this.currentPage.set(clamped);
  }

  prevPage(): void {
    this.setPage(this.currentPage() - 1);
  }

  nextPage(): void {
    this.setPage(this.currentPage() + 1);
  }

  onCategoriaChange(value: string): void {
    this.filterCategoriaId.set(value === '' ? null : +value);
    this.syncPagination(true);
  }

  onTipoProductoChange(value: string): void {
    this.filterTipoProductoId.set(value === '' ? null : +value);
    this.syncPagination(true);
  }

  onPageSizeChange(value: string): void {
    this.pageSize.set(+value);
    this.syncPagination(true);
  }

  clearFilters(): void {
    this.filterCategoriaId.set(null);
    this.filterTipoProductoId.set(null);
    this.syncPagination(true);
  }

  openDetail(product: Product): void {
    this.detailProduct.set(product);
    this.detailModalVisible.set(true);
  }

  closeDetail(): void {
    this.detailModalVisible.set(false);
    this.detailProduct.set(null);
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
