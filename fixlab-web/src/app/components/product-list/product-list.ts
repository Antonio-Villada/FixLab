import { Component, OnInit, inject } from '@angular/core';
import { DecimalPipe, CommonModule, TitleCasePipe } from '@angular/common';
import { ProductService } from '../../services/product';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [DecimalPipe, CommonModule, TitleCasePipe],
  templateUrl: './product-list.html'
})
export class ProductListComponent implements OnInit {
  private productService = inject(ProductService);

  products: Product[] = [];
  productsFiltered: Product[] = [];

  // 1. Aseguramos que sea un array de OBJETOS, no de strings
  categorias = [
    { id: 1, nombre: 'portatiles' },
    { id: 2, nombre: 'computadores' },
    { id: 3, nombre: 'accesorios' },
    { id: 4, nombre: 'redes' },
    { id: 5, nombre: 'repuestos' }
  ];

  // 2. Nombres exactos que pide el HTML
  categoriaSeleccionadaId: number | 'todos' = 'todos';
  nombreCategoriaActual: string = 'todos';

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.productsFiltered = data;
      },
      error: (err) => console.error('Error al cargar productos', err)
    });
  }

  // 3. Método actualizado para manejar el objeto o el string 'todos'
  filtrarPorCategoria(cat: any) {
    if (cat === 'todos') {
      this.categoriaSeleccionadaId = 'todos';
      this.nombreCategoriaActual = 'todos';
      this.productsFiltered = this.products;
    } else {
      this.categoriaSeleccionadaId = cat.id;
      this.nombreCategoriaActual = cat.nombre;
      // IMPORTANTE: Usamos categoryId que es lo que tiene tu modelo
      this.productsFiltered = this.products.filter(p => p.categoryId === cat.id);
    }
  }

  addToCart(product: Product) {
    console.log('Producto añadido:', product.name);
    alert(`¡${product.name} añadido al carrito!`);
  }
}