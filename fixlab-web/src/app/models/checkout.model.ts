/** Item del carrito para el backend (POST /api/pedidos) */
export interface ItemCarritoDTO {
  productoId: number;
  cantidad: number;
}

/** Body para crear pedido (PedidoReqDTO). */
export interface CheckoutReqDTO {
  direccionEnvio: string;
  items: ItemCarritoDTO[];
}

/** Respuesta del backend al crear pedido: datos para redirigir a Wompi. */
export interface WompiCheckoutDTO {
  pedidoId: number;
  referencia: string;
  montoEnCentavos: number;
  moneda: string;
  firmaIntegridad: string;
  llavePublica: string;
}

/** Respuesta antigua (solo por compatibilidad; el backend devuelve WompiCheckoutDTO). */
export interface CheckoutRespDTO {
  pedidoId: number;
  urlPago?: string;
}

/** Detalle de un ítem en un pedido (GET /api/pedidos/mis-pedidos). */
export interface DetallePedidoRespDTO {
  productoId: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

/** Pedido del usuario (GET /api/pedidos/mis-pedidos). */
export interface PedidoRespDTO {
  id: number;
  fechaCreacion: string;
  total: number;
  estado: string;
  clienteCedula?: string;
  clienteNombre?: string;
  direccionEnvio?: string;
  detalles?: DetallePedidoRespDTO[];
}
