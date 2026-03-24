/**
 * Interfaz compartida para la configuración por entorno.
 */
export interface Environment {
  production: boolean;
  /** URL base del backend (sin barra final). Vacío = mismo origen. */
  apiBaseUrl: string;
  /** Si true, al crear/editar producto se sube la imagen a POST /api/upload. Si false, solo se usa el campo URL. */
  useImageUpload?: boolean;
  /**
   * URL base del frontend para redirect de Wompi (p. ej. https://tu-tunel.ngrok.io).
   * Úsala cuando pruebes pagos en local: abre la app por esta URL para evitar 403 de Wompi.
   */
  appBaseUrlForWompi?: string;
}
