import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CartService, CartItem } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { NavbarComponent } from '../../shared/navbar/navbar.component';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent implements OnInit {
  items: CartItem[] = [];
  isPlacingOrder = false;
  errorMessage = '';

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cartService.cart$.subscribe(items => this.items = items);
  }

  increment(item: CartItem): void {
    this.cartService.updateQuantity(item.productId, item.quantity + 1);
  }

  decrement(item: CartItem): void {
    this.cartService.updateQuantity(item.productId, item.quantity - 1);
  }

  remove(item: CartItem): void {
    this.cartService.removeFromCart(item.productId);
  }

  get total(): number {
    return this.cartService.getTotalPrice();
  }

placeOrder(): void {
  const currentUser = this.authService.getCurrentUser();
  if (!currentUser) {
    this.router.navigate(['/login']);
    return;
  }

  this.isPlacingOrder = true;
  this.errorMessage = '';

  this.orderService.placeOrder({
    userId: currentUser.id,
    items: this.items.map(i => ({ productId: i.productId, quantity: i.quantity }))
  }).subscribe({
    next: (order) => {
      this.isPlacingOrder = false;

      if (order.status === 'PAYMENT_FAILED') {
        this.errorMessage = 'Payment failed. Please try again.';
        return;
      }

      this.cartService.clearCart();
      this.router.navigate(['/orders']);
    },
    error: (err) => {
      this.isPlacingOrder = false;
      this.errorMessage = err.error?.message || 'Failed to place order.';
    }
  });
}
}