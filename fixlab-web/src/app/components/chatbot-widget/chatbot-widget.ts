import { Component, inject, signal, viewChild, ElementRef, effect, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService, ChatMessage } from '../../services/chatbot.service';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot-widget.html',
  styleUrl: './chatbot-widget.css',
})
export class ChatbotWidgetComponent {
  private chatbot = inject(ChatbotService);
  protected auth = inject(AuthService);
  private platformId = inject(PLATFORM_ID);

  panelOpen = signal(false);
  input = signal('');

  protected readonly messages = this.chatbot.messagesSignal;
  protected readonly loadingHistorial = this.chatbot.loadingHistorial;
  protected readonly enviando = this.chatbot.enviando;

  private scrollAnchor = viewChild<ElementRef<HTMLDivElement>>('scrollAnchor');

  constructor() {
    effect(() => {
      if (!this.panelOpen()) return;
      this.messages();
      if (isPlatformBrowser(this.platformId)) {
        requestAnimationFrame(() => this.scrollToBottom());
      }
    });
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

  private scrollToBottom(): void {
    const el = this.scrollAnchor()?.nativeElement;
    el?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }
}
