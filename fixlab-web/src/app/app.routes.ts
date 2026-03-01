import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { ProductListComponent } from './components/product-list/product-list';
import { Dashboard } from './components/dashboard/dashboard';
import { authGuard } from './guards/auth-guard';
import { HomeComponent } from './components/home/home';

export const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'login', component: Login },
  { path: 'productos', component: ProductListComponent },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: '', redirectTo: '/home', pathMatch: 'full' }, // Redirigir al home por defecto
];