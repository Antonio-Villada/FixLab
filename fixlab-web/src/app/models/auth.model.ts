// Definimos el Enum para que coincida exactamente con @Enumerated(EnumType.STRING)
export enum RolUsuario {
  CLIENTE = 'CLIENTE',
  RECEPCIONISTA = 'RECEPCIONISTA',
  TECNICO = 'TECNICO',
  ADMIN = 'ADMIN',
}

// DTO para el inicio de sesión
export interface LoginReqDTO {
  email: string;
  password: string;
}

// DTO para el registro de clientes (alineado con backend POST /api/auth/registro)
export interface RegistroReqDTO {
  cedula: string;
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  telefono: string;
}

// Respuesta que contiene el JWT (y opcionalmente el rol para redirigir por tipo de usuario)
export interface TokenRespDTO {
  token: string;
  /** Rol del usuario: CLIENTE | RECEPCIONISTA | TECNICO | ADMIN. */
  rol?: string;
}

/** Primer paso del login: se envió el código al correo. */
export interface LoginPaso1RespDTO {
  paso: 'CODIGO_ENVIADO';
  emailMascarado: string;
}

export interface LoginVerificarCodigoReqDTO {
  email: string;
  codigo: string;
}

// Respuesta genérica para mensajes (como el de registro exitoso)
export interface MensajeRespDTO {
  mensaje: string;
}

// Usuario (GET /api/usuarios)
export interface UsuarioRespDTO {
  cedula: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string | null;
  rol: RolUsuario;
  correoVerificado: boolean;
  /** URL de foto de perfil (opcional; si no hay, el header muestra iniciales). */
  fotoUrl?: string | null;
}

/** Sugerencias de autocompletado en recepción (solo clientes). */
export interface ClienteSugerenciaRespDTO {
  cedula: string;
  nombre: string;
  apellido: string;
}

/** Personal que puede asignarse como técnico de una orden (técnico o administrador). */
export interface StaffTallerAsignableRespDTO {
  cedula: string;
  nombre: string;
  apellido: string;
  rol: RolUsuario;
}

// Actualizar usuario (PUT /api/usuarios/{cedula})
export interface UsuarioUpdateReqDTO {
  nombre: string;
  apellido: string;
  telefono: string;
}

// Crear empleado (POST /api/auth/registro-empleado) - solo ADMIN
export interface RegistroEmpleadoReqDTO {
  cedula: string;
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  telefono: string;
  rol: RolUsuario;
}

// Cambiar rol (PUT /api/auth/cambiar-rol) - solo ADMIN
export interface CambioRolReqDTO {
  cedula: string;
  nuevoRol: RolUsuario;
}

// Verificar correo con código de 6 dígitos (POST /api/auth/verificar-correo)
export interface VerificarCorreoReqDTO {
  email: string;
  codigo: string;
}

// Solicitar recuperación de contraseña (POST /api/auth/recuperar-password)
export interface SolicitarRecuperacionDTO {
  email: string;
}

// Verificar código de recuperación (POST /api/auth/verificar-codigo-recuperacion)
export interface VerificarCodigoRecuperacionReqDTO {
  email: string;
  codigo: string;
}

export interface TokenRecuperacionRespDTO {
  token: string;
}

// Restablecer contraseña con token (POST /api/auth/reset-password)
export interface ResetearPasswordDTO {
  token: string;
  nuevaPassword: string;
}

// Cambiar contraseña (usuario logueado, POST /api/auth/cambiar-password)
export interface CambiarPasswordReqDTO {
  contraseñaActual: string;
  nuevaPassword: string;
}

/** Eliminación de cuenta cliente (POST /api/usuarios/me/eliminar-cuenta). */
export interface EliminarCuentaClienteReqDTO {
  password: string;
}

// Asignar nueva contraseña a un usuario (solo ADMIN, POST /api/auth/admin/asignar-password)
export interface AdminAsignarPasswordReqDTO {
  cedula: string;
  nuevaPassword: string;
}