import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { ProductListComponent } from './components/product-list/product-list';
import { Dashboard } from './components/dashboard/dashboard';
import { authGuard } from './guards/auth-guard';
import { adminGuard } from './guards/admin-guard';
import { HomeComponent } from './components/home/home';
import { RegisterComponent } from './components/register/register';
import { AdminRedirectComponent } from './components/admin-redirect/admin-redirect';
import { CarritoComponent } from './components/carrito/carrito';
import { AdminProductosComponent } from './components/admin-productos/admin-productos';
import { AdminCategoriasComponent } from './components/admin-categorias/admin-categorias';
import { AdminTiposProductoComponent } from './components/admin-tipos-producto/admin-tipos-producto';
import { AdminUsuariosComponent } from './components/admin-usuarios/admin-usuarios';
import { AdminPedidosComponent } from './components/admin-pedidos/admin-pedidos';
import { PagoExitosoComponent } from './components/pago-exitoso/pago-exitoso';
import { FacturaComponent } from './components/factura/factura';
import { RecuperarPasswordComponent } from './components/recuperar-password/recuperar-password';
import { RestablecerPasswordComponent } from './components/restablecer-password/restablecer-password';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'login', component: Login },
  { path: 'recuperar-password', component: RecuperarPasswordComponent },
  { path: 'reset-password', component: RestablecerPasswordComponent },
  { path: 'productos', component: ProductListComponent },
  { path: 'carrito', component: CarritoComponent },
  { path: 'pago-exitoso', component: PagoExitosoComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'factura/:id', component: FacturaComponent, canActivate: [authGuard] },
  { path: 'admin', component: AdminRedirectComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/productos', component: AdminProductosComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/pedidos', component: AdminPedidosComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/categorias', component: AdminCategoriasComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/tipos-producto', component: AdminTiposProductoComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/usuarios', component: AdminUsuariosComponent, canActivate: [authGuard, adminGuard] },
  { path: '**', redirectTo: '/home', pathMatch: 'full' },
];