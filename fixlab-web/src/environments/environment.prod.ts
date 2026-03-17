import type { Environment } from './environment.model';

/**
 * Configuración para producción (ng build usa este archivo por fileReplacements).
 * Backend desplegado en 34.75.187.247 (puerto 8081).
 */
export const environment: Environment = {
  production: true,
  /** API desplegada en la IP del servidor. */
  apiBaseUrl: 'http://34.75.187.247:8081',
  useImageUpload: false,
  /** Sustituir por tu clave de sitio reCAPTCHA v2 de producción */
  recaptchaSiteKey: '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI',
  /**
   * URL donde los usuarios acceden al frontend.
   * Necesaria para que Wompi redirija tras el pago a /pago-exitoso.
   */
  appBaseUrlForWompi: 'https://fixlab.villadastudios.com',
};
