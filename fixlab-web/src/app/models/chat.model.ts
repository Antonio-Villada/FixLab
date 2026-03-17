/** Request para enviar un mensaje al chat (POST /api/chat/mensaje). */
export interface ChatMensajeReqDTO {
  mensaje: string;
}

/** Respuesta del backend del chat (híbrido: reglas + IA). */
export interface ChatRespuestaRespDTO {
  respuesta: string;
  tipoAccion?: string;
  payload?: string;
}

/** Mensaje mostrado en la UI (usuario o bot). */
export interface ChatMessage {
  emisor: 'usuario' | 'bot';
  texto: string;
  tipoAccion?: string;
  payload?: string;
}
