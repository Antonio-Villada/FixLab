import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { CartService } from './cart.service';

export type ChatRole = 'user' | 'bot';

export interface ChatMessage {
  id: string;
  role: ChatRole;
  text: string;
  at: number;
  /** Solo mensajes del asistente tras guardar en servidor; invitado no lo tiene. */
  replySource?: 'IA' | 'FAQ';
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
  respuestaFuente?: string;
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
  private router = inject(Router);
  private cart = inject(CartService);

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
      return '¡Hola! Soy tu asistente. ¡Bienvenido a FixLab! 🛠️\nTodo listo para asistirte. ¿Empezamos?';
    }
    return '¡Hola! Inicia sesión para guardar tu historial de consultas. Como invitado, el chat no se guarda al cerrar la página.';
  }

  sendUserMessage(text: string): void {
    const trimmed = text.trim();
    if (!trimmed) return;

    if (this.hasToken()) {
      const optimisticId = `opt-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
      this.messages.update((list) => [
        ...list,
        { id: optimisticId, role: 'user', text: trimmed, at: Date.now() },
      ]);
      this.enviando.set(true);
      this.http
        .post<ChatEnviarRespDTO>(`${this.baseUrl}/mensaje`, {
          texto: trimmed,
          ...this.buildChatContextPayload(),
        })
        .subscribe({
          next: (res) => {
            const botFuente =
              res.respuestaFuente === 'IA' || res.respuestaFuente === 'FAQ'
                ? res.respuestaFuente
                : undefined;
            this.messages.update((list) => {
              const rest = list.filter((m) => m.id !== optimisticId);
              return [
                ...rest,
                this.mapApiToMessage(res.userMessage),
                this.mapApiToMessage(res.botMessage, botFuente),
              ];
            });
            this.enviando.set(false);
          },
          error: () => {
            this.messages.update((list) => list.filter((m) => m.id !== optimisticId));
            this.enviando.set(false);
            this.pushLocalUserAndBot(trimmed);
          },
        });
      return;
    }

    this.pushLocalUserAndBot(trimmed);
  }

  /** Ruta del SPA y resumen del carrito (sin datos sensibles) para cada mensaje al API o al FAQ local. */
  private buildChatContextPayload(): { rutaApp?: string; resumenCarrito?: string } {
    if (!isPlatformBrowser(this.platformId)) {
      return {};
    }
    const raw = this.router.url ?? '';
    const rutaApp = raw.length > 500 ? raw.slice(0, 500) : raw || undefined;
    const n = this.cart.totalCount();
    let resumenCarrito: string | undefined;
    if (n > 0) {
      const sub = this.cart.subtotal();
      const line = `${n} unidades en carrito, subtotal ${sub}`;
      resumenCarrito = line.length > 300 ? line.slice(0, 300) : line;
    }
    return {
      ...(rutaApp ? { rutaApp } : {}),
      ...(resumenCarrito ? { resumenCarrito } : {}),
    };
  }

  private mapApiToMessage(d: ChatApiMensajeDTO, botReplySource?: 'IA' | 'FAQ'): ChatMessage {
    const isUser = d.role === 'USER';
    return {
      id: `srv-${d.id}`,
      role: isUser ? 'user' : 'bot',
      text: d.text,
      at: new Date(d.createdAt).getTime(),
      replySource: !isUser ? botReplySource : undefined,
    };
  }

  private pushLocalUserAndBot(trimmed: string): void {
    this.enviando.set(true);
    this.pushMessage({ role: 'user', text: trimmed });
    const reply = this.hybridReply(trimmed, this.buildChatContextPayload());
    window.setTimeout(() => {
      this.pushMessage({ role: 'bot', text: reply });
      this.enviando.set(false);
    }, 420);
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

  private fechaHoyDispositivo(): string {
    try {
      return new Intl.DateTimeFormat('es-CO', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric',
      }).format(new Date());
    } catch {
      return new Date().toLocaleDateString('es-CO');
    }
  }

  private hybridReply(
    userText: string,
    ctx: { rutaApp?: string; resumenCarrito?: string },
  ): string {
    const n = this.normalize(userText);
    const pantalla =
      ctx.rutaApp && ctx.rutaApp.length > 0 ? `Veo que estás en **${ctx.rutaApp}**. ` : '';
    const carritoHint = ctx.resumenCarrito ? ` (${ctx.resumenCarrito})` : '';

    if (/\b(hola|buenas|hey|hi)\b/.test(n)) {
      return (
        pantalla +
        '¡Hola! Pregúntame por pedidos, pagos (Wompi), envíos, productos o cuenta. Enlaces: [Productos](/productos), [Login](/login).' +
        (ctx.resumenCarrito ? ` Tu carrito ahora mismo: ${ctx.resumenCarrito}.` : '')
      );
    }
    if (/\b(gracias|thank)\b/.test(n)) {
      return '¡Con gusto! Si necesitas algo más, aquí estaré.';
    }

    if (/\b(que|qu[eé])\s+es\s+fixlab\b/.test(n) || (/\bfixlab\b/.test(n) && n.length < 40)) {
      return (
        pantalla +
        'FixLab es la plataforma de esta tienda y taller en línea: catálogo, pedidos con pagos (Wompi), ' +
        'tu cuenta en [Tu panel](/dashboard) y reparaciones en [Seguimiento](/reparaciones).'
      );
    }

    if (
      /(que\s+dia\s+es\s+hoy|que\s+dia\s+es\b|fecha\s+de\s+hoy|dia\s+de\s+hoy|what\s+day\s+is\s+today|today'?s?\s+date)/.test(
        n,
      )
    ) {
      return (
        pantalla +
        `Hoy es **${this.fechaHoyDispositivo()}** (fecha según tu dispositivo). ` +
        'Para FixLab: pedidos, pagos o [productos](/productos).'
      );
    }

    if (/(pedido|orden|compra realizada|estado del pedido|mis compras)/.test(n)) {
      return 'Inicia sesión y revisa tu actividad en [Tu panel](/dashboard). Para comprar: [Productos](/productos).';
    }

    if (/(pago|pagar|wompi|tarjeta|checkout|transaccion)/.test(n)) {
      const aqui =
        ctx.rutaApp?.includes('/carrito') || ctx.rutaApp?.includes('/checkout')
          ? ' Estás en el flujo de compra; revisa el total y sigue a la pasarela desde aquí.'
          : '';
      return (
        pantalla +
        'Los pagos se procesan con Wompi. Usa el [carrito](/carrito) y completa el pago en la pasarela.' +
        aqui +
        carritoHint
      );
    }

    if (/(envio|envío|domicilio|entrega|recibir)/.test(n)) {
      return 'Tiempos y costos según zona y pedido. Luego del pago, el estado lo ves en [Tu panel](/dashboard).';
    }

    if (/(producto|catalogo|catálogo|comprar|tienda)/.test(n)) {
      return 'Aquí está el catálogo: [Ver productos](/productos). Puedes añadir al [carrito](/carrito).';
    }

    if (/(cuenta|registro|registrarme|contraseña|password|clave|correo verificado)/.test(n)) {
      return 'Cuenta: [Login](/login), [Registro](/register), [Recuperar contraseña](/recuperar-password). Con sesión: [Panel](/dashboard).';
    }

    if (/(devolucion|devolución|reembolso)/.test(n)) {
      return 'Para devoluciones o cambios, contacta al equipo de FixLab con tu número de pedido. Un administrador podrá orientarte según la política de la tienda.';
    }

    if (/(horario|contacto|soporte|ayuda humana|hablar con)/.test(n)) {
      return 'Este asistente responde dudas frecuentes. Para un caso concreto, escribe al soporte de FixLab o revisa la sección de contacto del sitio.';
    }

    return (
      pantalla +
      'No tengo una respuesta exacta. Prueba: pedido, pago, envío, productos. O abre [Productos](/productos) o [Inicio](/home).'
    );
  }
}
