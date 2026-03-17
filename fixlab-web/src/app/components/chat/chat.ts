import { Component, inject, signal, ViewChild, ElementRef, AfterViewChecked, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../services/auth';
import { ChatMessage } from '../../models/chat.model';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class ChatComponent implements AfterViewChecked {
  private chatService = inject(ChatService);
  private authService = inject(AuthService);

  mensajes = signal<ChatMessage[]>([]);
  texto = signal('');
  enviando = signal(false);
  error = signal<string | null>(null);
  @ViewChild('mensajesContainer') private mensajesContainerRef!: ElementRef<HTMLDivElement>;
  private debeHacerScroll = false;
  private lastHistoryKey: string | null = null;
  private readonly HISTORY_PREFIX = 'fixlab_chat_history_';

  private readonly authSync = effect(() => {
    const loggedIn = this.authService.isLoggedInSignal();
    const key = loggedIn ? this.getHistoryKeyForCurrentUser() : null;

    if (!loggedIn) {
      this.lastHistoryKey = null;
      this.mensajes.set([]);
      return;
    }

    if (key && key !== this.lastHistoryKey) {
      this.lastHistoryKey = key;
      this.mensajes.set(this.readHistory(key));
      this.debeHacerScroll = true;
    }
  });

  ngAfterViewChecked(): void {
    if (this.debeHacerScroll) {
      this.debeHacerScroll = false;
      this.scrollToBottom();
    }
  }

  enviar(): void {
    const t = this.texto().trim();
    if (!t || this.enviando()) return;

    this.error.set(null);
    this.mensajes.update((list) => [...list, { emisor: 'usuario', texto: t }]);
    this.persistHistory();
    this.texto.set('');
    this.debeHacerScroll = true;
    this.enviando.set(true);

    this.chatService.enviarMensaje(t).subscribe({
      next: (resp) => {
        this.mensajes.update((list) => [
          ...list,
          {
            emisor: 'bot',
            texto: resp.respuesta,
            tipoAccion: resp.tipoAccion,
            payload: resp.payload,
          },
        ]);
        this.persistHistory();
        this.debeHacerScroll = true;
        this.enviando.set(false);
      },
      error: (err) => {
        const msg =
          err.error?.mensaje || err.status === 401
            ? 'Debes iniciar sesión para usar el chat.'
            : 'No se pudo enviar el mensaje. Intenta de nuevo.';
        this.error.set(msg);
        this.mensajes.update((list) => [
          ...list,
          { emisor: 'bot', texto: msg },
        ]);
        this.persistHistory();
        this.debeHacerScroll = true;
        this.enviando.set(false);
      },
    });
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.enviar();
    }
  }

  private scrollToBottom(): void {
    const el = this.mensajesContainerRef?.nativeElement;
    if (el) el.scrollTop = el.scrollHeight;
  }

  /** Enlace para ver pedido (factura). */
  getRutaVerPedido(payload: string | undefined): string {
    if (!payload) return '/dashboard';
    return `/factura/${payload}`;
  }

  private getHistoryKeyForCurrentUser(): string | null {
    const email = this.authService.getEmailFromToken();
    if (!email) return null;
    return `${this.HISTORY_PREFIX}${email.toLowerCase()}`;
  }

  private readHistory(key: string): ChatMessage[] {
    try {
      const raw = localStorage.getItem(key);
      if (!raw) return [];
      const parsed = JSON.parse(raw) as unknown;
      return Array.isArray(parsed) ? (parsed as ChatMessage[]) : [];
    } catch {
      return [];
    }
  }

  private persistHistory(): void {
    const key = this.lastHistoryKey ?? this.getHistoryKeyForCurrentUser();
    if (!key) return;
    try {
      localStorage.setItem(key, JSON.stringify(this.mensajes()));
    } catch {
      // noop
    }
  }
}
