import type { Environment } from './environment.model';

/**
 * Configuración para desarrollo / pruebas locales (ng serve usa este archivo).
 * En producción, el build usa environment.prod.ts (fileReplacements en angular.json).
 *
 * LOCAL: apiBaseUrl apunta al backend en tu máquina (localhost:8081).
 * WOMPI: Para probar pagos en local necesitas túneles ngrok y abrir la app por appBaseUrlForWompi.
 */
export const environment: Environment = {
  production: false,
  /** Backend: local (localhost:8081) para pruebas. En prod/build usar environment.prod.ts. */
  apiBaseUrl: 'http://localhost:8081',
  useImageUpload: false,
  /** Clave de sitio reCAPTCHA v2. En desarrollo puedes usar la de prueba: 6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI */
  recaptchaSiteKey: '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI',
  /** URL del túnel ngrok al FRONTEND (4200). Ejecuta: ngrok http 4200. Pega aquí la URL (ej: https://abc123.ngrok-free.dev). Sin esto Wompi da 403/483. */
  appBaseUrlForWompi: 'https://fixlab.villadastudios.com',
};
