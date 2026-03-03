import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CheckoutReqDTO, CheckoutRespDTO } from '../models/checkout.model';

@Injectable({
  providedIn: 'root'
})
export class CheckoutService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl
    ? `${environment.apiBaseUrl.replace(/\/$/, '')}/api/ventas`
    : '/api/ventas';

  /**
   * Crea el pedido en el backend y obtiene la URL de pago de Mercado Pago.
   * Requiere usuario autenticado (CLIENTE o ADMIN). El token se envía vía AuthInterceptor.
   */
  checkout(body: CheckoutReqDTO): Observable<CheckoutRespDTO> {
    return this.http.post<CheckoutRespDTO>(`${this.baseUrl}/checkout`, body);
  }
}
