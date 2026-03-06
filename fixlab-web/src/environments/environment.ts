import type { Environment } from './environment.model';

/**
 * Configuración para desarrollo (ng serve / build --configuration=development).
 * En producción se sustituye por environment.prod.ts (fileReplacements en angular.json).
 */
export const environment: Environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8081',
  useImageUpload: false,
  /** Clave de sitio reCAPTCHA v2. En desarrollo puedes usar la de prueba: 6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI */
  recaptchaSiteKey: '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI',
};
