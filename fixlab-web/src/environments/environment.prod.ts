import type { Environment } from './environment.model';

/**
 * Configuración para producción (ng build usa este archivo por fileReplacements).
 * Frontend desplegado en https://fixlab.villadastudios.com → API en https://api.villadastudios.com
 */
export const environment: Environment = {
  production: true,
  /** API desplegada (mismo dominio o IP del backend en producción). */
  apiBaseUrl: 'https://api.villadastudios.com',
  useImageUpload: false,
  /**
   * URL donde los usuarios acceden al frontend.
   * Necesaria para que Wompi redirija tras el pago a /pago-exitoso.
   */
  appBaseUrlForWompi: 'https://fixlab.villadastudios.com',
};
