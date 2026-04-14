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
  /** Backend local. Coincide con server.port=8081 del API. */
  /**apiBaseUrl: 'https://api.villadastudios.com',*/
  apiBaseUrl: 'https://api.fixlabcol.com',
  /**apiBaseUrl: 'http://localhost:8081',*/
  useImageUpload: false,
  /** Producción: mismo host que el front. En local con Wompi, pon aquí la URL del túnel ngrok al front (4200). */
  appBaseUrlForWompi: 'https://app.fixlabcol.com',
  enablePostventaModule: true,
};
