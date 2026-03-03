import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private http = inject(HttpClient);
  private readonly apiBase = environment.apiBaseUrl?.replace(/\/$/, '') ?? '';
  private readonly baseUrl = this.apiBase ? `${this.apiBase}/api/productos` : '/api/productos';

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.baseUrl);
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  /**
   * Crea un producto enviando multipart/form-data al backend.
   * Parámetros: sku, nombre, descripcion, precio, stock, imagen (archivo).
   * Si imagen es null y el backend lo permite (imagen required=false), se envía sin archivo.
   */
  createWithMultipart(
    data: { sku: string; nombre: string; descripcion: string; precio: number; stock: number },
    imagen: File | null
  ): Observable<Product> {
    const formData = new FormData();
    formData.append('sku', data.sku);
    formData.append('nombre', data.nombre);
    formData.append('descripcion', data.descripcion ?? '');
    formData.append('precio', String(data.precio));
    formData.append('stock', String(data.stock));
    if (imagen) {
      formData.append('imagen', imagen);
    }
    return this.http.post<Product>(this.baseUrl, formData);
  }

  update(id: number, product: Partial<Product>): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, product);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
