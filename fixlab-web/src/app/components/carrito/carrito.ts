import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth';
import { CheckoutService } from '../../services/checkout.service';
import { CheckoutReqDTO } from '../../models/checkout.model';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, RouterLink, DecimalPipe, FormsModule],
  templateUrl: './carrito.html',
  styleUrl: './carrito.css'
})
export class CarritoComponent {
  cartService = inject(CartService);
  authService = inject(AuthService);
  private checkoutService = inject(CheckoutService);

  direccionEnvio = '';
  showDireccionModal = signal(false);
  loadingPago = signal(false);
  errorPago = signal<string | null>(null);

  constructor() {
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
    const conId = items.filter(i => i.product.id != null);
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

    const items = this.cartService.cartItems()
      .filter(i => i.product.id != null)
      .map(i => ({ productoId: i.product.id!, cantidad: i.quantity }));

    const body: CheckoutReqDTO = {
      direccionEnvio: dir,
      items
    };

    this.loadingPago.set(true);
    this.errorPago.set(null);

    this.checkoutService.checkout(body).subscribe({
      next: (res) => {
        this.cartService.clear();
        this.showDireccionModal.set(false);
        this.loadingPago.set(false);
        window.location.href = res.urlPago;
      },
      error: (err) => {
        this.loadingPago.set(false);
        this.errorPago.set(
          err.error?.mensaje || err.message || 'Error al crear el pago. Intenta de nuevo.'
        );
      }
    });
  }
}
