import { Injectable, signal, computed } from '@angular/core';
import { Product } from '../models/product.model';
import { CartItem } from '../models/cart.model';

const CART_STORAGE_KEY = 'fixlab_cart';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private readonly items = signal<CartItem[]>(this.loadFromStorage());

  /** Número total de unidades en el carrito (suma de cantidades). */
  totalCount = computed(() => this.items().reduce((sum, i) => sum + i.quantity, 0));

  /** Items del carrito (para mostrar en la vista). */
  cartItems = computed(() => [...this.items()]);

  /** Subtotal en dinero (suma de precio * cantidad por item). */
  subtotal = computed(() =>
    this.items().reduce((sum, i) => sum + i.product.precio * i.quantity, 0)
  );

  private loadFromStorage(): CartItem[] {
    try {
      const raw = localStorage.getItem(CART_STORAGE_KEY);
      if (!raw) return [];
      const parsed = JSON.parse(raw) as Array<{ product: Product; quantity: number }>;
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  private saveToStorage(): void {
    try {
      localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(this.items()));
    } catch {}
  }

  private productKey(p: Product): string {
    return p.id != null ? String(p.id) : p.sku;
  }

  /** Añade una unidad del producto al carrito (o incrementa si ya está). Respeta stock y producto activo. */
  addItem(product: Product, quantity: number = 1): void {
    const maxQty = product.stock ?? 0;
    if (product.activo === false || maxQty <= 0) {
      return;
    }
    const key = this.productKey(product);
    const current = this.items();
    const idx = current.findIndex(i => this.productKey(i.product) === key);

    let next: CartItem[];
    if (idx >= 0) {
      const item = current[idx];
      const newQty = Math.min(item.quantity + quantity, maxQty);
      if (newQty <= 0) {
        next = current.filter((_, i) => i !== idx);
      } else {
        next = current.map((it, i) =>
          i === idx ? { ...it, quantity: newQty } : it
        );
      }
    } else {
      const qty = Math.min(Math.max(1, quantity), maxQty);
      if (qty <= 0) {
        return;
      }
      next = [...current, { product, quantity: qty }];
    }

    this.items.set(next);
    this.saveToStorage();
  }

  /** Cambia la cantidad de un producto en el carrito (0 = eliminar). */
  setQuantity(product: Product, quantity: number): void {
    const key = this.productKey(product);
    const current = this.items();
    if (quantity <= 0) {
      this.items.set(current.filter(i => this.productKey(i.product) !== key));
    } else {
      const maxQty = Math.max(0, product.stock ?? 0);
      if (product.activo === false || maxQty <= 0) {
        this.items.set(current.filter(i => this.productKey(i.product) !== key));
        this.saveToStorage();
        return;
      }
      const qty = Math.min(quantity, maxQty);
      const idx = current.findIndex(i => this.productKey(i.product) === key);
      if (idx >= 0) {
        const next = current.map((it, i) =>
          i === idx ? { ...it, quantity: qty } : it
        );
        this.items.set(next);
      } else {
        this.items.set([...current, { product, quantity: qty }]);
      }
    }
    this.saveToStorage();
  }

  /** Quita el producto del carrito. */
  removeItem(product: Product): void {
    this.setQuantity(product, 0);
  }

  /** Vacía el carrito. */
  clear(): void {
    this.items.set([]);
    this.saveToStorage();
  }
}
