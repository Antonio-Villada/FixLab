import type { Environment } from './environment.model';

/**
 * Configuración para desarrollo (ng serve / build --configuration=development).
 * En producción se sustituye por environment.prod.ts (fileReplacements en angular.json).
 *
 * WOMPI CON NGROK: Necesitas DOS túneles:
 * 1) ngrok http 8081 → Backend (webhooks). Configurar esa URL en el dashboard de Wompi.
 * 2) ngrok http 4200 → Frontend. Poner esa URL aquí en appBaseUrlForWompi y abrir la app por ella.
 */
export const environment: Environment = {
  production: false,
  /** Backend: localhost para desarrollo local, o 34.75.187.247:8081 para probar contra el backend desplegado */
  apiBaseUrl: 'http://34.75.187.247:8081',
  useImageUpload: false,
  /** Clave de sitio reCAPTCHA v2. En desarrollo puedes usar la de prueba: 6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI */
  recaptchaSiteKey: '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI',
  /** URL del túnel ngrok al FRONTEND (4200). Ejecuta: ngrok http 4200. Pega aquí la URL (ej: https://abc123.ngrok-free.dev). Sin esto Wompi da 403/483. */
  appBaseUrlForWompi: undefined,
};
