import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Product } from './product.service';

export interface CartItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
  stockQuantity: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private cartSubject = new BehaviorSubject<CartItem[]>(this.loadCart());
  cart$ = this.cartSubject.asObservable();

  private loadCart(): CartItem[] {
    const saved = localStorage.getItem('cart');
    return saved ? JSON.parse(saved) : [];
  }

  private saveCart(items: CartItem[]): void {
    localStorage.setItem('cart', JSON.stringify(items));
    this.cartSubject.next(items);
  }

  getItems(): CartItem[] {
    return this.cartSubject.value;
  }

  addToCart(product: Product, quantity: number = 1): void {
    const items = [...this.getItems()];
    const existing = items.find(i => i.productId === product.id);

    if (existing) {
      existing.quantity = Math.min(existing.quantity + quantity, product.stockQuantity);
    } else {
      items.push({
        productId: product.id,
        name: product.name,
        price: product.price,
        quantity: Math.min(quantity, product.stockQuantity),
        stockQuantity: product.stockQuantity
      });
    }
    this.saveCart(items);
  }

  updateQuantity(productId: number, quantity: number): void {
    const items = [...this.getItems()];
    const item = items.find(i => i.productId === productId);
    if (item) {
      item.quantity = Math.max(1, Math.min(quantity, item.stockQuantity));
      this.saveCart(items);
    }
  }

  removeFromCart(productId: number): void {
    this.saveCart(this.getItems().filter(i => i.productId !== productId));
  }

  clearCart(): void {
    this.saveCart([]);
  }

  getTotalItems(): number {
    return this.getItems().reduce((sum, i) => sum + i.quantity, 0);
  }

  getTotalPrice(): number {
    return this.getItems().reduce((sum, i) => sum + i.price * i.quantity, 0);
  }
}