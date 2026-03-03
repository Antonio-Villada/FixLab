import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { DecimalPipe, CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductService } from '../../services/product';
import { AuthService } from '../../services/auth';
import { CartService } from '../../services/cart.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [DecimalPipe, CommonModule, ReactiveFormsModule],
  templateUrl: './product-list.html',
  styleUrl: './product-list.css'
})
export class ProductListComponent implements OnInit {
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private cartService = inject(CartService);
  private fb = inject(FormBuilder);

  products = signal<Product[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  modalVisible = signal(false);
  editingProduct = signal<Product | null>(null);
  /** Vista previa: URL local del archivo seleccionado o la URL del formulario */
  imagePreviewUrl = signal<string | null>(null);
  /** Archivo seleccionado para subir (al enviar se sube y se usa la URL devuelta) */
  selectedFile = signal<File | null>(null);
  uploadingImage = signal(false);

  isAdmin = computed(() => this.authService.isAdmin());
  /** Para clientes solo productos activos; para admin todos */
  productsToShow = computed(() => {
    const list = this.products();
    return this.isAdmin() ? list : list.filter(p => p.activo);
  });
  isEditing = computed(() => this.editingProduct() !== null);

  form: FormGroup = this.fb.group({
    sku: ['', [Validators.required, Validators.maxLength(50)]],
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    imagenUrl: ['', [Validators.maxLength(500)]],
    activo: [true]
  });

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

  addToCart(product: Product): void {
    this.cartService.addItem(product);
  }

  openCreate(): void {
    this.editingProduct.set(null);
    this.form.reset({ sku: '', nombre: '', descripcion: '', precio: 0, stock: 0, imagenUrl: '', activo: true });
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
      activo: product.activo
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
    if (url && url.startsWith('blob:')) {
      URL.revokeObjectURL(url);
    }
    this.imagePreviewUrl.set(null);
    this.selectedFile.set(null);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !file.type.startsWith('image/')) return;
    this.clearImagePreview();
    this.selectedFile.set(file);
    this.imagePreviewUrl.set(URL.createObjectURL(file));
    this.form.patchValue({ imagenUrl: '' });
    input.value = '';
  }

  /** Quita la imagen seleccionada para poder usar solo el campo URL o guardar sin imagen. */
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
    const editing = this.editingProduct();
    const file = this.selectedFile();

    if (editing?.id != null) {
      // Editar: se envía JSON (PUT). Si tu backend tiene PUT con multipart, se puede cambiar.
      const payload = {
        sku: value.sku,
        nombre: value.nombre,
        descripcion: value.descripcion || '',
        precio: Number(value.precio),
        stock: Number(value.stock),
        imagenUrl: value.imagenUrl || '',
        activo: !!value.activo
      };
      this.productService.update(editing.id, payload).subscribe({
        next: () => { this.loadProducts(); this.closeModal(); },
        error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al actualizar')
      });
      return;
    }

    // Crear: multipart con imagen (tu controlador POST espera sku, nombre, descripcion, precio, stock, imagen)
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
        this.errorMessage.set(err.error?.mensaje || err.message || 'Error al crear el producto.');
      }
    });
  }

  confirmDelete(product: Product): void {
    if (!product.id) return;
    if (!confirm(`¿Eliminar el producto "${product.nombre}"?`)) return;
    this.productService.delete(product.id).subscribe({
      next: () => this.loadProducts(),
      error: (err) => this.errorMessage.set(err.error?.mensaje || 'Error al eliminar')
    });
  }
}
