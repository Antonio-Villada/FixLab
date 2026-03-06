/**
 * Interfaz compartida para la configuración por entorno.
 */
export interface Environment {
  production: boolean;
  /** URL base del backend (sin barra final). Vacío = mismo origen. */
  apiBaseUrl: string;
  /** Si true, al crear/editar producto se sube la imagen a POST /api/upload. Si false, solo se usa el campo URL. */
  useImageUpload?: boolean;
  /** Clave del sitio de Google reCAPTCHA v2 (checkbox). Obtener en https://www.google.com/recaptcha/admin */
  recaptchaSiteKey?: string;
}
