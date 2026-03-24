import { Component, signal, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header/header';
import { FooterComponent } from './components/footer/footer';
import { AccessibilityWidgetComponent } from './components/accessibility-widget/accessibility-widget';
import { ChatbotWidgetComponent } from './components/chatbot-widget/chatbot-widget';
import { AuthService } from './services/auth';
import { ChatbotService } from './services/chatbot.service';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    HeaderComponent,
    FooterComponent,
    AccessibilityWidgetComponent,
    ChatbotWidgetComponent,
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('fixlab-web');
  private authService = inject(AuthService);
  private chatbotService = inject(ChatbotService);

  ngOnInit(): void {
    this.authService.syncLoginStateFromStorage();
    if (this.authService.isLoggedIn()) {
      this.chatbotService.loadHistorial();
    }
  }
}
