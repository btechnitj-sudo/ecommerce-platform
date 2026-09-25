import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { OrderService, Order } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss'
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  errorMessage = '';
  successMessage = '';
  isLoading = true;
  currentUserId!: number;

  constructor(
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.router.navigate(['/login']);
      return;
    }
    this.currentUserId = currentUser.id;
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.orderService.getOrdersByUser(this.currentUserId).subscribe({
      next: (orders) => {
        this.orders = orders;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load orders.';
        this.isLoading = false;
      }
    });
  }

  canCancel(order: Order): boolean {
    return order.status === 'PENDING' || order.status === 'CONFIRMED';
  }

  cancelOrder(order: Order): void {
    if (!confirm(`Cancel Order #${order.id}? This cannot be undone.`)) {
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.orderService.cancelOrder(order.id, this.currentUserId).subscribe({
      next: () => {
        this.successMessage = `Order #${order.id} cancelled.`;
        this.loadOrders();
      },
      error: () => {
        this.errorMessage = 'Failed to cancel order.';
      }
    });
  }
}