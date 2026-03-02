// Definimos el Enum para que coincida exactamente con @Enumerated(EnumType.STRING)
export enum RolUsuario {
  CLIENTE = 'CLIENTE',
  ADMIN = 'ADMIN',
  TECNICO = 'TECNICO'
}

// DTO para el inicio de sesión
export interface LoginReqDTO {
  email: string;
  password: string;
}

// DTO para el registro (Basado en tu entidad Usuario)
export interface RegistroReqDTO {
  nombre: string;
  email: string;
  password: string;
  telefono: string;
  rol: RolUsuario; // Enviará la cadena "CLIENTE"
}

// Respuesta que contiene el JWT
export interface TokenRespDTO {
  token: string;
  // Si tu backend devuelve más info, agrégala aquí:
  // rol?: string;
  // nombre?: string;
}

// Respuesta genérica para mensajes (como el de registro exitoso)
export interface MensajeRespDTO {
  mensaje: string;
}