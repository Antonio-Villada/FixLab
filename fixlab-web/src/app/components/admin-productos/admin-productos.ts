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
  EntradaMercanciaRespDTO,
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

  /** Filtros de tabla (nombre/SKU, categoría, tipo). */
  filtroTexto = signal('');
  filtroCategoriaId = signal<number | null>(null);
  filtroTipoProductoId = signal<number | null>(null);
  filtroSoloStockBajo = signal(false);

  /** Lista aplicando filtros actuales (sin nueva llamada al API). */
  productsFiltrados = computed(() => {
    let list = this.products();
    const texto = this.filtroTexto().trim().toLowerCase();
    if (texto) {
      list = list.filter((p) => {
        const n = (p.nombre ?? '').toLowerCase();
        const s = (p.sku ?? '').toLowerCase();
        return n.includes(texto) || s.includes(texto);
      });
    }
    const catId = this.filtroCategoriaId();
    if (catId != null) {
      list = list.filter((p) => p.categoria?.id === catId);
    }
    const tipoId = this.filtroTipoProductoId();
    if (tipoId != null) {
      list = list.filter((p) => p.tipoProducto?.id === tipoId);
    }
    if (this.filtroSoloStockBajo()) {
      list = list.filter((p) => this.esStockBajo(p));
    }
    return list;
  });

  /** Productos activos con stock en o por debajo del mínimo (para aviso superior). */
  productosConAlertaStock = computed(() =>
    this.products().filter((p) => p.activo !== false && this.esStockBajo(p)).length,
  );

  filtrosActivos = computed(
    () =>
      this.filtroTexto().trim().length > 0 ||
      this.filtroCategoriaId() != null ||
      this.filtroTipoProductoId() != null ||
      this.filtroSoloStockBajo(),
  );

  form: FormGroup = this.fb.group({
    sku: ['', [Validators.required, Validators.maxLength(50)]],
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    stockMinimo: [5, [Validators.required, Validators.min(0)]],
    categoriaId: [null as number | null, [Validators.required]],
    tipoProductoId: [null as number | null, [Validators.required]],
  });

  /** Modal registrar entrada de mercancía. */
  entradaProduct = signal<Product | null>(null);
  entradaCantidad = signal('1');
  entradaComentario = signal('');
  entradaSubmitting = signal(false);

  /** Modal historial de entradas. */
  historialProduct = signal<Product | null>(null);
  historialEntradas = signal<EntradaMercanciaRespDTO[]>([]);
  historialLoading = signal(false);

  isEditing = computed(() => this.editingProduct() !== null);

  limpiarFiltros(): void {
    this.filtroTexto.set('');
    this.filtroCategoriaId.set(null);
    this.filtroTipoProductoId.set(null);
    this.filtroSoloStockBajo.set(false);
  }

  stockMinimoEfectivo(p: Product): number {
    const v = p.stockMinimo;
    return v != null && Number.isFinite(v) ? v : 5;
  }

  esStockBajo(p: Product): boolean {
    return p.stock <= this.stockMinimoEfectivo(p);
  }

  openEntradaMercancia(product: Product): void {
    this.entradaProduct.set(product);
    this.entradaCantidad.set('1');
    this.entradaComentario.set('');
  }

  closeEntradaMercancia(): void {
    this.entradaProduct.set(null);
    this.entradaSubmitting.set(false);
  }

  submitEntradaMercancia(): void {
    const product = this.entradaProduct();
    if (!product?.id) return;
    const rawCant = String(this.entradaCantidad() ?? '').trim();
    const cant = parseInt(rawCant, 10);
    if (!Number.isFinite(cant) || cant < 1) {
      this.errorMessage.set('La cantidad debe ser un entero mayor o igual a 1.');
      return;
    }
    this.errorMessage.set(null);
    this.entradaSubmitting.set(true);
    const comentario = this.entradaComentario().trim();
    this.productService
      .registrarEntradaMercancia(product.id, {
        cantidad: cant,
        comentario: comentario.length > 0 ? comentario : undefined,
      })
      .subscribe({
        next: (resp) => {
          this.entradaSubmitting.set(false);
          this.infoMessage.set(
            `Entrada registrada: +${resp.cantidad} unidades. Nuevo stock: ${resp.nuevoStock}.`,
          );
          this.closeEntradaMercancia();
          this.loadProducts();
        },
        error: (err) => {
          this.entradaSubmitting.set(false);
          const body = err.error;
          let msg: string | undefined;
          if (typeof body?.mensaje === 'string') {
            msg = body.mensaje;
          } else if (body && typeof body === 'object') {
            const first = Object.values(body as Record<string, string>).find((v) => typeof v === 'string');
            msg = first;
          }
          this.errorMessage.set(msg ?? 'No se pudo registrar la entrada de mercancía.');
        },
      });
  }

  openHistorialEntradas(product: Product): void {
    if (!product.id) return;
    this.historialProduct.set(product);
    this.historialEntradas.set([]);
    this.historialLoading.set(true);
    this.productService.getEntradasMercancia(product.id).subscribe({
      next: (list) => {
        this.historialEntradas.set(list);
        this.historialLoading.set(false);
      },
      error: () => {
        this.historialLoading.set(false);
        this.historialEntradas.set([]);
        this.errorMessage.set('No se pudo cargar el historial de entradas.');
      },
    });
  }

  closeHistorialEntradas(): void {
    this.historialProduct.set(null);
    this.historialEntradas.set([]);
  }

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
      stockMinimo: 5,
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
      stockMinimo: this.stockMinimoEfectivo(product),
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
      stockMinimo: Number(value.stockMinimo),
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
