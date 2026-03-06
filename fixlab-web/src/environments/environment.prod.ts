import type { Environment } from './environment.model';

/**
 * Configuración para producción (ng build --configuration=production).
 * apiBaseUrl vacío = peticiones al mismo host (ej. /api/auth).
 * Si tu API está en otro dominio, pon aquí la URL base (ej. https://api.miapp.com).
 */
export const environment: Environment = {
  production: true,
  apiBaseUrl: '',
  useImageUpload: false,
  /** Sustituir por tu clave de sitio reCAPTCHA v2 de producción */
  recaptchaSiteKey: '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI',
};
