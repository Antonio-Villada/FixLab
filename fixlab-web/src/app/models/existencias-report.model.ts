export interface ExistenciasResumen {
  fechaGeneracion: string;
  totalProductos: number;
  productosActivos: number;
  productosInactivos: number;
  totalUnidadesStock: number;
  totalUnidadesStockActivos: number;
  productosConStockBajo: number;
  valorInventarioActivos: number;
}

export interface ExistenciasLinea {
  sku: string;
  nombre: string;
  categoriaNombre: string;
  tipoProductoNombre: string;
  activo: boolean;
  stock: number;
  stockMinimo: number;
  precioUnitario: number;
  valorExistencia: number;
  stockBajo: boolean;
  estadoExistencia: string;
}

export interface ExistenciasReporte {
  resumen: ExistenciasResumen;
  lineas: ExistenciasLinea[];
}
