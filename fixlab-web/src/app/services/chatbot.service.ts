import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../environments/environment';

export type ChatRole = 'user' | 'bot';

export interface ChatMessage {
  id: string;
  role: ChatRole;
  text: string;
  at: number;
}

interface ChatApiMensajeDTO {
  id: number;
  role: 'USER' | 'BOT';
  text: string;
  createdAt: string;
}

interface ChatEnviarRespDTO {
  userMessage: ChatApiMensajeDTO;
  botMessage: ChatApiMensajeDTO;
}

/** Misma clave que AuthService para saber si hay sesión sin dependencia circular */
const TOKEN_KEY = 'fixlab_auth_token';

/**
 * Usuario con sesión: historial en servidor (por email / JWT).
 * Invitado: solo en memoria, misma lógica híbrida local.
 */
@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);

  private readonly baseUrl = environment.apiBaseUrl
    ? `${environment.apiBaseUrl.replace(/\/$/, '')}/api/chat`
    : '/api/chat';

  private readonly messages = signal<ChatMessage[]>([]);
  readonly messagesSignal = this.messages.asReadonly();
  readonly loadingHistorial = signal(false);
  readonly enviando = signal(false);

  private hasToken(): boolean {
    return isPlatformBrowser(this.platformId) && !!localStorage.getItem(TOKEN_KEY);
  }

  /** Tras login o al cargar la app con token: recupera mensajes del usuario. */
  loadHistorial(): void {
    if (!this.hasToken()) {
      return;
    }
    this.loadingHistorial.set(true);
    this.http.get<ChatApiMensajeDTO[]>(`${this.baseUrl}/historial`).subscribe({
      next: (rows) => {
        this.messages.set(rows.map((r) => this.mapApiToMessage(r)));
        this.loadingHistorial.set(false);
      },
      error: () => {
        this.loadingHistorial.set(false);
      },
    });
  }

  /** Cierra vista local (logout). No borra el historial en BD. */
  resetConversation(): void {
    this.messages.set([]);
  }

  /**
   * Nuevo chat: invitado solo limpia memoria; usuario logueado borra historial en servidor.
   */
  limpiarConversacion(): void {
    if (!this.hasToken()) {
      this.messages.set([]);
      return;
    }
    this.http.delete<void>(`${this.baseUrl}/historial`).subscribe({
      next: () => this.messages.set([]),
      error: () => this.messages.set([]),
    });
  }

  getWelcomeMessage(isLoggedIn: boolean): string {
    if (isLoggedIn) {
      return '¡Hola! Soy el asistente de FixLab. Aquí ves tu historial de consultas guardado en tu cuenta. Usa + para empezar un chat nuevo.';
    }
    return '¡Hola! Inicia sesión para guardar tu historial de consultas. Como invitado, el chat no se guarda al cerrar la página.';
  }

  sendUserMessage(text: string): void {
    const trimmed = text.trim();
    if (!trimmed) return;

    if (this.hasToken()) {
      this.enviando.set(true);
      this.http.post<ChatEnviarRespDTO>(`${this.baseUrl}/mensaje`, { texto: trimmed }).subscribe({
        next: (res) => {
          this.messages.update((list) => [
            ...list,
            this.mapApiToMessage(res.userMessage),
            this.mapApiToMessage(res.botMessage),
          ]);
          this.enviando.set(false);
        },
        error: () => {
          this.enviando.set(false);
          this.pushLocalUserAndBot(trimmed);
        },
      });
      return;
    }

    this.pushLocalUserAndBot(trimmed);
  }

  private mapApiToMessage(d: ChatApiMensajeDTO): ChatMessage {
    return {
      id: `srv-${d.id}`,
      role: d.role === 'USER' ? 'user' : 'bot',
      text: d.text,
      at: new Date(d.createdAt).getTime(),
    };
  }

  private pushLocalUserAndBot(trimmed: string): void {
    this.pushMessage({ role: 'user', text: trimmed });
    const reply = this.hybridReply(trimmed);
    window.setTimeout(() => this.pushMessage({ role: 'bot', text: reply }), 280);
  }

  private pushMessage(partial: Omit<ChatMessage, 'id' | 'at'>): void {
    const msg: ChatMessage = {
      ...partial,
      id: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
      at: Date.now(),
    };
    this.messages.update((list) => [...list, msg]);
  }

  private normalize(s: string): string {
    return s
      .toLowerCase()
      .normalize('NFD')
      .replace(/\p{M}/gu, '');
  }

  private hybridReply(userText: string): string {
    const n = this.normalize(userText);

    if (/\b(hola|buenas|hey|hi)\b/.test(n)) {
      return '¡Hola! Pregúntame por pedidos, pagos (Wompi), envíos, productos o cuenta y contraseña.';
    }
    if (/\b(gracias|thank)\b/.test(n)) {
      return '¡Con gusto! Si necesitas algo más, aquí estaré.';
    }

    if (/(pedido|orden|compra realizada|estado del pedido|mis compras)/.test(n)) {
      return 'Para ver tus pedidos, inicia sesión y entra al panel (dashboard). Los administradores pueden gestionar pedidos desde Admin → Pedidos.';
    }

    if (/(pago|pagar|wompi|tarjeta|checkout|transaccion)/.test(n)) {
      return 'Los pagos se procesan con Wompi. Añade productos al carrito, revisa el total y completa el pago en la pasarela. Si algo falla, revisa el correo o intenta de nuevo.';
    }

    if (/(envio|envío|domicilio|entrega|recibir)/.test(n)) {
      return 'Los tiempos y costos de envío dependen de tu zona y del pedido. Tras pagar, podrás ver el estado del pedido en tu cuenta.';
    }

    if (/(producto|catalogo|catálogo|comprar|tienda)/.test(n)) {
      return 'Explora el catálogo en Productos desde el menú. Ahí puedes filtrar y añadir al carrito.';
    }

    if (/(cuenta|registro|registrarme|contraseña|password|clave|correo verificado)/.test(n)) {
      return 'Puedes registrarte o iniciar sesión desde el menú. Si olvidaste la contraseña, usa Recuperar contraseña. El inicio de sesión puede pedirte un código por correo.';
    }

    if (/(devolucion|devolución|reembolso)/.test(n)) {
      return 'Para devoluciones o cambios, contacta al equipo de FixLab con tu número de pedido. Un administrador podrá orientarte según la política de la tienda.';
    }

    if (/(horario|contacto|soporte|ayuda humana|hablar con)/.test(n)) {
      return 'Este asistente responde dudas frecuentes. Para un caso concreto, escribe al soporte de FixLab o revisa la sección de contacto del sitio.';
    }

    return (
      'No tengo una respuesta exacta para eso. Prueba con palabras como pedido, pago, envío, productos o contraseña. ' +
      'También puedes usar el menú para navegar la tienda.'
    );
  }
}
