import type { Environment } from './environment.model';

/**
 * Configuración para desarrollo (ng serve / build --configuration=development).
 * En producción se sustituye por environment.prod.ts (fileReplacements en angular.json).
 */
export const environment: Environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8081',
  /** Activa cuando el backend tenga POST /api/upload que devuelva { url: string } */
  useImageUpload: false,
};
