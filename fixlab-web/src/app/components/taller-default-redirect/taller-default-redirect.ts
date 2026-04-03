import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

/** Redirige el path vacío de /admin/taller: admin → recepción, técnico → lista. */
@Component({
  selector: 'app-taller-default-redirect',
  standalone: true,
  template: '',
})
export class TallerDefaultRedirectComponent implements OnInit {
  private router = inject(Router);
  private auth = inject(AuthService);

  ngOnInit(): void {
    const dest = this.auth.isAdmin() ? '/admin/taller/recepcion' : '/admin/taller/lista';
    this.router.navigateByUrl(dest, { replaceUrl: true });
  }
}
