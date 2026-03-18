import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth';
import { CheckoutService } from '../../services/checkout.service';
import { CheckoutReqDTO } from '../../models/checkout.model';
import { environment } from '../../../environments/environment';
import { timeout, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, RouterLink, DecimalPipe, FormsModule],
  templateUrl: './carrito.html',
  styleUrl: './carrito.css',
})
export class CarritoComponent implements OnInit {
  cartService = inject(CartService);
  authService = inject(AuthService);
  private checkoutService = inject(CheckoutService);

  direccionEnvio = '';
  showDireccionModal = signal(false);
  loadingPago = signal(false);
  errorPago = signal<string | null>(null);

  ngOnInit(): void {
    this.authService.syncLoginStateFromStorage();
    if (!this.authService.isLoggedIn()) {
      this.cartService.clear();
    }
  }

  procederAPagar(): void {
    this.errorPago.set(null);
    if (!this.authService.isLoggedIn()) {
      this.errorPago.set('Inicia sesión para poder pagar.');
      return;
    }
    if (this.cartService.totalCount() === 0) {
      this.errorPago.set('El carrito está vacío.');
      return;
    }
    const items = this.cartService.cartItems();
    const conId = items.filter((i) => i.product.id != null);
    if (conId.length === 0) {
      this.errorPago.set('Los productos del carrito no tienen ID. Vuelve a añadirlos desde la tienda.');
      return;
    }
    if (conId.length < items.length) {
      this.errorPago.set('Algunos productos no se pueden enviar al pago. Quítalos y añádelos de nuevo desde Productos.');
      return;
    }
    this.direccionEnvio = '';
    this.showDireccionModal.set(true);
  }

  cancelarPago(): void {
    this.showDireccionModal.set(false);
    this.errorPago.set(null);
  }

  confirmarPago(): void {
    const dir = this.direccionEnvio?.trim();
    if (!dir) {
      this.errorPago.set('Ingresa la dirección de envío.');
      return;
    }

    const items = this.cartService
      .cartItems()
      .filter((i) => i.product.id != null)
      .map((i) => ({ productoId: i.product.id!, cantidad: i.quantity }));

    const body: CheckoutReqDTO = {
      direccionEnvio: dir,
      items,
    };

    this.loadingPago.set(true);
    this.errorPago.set(null);

    // Redirect tras el pago: Wompi rechaza localhost (error 483). Solo enviar si appBaseUrlForWompi está definido (URL de ngrok del frontend).
    const baseForRedirect = (typeof environment !== 'undefined' && environment?.appBaseUrlForWompi)
      ? environment.appBaseUrlForWompi.replace(/\/$/, '')
      : null;
    const redirectUrl = baseForRedirect ? `${baseForRedirect}/pago-exitoso` : undefined;

    this.checkoutService
      .crearPedido(body)
      .pipe(
        timeout(20000),
        finalize(() => this.loadingPago.set(false)),
      )
      .subscribe({
        next: (res) => {
          this.cartService.clear();
          this.showDireccionModal.set(false);
          this.checkoutService.openWompiCheckout(res, redirectUrl, () => {
            // Usuario cerró o completó el pago en el widget; el webhook confirmará en el backend
          });
        },
        error: (err) => {
          const isTimeout = err?.name === 'TimeoutError' || err?.message?.includes('timeout');
          this.errorPago.set(
            isTimeout
              ? 'El servidor no respondió a tiempo. Comprueba que la API esté en ejecución e inténtalo de nuevo.'
              : err?.error?.mensaje || err?.message || 'Error al crear el pedido. Intenta de nuevo.'
          );
        },
      });
  }
}
