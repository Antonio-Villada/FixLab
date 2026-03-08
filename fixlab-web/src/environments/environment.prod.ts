import type { Environment } from './environment.model';

/**
 * Configuración para producción (ng build --configuration=production).
 */
export const environment: Environment = {
  production: true,
  apiBaseUrl: 'http://34.75.187.247:8081',
  useImageUpload: false,
  /** Sustituir por tu clave de sitio reCAPTCHA v2 de producción */
  recaptchaSiteKey: '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI',
  /**
   * URL donde los usuarios acceden al frontend (ej. http://34.75.187.247 o https://tu-dominio.com).
   * Necesaria para que Wompi redirija tras el pago a /pago-exitoso.
   * Si el frontend está en otro puerto, incluir: http://34.75.187.247:PUERTO
   */
  appBaseUrlForWompi: 'http://34.75.187.247',
};
