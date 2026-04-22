export interface InformeMeta {
  desde: string;
  hasta: string;
  generadoEn: string;
}

export interface MovimientosStockReporte {
  meta: InformeMeta;
  lineas: Array<{
    id: number;
    fechaRegistro: string;
    productoId: number;
    sku: string;
    nombreProducto: string;
    cantidad: number;
    comentario?: string;
  }>;
}

export interface ProductosSinVentasReporte {
  meta: InformeMeta;
  totalProductosActivosSinVentas: number;
  lineas: Array<{
    productoId: number;
    sku: string;
    nombre: string;
    categoriaNombre: string;
    tipoProductoNombre: string;
    stock: number;
    activo: boolean;
  }>;
}

export interface RotacionProductosReporte {
  meta: InformeMeta;
  lineas: Array<{
    productoId: number;
    sku: string;
    nombre: string;
    categoriaNombre: string;
    unidadesVendidasPeriodo: number;
    stockActual: number;
    indiceRotacion: number;
  }>;
}

export interface VentasResumenReporte {
  meta: InformeMeta;
  totalPedidosPeriodo: number;
  pedidosPagados: number;
  pedidosEntregados: number;
  pedidosCancelados: number;
  pedidosOtrosEstados: number;
  totalMontoPedidosPagados: number;
  totalMontoPedidosEntregados: number;
  totalMontoTodosEstados: number;
  ticketPromedioPagados: number | null;
  pedidosPorEstado: Record<string, number>;
}

export interface VentasPorCategoriaReporte {
  meta: InformeMeta;
  montoTotalPagado: number;
  lineas: Array<{
    categoriaId: number;
    categoriaNombre: string;
    unidadesVendidas: number;
    montoTotal: number;
    participacionPorcentaje: number;
  }>;
}

export interface TopProductosReporte {
  meta: InformeMeta;
  limite: number;
  lineas: Array<{
    productoId: number;
    sku: string;
    nombre: string;
    categoriaNombre: string;
    tipoProductoNombre: string;
    unidadesVendidas: number;
    montoTotal: number;
  }>;
}

export interface PedidosLogisticaReporte {
  generadoEn: string;
  totalCoincidencias: number;
  lineas: Array<{
    pedidoId: number;
    fechaCreacion: string;
    estado: string;
    total: number;
    clienteCedula: string;
    clienteNombre: string;
    direccionEnvio?: string;
  }>;
}

export interface ReparacionesPorEstadoReporte {
  meta: InformeMeta;
  totalReparacionesPeriodo: number;
  lineas: Array<{ estado: string; cantidad: number }>;
}

export interface ReparacionesPorTecnicoReporte {
  meta: InformeMeta;
  lineas: Array<{
    tecnicoCedula: string;
    tecnicoNombre: string;
    reparacionesEntregadas: number;
    reparacionesActivasOtras: number;
  }>;
}

export interface RepuestosTallerReporte {
  meta: InformeMeta;
  lineas: Array<{
    productoId: number;
    sku: string;
    nombreProducto: string;
    unidadesUsadas: number;
    valorTotal: number;
  }>;
}

export interface PqrsResumenReporte {
  meta: InformeMeta;
  totalSolicitudes: number;
  diasPromedioHastaCierre: number | null;
  porTipo: Array<{ tipo: string; cantidad: number }>;
  porEstado: Array<{ estado: string; cantidad: number }>;
}

export interface GarantiasServicioReporte {
  generadoEn: string;
  diasVentana: number;
  vencidas: number;
  proximasAVencer: number;
  lineas: Array<{
    reparacionId: number;
    numeroTicket: string;
    clienteNombre: string;
    fechaFinGarantia: string;
    situacion: string;
  }>;
}

export interface ClientesComprasReporte {
  meta: InformeMeta;
  clientesDistintosConCompra: number;
  topClientes: Array<{
    clienteCedula: string;
    clienteNombre: string;
    pedidosPagados: number;
    montoTotal: number;
  }>;
}

export interface UsuariosPorRolReporte {
  generadoEn: string;
  totalUsuarios: number;
  lineas: Array<{ rol: string; cantidadUsuarios: number; activos: number }>;
}

export interface FinancieroSnapshotReporte {
  meta: InformeMeta;
  generadoEn: string;
  valorInventarioActivos: number;
  ventasPagadasPeriodo: number;
  ventasEntregadasPeriodo: number;
  pedidosPagadosPeriodo: number;
}
