import type { Environment } from './environment.model';

/**
 * Configuración para producción (ng build usa este archivo por fileReplacements).
 * Frontend: https://app.fixlabcol.com
 */
export const environment: Environment = {
  production: true,
  /**apiBaseUrl: 'https://api.villadastudios.com',*/
  apiBaseUrl: 'https://api.fixlabcol.com',
  useImageUpload: false,
  /**
   * URL donde los usuarios acceden al frontend.
   * Necesaria para que Wompi redirija tras el pago a /pago-exitoso.
   */
  appBaseUrlForWompi: 'https://app.fixlabcol.com',
  /** Cambia a true cuando quieras publicar PQRS/postventa en app.fixlabcol.com */
  enablePostventaModule: false,
};
