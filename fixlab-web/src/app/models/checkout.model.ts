/** Item del carrito para el backend (POST /api/ventas/checkout) */
export interface ItemCarritoDTO {
  productoId: number;
  cantidad: number;
}

export interface CheckoutReqDTO {
  direccionEnvio: string;
  items: ItemCarritoDTO[];
}

export interface CheckoutRespDTO {
  pedidoId: number;
  urlPago: string;
}
