import { Component, OnInit, inject, signal, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';

const STORAGE_KEY = 'fixlab-a11y';

interface A11yPrefs {
  fontSize: number;
  highContrast: boolean;
  largeCursor: boolean;
  reduceMotion: boolean;
}

const DEFAULTS: A11yPrefs = {
  fontSize: 100,
  highContrast: false,
  largeCursor: false,
  reduceMotion: false,
};

@Component({
  selector: 'app-accessibility-widget',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './accessibility-widget.html',
  styleUrl: './accessibility-widget.css',
})
export class AccessibilityWidgetComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);

  panelOpen = signal(false);
  prefs = signal<A11yPrefs>({ ...DEFAULTS });

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      try {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored) {
          const parsed = JSON.parse(stored) as Partial<A11yPrefs>;
          this.prefs.set({ ...DEFAULTS, ...parsed });
          this.applyPrefs(this.prefs());
        }
      } catch {
        // ignorar errores de parseo
      }
    }
  }

  togglePanel(): void {
    this.panelOpen.update((v) => !v);
  }

  private applyPrefs(p: A11yPrefs): void {
    if (isPlatformBrowser(this.platformId)) {
      const root = document.documentElement;
      root.style.fontSize = `${p.fontSize}%`;
      root.classList.toggle('a11y-high-contrast', p.highContrast);
      root.classList.toggle('a11y-large-cursor', p.largeCursor);
      root.classList.toggle('a11y-reduce-motion', p.reduceMotion);
    }
  }

  private savePrefs(p: A11yPrefs): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(p));
    }
  }

  setFontSize(delta: number): void {
    const p = { ...this.prefs() };
    p.fontSize = Math.max(80, Math.min(140, p.fontSize + delta));
    this.prefs.set(p);
    this.applyPrefs(p);
    this.savePrefs(p);
  }

  toggleHighContrast(): void {
    const p = { ...this.prefs(), highContrast: !this.prefs().highContrast };
    this.prefs.set(p);
    this.applyPrefs(p);
    this.savePrefs(p);
  }

  toggleLargeCursor(): void {
    const p = { ...this.prefs(), largeCursor: !this.prefs().largeCursor };
    this.prefs.set(p);
    this.applyPrefs(p);
    this.savePrefs(p);
  }

  toggleReduceMotion(): void {
    const p = { ...this.prefs(), reduceMotion: !this.prefs().reduceMotion };
    this.prefs.set(p);
    this.applyPrefs(p);
    this.savePrefs(p);
  }

  reset(): void {
    this.prefs.set({ ...DEFAULTS });
    this.applyPrefs(DEFAULTS);
    this.savePrefs(DEFAULTS);
  }
}
