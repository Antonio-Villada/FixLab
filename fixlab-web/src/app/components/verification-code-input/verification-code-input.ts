import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  forwardRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-verification-code-input',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './verification-code-input.html',
  styleUrl: './verification-code-input.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => VerificationCodeInputComponent),
      multi: true,
    },
  ],
})
export class VerificationCodeInputComponent
  implements ControlValueAccessor, OnInit, OnChanges, AfterViewInit
{
  @Input() length = 6;
  /** Texto del encabezado centrado (mayúsculas vía CSS). Vacío = sin título. */
  @Input() heading = '';
  @Input() ariaLabel = 'Código de verificación';
  @Input() hasError = false;

  digits: string[] = [];
  disabled = false;

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(
    private cdr: ChangeDetectorRef,
    private host: ElementRef<HTMLElement>,
  ) {}

  get indices(): number[] {
    return Array.from({ length: this.length }, (_, i) => i);
  }

  ngOnInit(): void {
    this.ensureDigitsLength();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['length']) {
      this.ensureDigitsLength();
    }
  }

  ngAfterViewInit(): void {
    this.syncInputDom();
  }

  private ensureDigitsLength(): void {
    const next = Array(this.length).fill('');
    for (let i = 0; i < Math.min(this.digits.length, this.length); i++) {
      next[i] = this.digits[i] ?? '';
    }
    this.digits = next;
    this.cdr.markForCheck();
    queueMicrotask(() => this.syncInputDom());
  }

  writeValue(value: string | null): void {
    const raw = (value ?? '').replace(/\D/g, '').slice(0, this.length);
    this.digits = Array.from({ length: this.length }, (_, i) => raw[i] ?? '');
    this.cdr.markForCheck();
    queueMicrotask(() => this.syncInputDom());
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    this.cdr.markForCheck();
  }

  onInput(event: Event, index: number): void {
    if (this.disabled) return;
    const input = event.target as HTMLInputElement;
    const cleaned = input.value.replace(/\D/g, '');
    if (cleaned.length > 1) {
      for (let k = index; k < this.length; k++) {
        this.digits[k] = '';
      }
      this.spreadFromString(cleaned, index);
      this.emit();
      this.syncInputDom();
      this.focusInput(Math.min(index + cleaned.length - 1, this.length - 1));
      return;
    }
    const digit = cleaned.slice(-1);
    this.digits[index] = digit;
    input.value = digit;
    this.emit();
    if (digit && index < this.length - 1) {
      this.focusInput(index + 1);
    }
  }

  onKeydown(event: KeyboardEvent, index: number): void {
    if (this.disabled) return;
    const input = event.target as HTMLInputElement;
    if (event.key === 'Backspace') {
      if (input.value) {
        this.digits[index] = '';
        input.value = '';
        this.emit();
      } else if (index > 0) {
        const prev = this.host.nativeElement.querySelectorAll<HTMLInputElement>('.otp-input')[index - 1];
        if (prev) {
          prev.focus();
          prev.value = '';
          this.digits[index - 1] = '';
          this.emit();
        }
      }
      event.preventDefault();
      return;
    }
    if (event.key === 'ArrowLeft' && index > 0) {
      event.preventDefault();
      this.focusInput(index - 1);
    } else if (event.key === 'ArrowRight' && index < this.length - 1) {
      event.preventDefault();
      this.focusInput(index + 1);
    }
  }

  onPaste(event: ClipboardEvent): void {
    if (this.disabled) return;
    event.preventDefault();
    const text = (event.clipboardData?.getData('text') ?? '').replace(/\D/g, '').slice(0, this.length);
    this.digits = Array(this.length).fill('');
    this.spreadFromString(text, 0);
    this.emit();
    this.syncInputDom();
    const focusAt = text.length >= this.length ? this.length - 1 : Math.max(0, text.length - 1);
    this.focusInput(focusAt);
  }

  onHostFocusOut(ev: FocusEvent): void {
    const next = ev.relatedTarget as Node | null;
    if (next && this.host.nativeElement.contains(next)) {
      return;
    }
    this.onTouched();
  }

  /** Escribe `text` en celdas desde `startIndex` sin borrar el resto. */
  private spreadFromString(text: string, startIndex: number): void {
    for (let j = 0; j < text.length && startIndex + j < this.length; j++) {
      this.digits[startIndex + j] = text[j] ?? '';
    }
  }

  private emit(): void {
    this.onChange(this.digits.join(''));
    this.cdr.markForCheck();
  }

  private syncInputDom(): void {
    const inputs = this.host.nativeElement.querySelectorAll<HTMLInputElement>('.otp-input');
    inputs.forEach((el, i) => {
      el.value = this.digits[i] ?? '';
    });
  }

  private focusInput(index: number): void {
    queueMicrotask(() => {
      const inputs = this.host.nativeElement.querySelectorAll<HTMLInputElement>('.otp-input');
      const el = inputs[index];
      el?.focus();
      el?.select();
    });
  }
}
