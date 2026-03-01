export interface LoginReqDTO {
  email: string;
  password: string;
}

export interface TokenRespDTO {
  token: string;
  // Añade aquí otros campos si tu DTO de Spring los tiene (ej: nombre, rol)
}

export interface MensajeRespDTO {
  mensaje: string;
}