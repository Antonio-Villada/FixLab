import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Product,
  ProductoReqDTO,
  CategoriaRespDTO,
  TipoProductoRespDTO,
} from '../models/product.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private http = inject(HttpClient);
  private readonly apiBase = environment.apiBaseUrl?.replace(/\/$/, '') ?? '';
  private readonly baseUrl = this.apiBase ? `${this.apiBase}/api/productos` : '/api/productos';
  private readonly categoriasUrl = this.apiBase ? `${this.apiBase}/api/categorias` : '/api/categorias';
  private readonly tiposUrl = this.apiBase ? `${this.apiBase}/api/tipos-producto` : '/api/tipos-producto';

  getProducts(filtro?: string, categoriaId?: number): Observable<Product[]> {
    if (filtro) {
      const params: Record<string, string | number> = { filtro };
      if (categoriaId != null && categoriaId > 0) {
        params['categoriaId'] = categoriaId;
      }
      return this.http.get<Product[]>(this.baseUrl, { params });
    }
    return this.http.get<Product[]>(this.baseUrl);
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  getCategorias(): Observable<CategoriaRespDTO[]> {
    return this.http.get<CategoriaRespDTO[]>(this.categoriasUrl);
  }

  getTiposProducto(): Observable<TipoProductoRespDTO[]> {
    return this.http.get<TipoProductoRespDTO[]>(this.tiposUrl);
  }

  /**
   * Crea un producto con multipart/form-data (backend: ProductoReqDTO + imagen).
   */
  createWithMultipart(data: ProductoReqDTO, imagen: File): Observable<Product> {
    const formData = this.buildProductFormData(data, imagen);
    return this.http.post<Product>(this.baseUrl, formData);
  }

  /**
   * Actualiza un producto con multipart/form-data (imagen opcional).
   */
  updateWithMultipart(id: number, data: ProductoReqDTO, imagen: File | null): Observable<Product> {
    const formData = this.buildProductFormData(data, imagen);
    return this.http.put<Product>(`${this.baseUrl}/${id}`, formData);
  }

  private buildProductFormData(data: ProductoReqDTO, imagen: File | null): FormData {
    const formData = new FormData();
    formData.append('nombre', data.nombre);
    formData.append('descripcion', data.descripcion ?? '');
    formData.append('precio', String(data.precio));
    formData.append('stock', String(data.stock));
    formData.append('sku', data.sku);
    formData.append('imagenUrl', data.imagenUrl ?? '');
    formData.append('categoriaId', String(data.categoriaId));
    formData.append('tipoProductoId', String(data.tipoProductoId));
    if (imagen) {
      formData.append('imagen', imagen);
    }
    return formData;
  }

  delete(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
