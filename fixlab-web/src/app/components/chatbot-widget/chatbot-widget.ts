import { Component, inject, signal, viewChild, ElementRef, effect, PLATFORM_ID, OnDestroy } from '@angular/core';
import { CommonModule, DOCUMENT, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ChatbotService, ChatMessage } from '../../services/chatbot.service';
import { AuthService } from '../../services/auth';
import { ChatRichPart, parseChatRichText } from './chat-rich-text';

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './chatbot-widget.html',
  styleUrl: './chatbot-widget.css',
})
export class ChatbotWidgetComponent implements OnDestroy {
  private chatbot = inject(ChatbotService);
  protected auth = inject(AuthService);
  private platformId = inject(PLATFORM_ID);
  private document = inject(DOCUMENT);

  panelOpen = signal(false);
  input = signal('');

  protected readonly messages = this.chatbot.messagesSignal;
  protected readonly loadingHistorial = this.chatbot.loadingHistorial;
  protected readonly enviando = this.chatbot.enviando;

  private scrollAnchor = viewChild<ElementRef<HTMLDivElement>>('scrollAnchor');

  constructor() {
    effect(() => {
      const open = this.panelOpen();
      if (isPlatformBrowser(this.platformId)) {
        this.document.body.classList.toggle('fixlab-chat-open', open);
      }
    });
    effect(() => {
      if (!this.panelOpen()) return;
      this.messages();
      if (isPlatformBrowser(this.platformId)) {
        requestAnimationFrame(() => this.scrollToBottom());
      }
    });
  }

  ngOnDestroy(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.document.body.classList.remove('fixlab-chat-open');
    }
  }

  togglePanel(): void {
    this.panelOpen.update((v) => !v);
  }

  closePanel(): void {
    this.panelOpen.set(false);
  }

  newChat(): void {
    this.chatbot.limpiarConversacion();
  }

  welcomeText(): string {
    return this.chatbot.getWelcomeMessage(this.auth.isLoggedIn());
  }

  send(): void {
    const text = this.input().trim();
    if (!text) return;
    this.input.set('');
    this.chatbot.sendUserMessage(text);
  }

  onKeydown(ev: KeyboardEvent): void {
    if (ev.key === 'Enter' && !ev.shiftKey) {
      ev.preventDefault();
      this.send();
    }
  }

  trackById(_: number, m: ChatMessage): string {
    return m.id;
  }

  /** Texto con [enlaces](/ruta) y **negrita** → trozos para la plantilla. */
  chatRichParts(raw: string): ChatRichPart[] {
    return parseChatRichText(raw);
  }

  private scrollToBottom(): void {
    const el = this.scrollAnchor()?.nativeElement;
    el?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }
}
