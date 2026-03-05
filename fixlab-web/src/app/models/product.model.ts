/**
 * Modelo alineado con la entidad Producto del backend (JPA).
 */
export interface Product {
  id?: number;
  sku: string;
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  imagenUrl?: string;
  activo: boolean;
}
