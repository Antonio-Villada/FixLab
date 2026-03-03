import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * Rutas estáticas (prerender): no dependen del API.
 * El resto (productos, admin, dashboard) se renderizan en el servidor bajo demanda
 * para evitar llamadas al backend durante el build.
 */
export const serverRoutes: ServerRoute[] = [
  { path: '', renderMode: RenderMode.Prerender },
  { path: 'home', renderMode: RenderMode.Prerender },
  { path: 'login', renderMode: RenderMode.Prerender },
  { path: 'register', renderMode: RenderMode.Prerender },
  { path: 'productos', renderMode: RenderMode.Server },
  { path: 'carrito', renderMode: RenderMode.Server },
  { path: 'dashboard', renderMode: RenderMode.Server },
  { path: 'admin/**', renderMode: RenderMode.Server },
];
