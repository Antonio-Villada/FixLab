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

export const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'login', component: Login },
  { path: 'productos', component: ProductListComponent },
  { path: 'carrito', component: CarritoComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'admin', component: AdminRedirectComponent, canActivate: [authGuard, adminGuard] },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
];