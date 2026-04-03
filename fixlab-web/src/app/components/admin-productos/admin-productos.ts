import { Component, OnInit, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProductService } from '../../services/product';
import { ReparacionService } from '../../services/reparacion.service';
import {
  Product,
  ProductoReqDTO,
  CategoriaRespDTO,
  TipoProductoRespDTO,
} from '../../models/product.model';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-productos.html',
  styleUrl: './admin-productos.css',
})
export class AdminProductosComponent implements OnInit {
  private productService = inject(ProductService);
  private reparacionService = inject(ReparacionService);
  private fb = inject(FormBuilder);
  private platformId = inject(PLATFORM_ID);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

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
  // Modo selección de repuestos para una reparación.
  modoSeleccionReparacion = signal(false);
  reparacionId = signal<number | null>(null);
  returnTo = signal<string>('/admin/taller/gestion');
  cantidadSeleccion = signal<Record<number, string>>({});
  infoMessage = signal<string | null>(null);

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
      this.route.queryParamMap.subscribe((params) => {
        const repId = Number(params.get('reparacionId'));
        const returnTo = params.get('returnTo');
        const activo = Number.isFinite(repId) && repId > 0;
        this.modoSeleccionReparacion.set(activo);
        this.reparacionId.set(activo ? repId : null);
        this.returnTo.set(returnTo?.trim() || '/admin/taller/gestion');
      });
      this.loadProducts();
      this.loadCategoriasAndTipos();
    }
  }

  cantidadDe(productoId?: number): string {
    if (!productoId) return '1';
    return this.cantidadSeleccion()[productoId] ?? '1';
  }

  setCantidad(productoId: number, cantidad: string): void {
    this.cantidadSeleccion.update((curr) => ({ ...curr, [productoId]: cantidad }));
  }

  agregarProductoAReparacion(product: Product): void {
    const repId = this.reparacionId();
    if (!repId || !product.id) return;
    if (product.activo === false) {
      this.errorMessage.set('El producto está inactivo.');
      return;
    }
    const stock = product.stock;
    if (stock <= 0) {
      this.errorMessage.set('Sin stock para este producto.');
      return;
    }
    const raw = this.cantidadDe(product.id).trim();
    const cant = parseInt(raw, 10);
    if (!Number.isFinite(cant) || cant <= 0) {
      this.errorMessage.set('La cantidad debe ser un entero mayor que 0.');
      return;
    }
    if (cant > stock) {
      this.errorMessage.set(`Stock insuficiente (disponible: ${stock}).`);
      return;
    }
    this.errorMessage.set(null);
    this.infoMessage.set(null);
    this.reparacionService.agregarProducto(repId, { productoId: product.id, cantidad: cant }).subscribe({
      next: () => {
        this.infoMessage.set(`Agregado: ${product.nombre} x ${cant}`);
        this.loadProducts();
      },
      error: (err) => {
        this.errorMessage.set(err.error?.mensaje || 'No se pudo agregar el producto a la reparación.');
      },
    });
  }

  volverAGestion(): void {
    const repId = this.reparacionId();
    const destino = this.returnTo();
    this.router.navigate([destino], {
      queryParams: repId ? { id: repId } : {},
    });
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
