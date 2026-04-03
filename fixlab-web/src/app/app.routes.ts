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
import { TermsAndConditionsComponent } from './components/terms-and-conditions/terms-and-conditions';
import { PrivacyPolicyComponent } from './components/privacy-policy/privacy-policy';
import { ReparacionesPageComponent } from './components/reparaciones-page/reparaciones-page';
import { AdminReparacionesComponent } from './components/admin-reparaciones/admin-reparaciones';
import { AdminRecepcionComponent } from './components/admin-recepcion/admin-recepcion';
import { AdminTallerShellComponent } from './components/admin-taller-shell/admin-taller-shell';
import { AdminTallerListaComponent } from './components/admin-taller-lista/admin-taller-lista';
import { recepcionGuard } from './guards/recepcion-guard';
import { tallerShellGuard } from './guards/taller-shell-guard';
import { tallerRecepcionShellGuard } from './guards/taller-recepcion-shell-guard';
import { TallerDefaultRedirectComponent } from './components/taller-default-redirect/taller-default-redirect';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'login', component: Login },
  { path: 'recuperar-password', component: RecuperarPasswordComponent },
  { path: 'reset-password', redirectTo: 'recuperar-password', pathMatch: 'full' },
  { path: 'productos', component: ProductListComponent },
  { path: 'reparaciones', component: ReparacionesPageComponent },
  { path: 'carrito', component: CarritoComponent },
  { path: 'pago-exitoso', component: PagoExitosoComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'factura/:id', component: FacturaComponent, canActivate: [authGuard] },
  { path: 'admin', component: AdminRedirectComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/productos', component: AdminProductosComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/pedidos', component: AdminPedidosComponent, canActivate: [authGuard, adminGuard] },
  {
    path: 'admin/taller',
    component: AdminTallerShellComponent,
    canActivate: [authGuard, tallerShellGuard],
    children: [
      { path: '', pathMatch: 'full', component: TallerDefaultRedirectComponent },
      {
        path: 'recepcion',
        component: AdminRecepcionComponent,
        canActivate: [tallerRecepcionShellGuard],
      },
      { path: 'lista', component: AdminTallerListaComponent },
      { path: 'gestion', component: AdminReparacionesComponent },
      { path: 'seguimiento', component: ReparacionesPageComponent },
    ],
  },
  { path: 'admin/reparaciones', redirectTo: '/admin/taller/lista', pathMatch: 'full' },
  { path: 'admin/recepcion', component: AdminRecepcionComponent, canActivate: [authGuard, recepcionGuard] },
  { path: 'admin/categorias', component: AdminCategoriasComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/tipos-producto', component: AdminTiposProductoComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/usuarios', component: AdminUsuariosComponent, canActivate: [authGuard, adminGuard] },
  { path: 'terminos-y-condiciones', component: TermsAndConditionsComponent },
  { path: 'politica-de-privacidad', component: PrivacyPolicyComponent },
  { path: '**', redirectTo: '/home', pathMatch: 'full' },
];


