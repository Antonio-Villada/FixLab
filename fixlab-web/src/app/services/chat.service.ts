import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ChatMensajeReqDTO, ChatRespuestaRespDTO } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl
    ? `${environment.apiBaseUrl.replace(/\/$/, '')}/api/chat`
    : '/api/chat';

  /** Envía un mensaje al backend (requiere JWT). */
  enviarMensaje(mensaje: string): Observable<ChatRespuestaRespDTO> {
    const body: ChatMensajeReqDTO = { mensaje: mensaje.trim() };
    return this.http.post<ChatRespuestaRespDTO>(`${this.baseUrl}/mensaje`, body);
  }
}
