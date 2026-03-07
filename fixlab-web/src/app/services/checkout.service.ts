import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CheckoutReqDTO, WompiCheckoutDTO, PedidoRespDTO } from '../models/checkout.model';

const WOMPI_CHECKOUT_BASE = 'https://checkout.wompi.co/p/';
const WOMPI_WIDGET_SCRIPT = 'https://checkout.wompi.co/widget.js';

declare global {
  interface Window {
    WidgetCheckout?: new (config: WompiWidgetConfig) => { open: (callback?: (result: unknown) => void) => void };
  }
}

interface WompiWidgetConfig {
  currency: string;
  amountInCents: number;
  reference: string;
  publicKey: string;
  signature: { integrity: string };
  redirectUrl?: string;
}

@Injectable({
  providedIn: 'root',
})
export class CheckoutService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl
    ? `${environment.apiBaseUrl.replace(/\/$/, '')}/api/pedidos`
    : '/api/pedidos';

  crearPedido(body: CheckoutReqDTO): Observable<WompiCheckoutDTO> {
    return this.http.post<WompiCheckoutDTO>(this.baseUrl, body);
  }

  /** Lista de pedidos del usuario actual. */
  getMisPedidos(): Observable<PedidoRespDTO[]> {
    return this.http.get<PedidoRespDTO[]>(`${this.baseUrl}/mis-pedidos`);
  }

  /** Todos los pedidos (solo ADMIN). */
  getTodosPedidos(): Observable<PedidoRespDTO[]> {
    return this.http.get<PedidoRespDTO[]>(this.baseUrl);
  }

  /** Obtener un pedido por ID (cliente: solo los suyos; admin: cualquiera). */
  getPedidoPorId(id: number): Observable<PedidoRespDTO> {
    return this.http.get<PedidoRespDTO>(`${this.baseUrl}/${id}`);
  }

  /**
   * Construye la URL de pago de Wompi (por si se usa redirección).
   * Parámetro con nombre literal "signature:integrity" para evitar 403 por encoding.
   */
  buildWompiPaymentUrl(data: WompiCheckoutDTO, redirectUrl?: string): string {
    const p: string[] = [
      `public-key=${encodeURIComponent(data.llavePublica)}`,
      `currency=${encodeURIComponent(data.moneda)}`,
      `amount-in-cents=${encodeURIComponent(String(data.montoEnCentavos))}`,
      `reference=${encodeURIComponent(data.referencia)}`,
      `signature:integrity=${encodeURIComponent(data.firmaIntegridad)}`,
    ];
    if (redirectUrl) {
      p.push(`redirect-url=${encodeURIComponent(redirectUrl)}`);
    }
    return `${WOMPI_CHECKOUT_BASE}?${p.join('&')}`;
  }

  /**
   * Abre el checkout de Wompi con el Widget (sin redirección), evitando 403 de CloudFront.
   * Si el widget no está disponible, redirige con la URL.
   */
  openWompiCheckout(
    data: WompiCheckoutDTO,
    redirectUrl?: string,
    onResult?: (result: unknown) => void
  ): void {
    const openWidget = (): void => {
      if (typeof window !== 'undefined' && window.WidgetCheckout) {
        const config: WompiWidgetConfig = {
          currency: data.moneda,
          amountInCents: data.montoEnCentavos,
          reference: data.referencia,
          publicKey: data.llavePublica,
          signature: { integrity: data.firmaIntegridad },
        };
        if (redirectUrl) config.redirectUrl = redirectUrl;
        const checkout = new window.WidgetCheckout(config);
        checkout.open(onResult);
      } else {
        window.location.href = this.buildWompiPaymentUrl(data, redirectUrl);
      }
    };

    if (typeof document === 'undefined') {
      window.location.href = this.buildWompiPaymentUrl(data, redirectUrl);
      return;
    }

    const existing = document.querySelector(`script[src="${WOMPI_WIDGET_SCRIPT}"]`);
    if (existing) {
      openWidget();
      return;
    }

    const script = document.createElement('script');
    script.src = WOMPI_WIDGET_SCRIPT;
    script.async = true;
    script.onload = () => openWidget();
    script.onerror = () => {
      this.submitWompiForm(data, redirectUrl);
    };
    document.head.appendChild(script);
  }

  /**
   * Envía un formulario GET a Wompi (alternativa si el widget falla).
   * Abre la URL de checkout en la misma ventana.
   */
  submitWompiForm(data: WompiCheckoutDTO, redirectUrl?: string): void {
    const url = this.buildWompiPaymentUrl(data, redirectUrl);
    const form = document.createElement('form');
    form.method = 'GET';
    form.action = WOMPI_CHECKOUT_BASE;
    form.target = '_self';
    const params = new URLSearchParams(url.includes('?') ? url.split('?')[1] : '');
    params.forEach((value, key) => {
      const input = document.createElement('input');
      input.type = 'hidden';
      input.name = key;
      input.value = value;
      form.appendChild(input);
    });
    document.body.appendChild(form);
    form.submit();
    form.remove();
  }
}
