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
  /** Umbral de alerta; si el backend no lo envía, la UI usa 5. */
  stockMinimo?: number;
  imagenUrl?: string;
  activo?: boolean;
  categoria?: CategoriaRespDTO;
  tipoProducto?: TipoProductoRespDTO;
}

/** Payload para crear/actualizar producto (ProductoReqDTO + ids). */
export interface ProductoReqDTO {
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  stockMinimo?: number;
  sku: string;
  imagenUrl?: string;
  categoriaId: number;
  tipoProductoId: number;
}

export interface EntradaMercanciaReqDTO {
  cantidad: number;
  comentario?: string;
}

export interface EntradaMercanciaRespDTO {
  id: number;
  productoId: number;
  sku: string;
  nombreProducto: string;
  cantidad: number;
  nuevoStock?: number;
  comentario?: string;
  fechaRegistro: string;
}
