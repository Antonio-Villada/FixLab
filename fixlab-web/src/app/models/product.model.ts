/**
 * Modelo alineado con ProductoRespDTO del backend.
 */
export interface CategoriaRespDTO {
  id: number;
  nombre: string;
}

export interface TipoProductoRespDTO {
  id: number;
  nombre: string;
}

export interface Product {
  id?: number;
  sku: string;
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  imagenUrl?: string;
  activo?: boolean;
  /** Presente cuando se listan con filtro "más vendidos". */
  cantidadVendida?: number;
  categoria?: CategoriaRespDTO;
  tipoProducto?: TipoProductoRespDTO;
}

/** Payload para crear/actualizar producto (ProductoReqDTO + ids). */
export interface ProductoReqDTO {
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  sku: string;
  imagenUrl?: string;
  categoriaId: number;
  tipoProductoId: number;
}
