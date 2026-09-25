import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService, Product, CreateProductRequest, UpdateProductRequest } from '../../services/product.service';
import { OrderService, Order } from '../../services/order.service';
import { NavbarComponent } from '../../shared/navbar/navbar.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss'
})
export class AdminComponent implements OnInit {
  activeTab: 'products' | 'orders' = 'products';

  products: Product[] = [];
  orders: Order[] = [];

  isLoading = true;
  errorMessage = '';
  successMessage = '';

  // Add/edit form state
  showForm = false;
  editingProductId: number | null = null;
  formModel: CreateProductRequest = this.emptyForm();

  constructor(
    private productService: ProductService,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  switchTab(tab: 'products' | 'orders'): void {
    this.activeTab = tab;
    this.successMessage = '';
    this.errorMessage = '';
    if (tab === 'orders' && this.orders.length === 0) {
      this.loadOrders();
    }
  }

 loadProducts(): void {
  this.isLoading = true;
  this.productService.getAllProducts(0, 100).subscribe({
    next: (response) => {
      this.products = response.content;
      this.isLoading = false;
    },
    error: () => {
      this.errorMessage = 'Failed to load products.';
      this.isLoading = false;
    }
  });
}

  loadOrders(): void {
    this.isLoading = true;
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load orders. (Admin access required.)';
        this.isLoading = false;
      }
    });
  }

  emptyForm(): CreateProductRequest {
    return {
      name: '',
      description: '',
      price: 0,
      stockQuantity: 0,
      sku: '',
      category: ''
    };
  }

  openAddForm(): void {
    this.editingProductId = null;
    this.formModel = this.emptyForm();
    this.showForm = true;
  }

  openEditForm(product: Product): void {
    this.editingProductId = product.id;
    this.formModel = {
      name: product.name,
      description: product.description,
      price: product.price,
      stockQuantity: product.stockQuantity,
      sku: product.sku,
      category: product.category
    };
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingProductId = null;
  }

  submitForm(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.editingProductId) {
      const updateRequest: UpdateProductRequest = {
        name: this.formModel.name,
        description: this.formModel.description,
        price: this.formModel.price,
        category: this.formModel.category
      };

      this.productService.updateProduct(this.editingProductId, updateRequest).subscribe({
        next: () => {
          this.successMessage = 'Product updated successfully.';
          this.showForm = false;
          this.loadProducts();
        },
        error: () => {
          this.errorMessage = 'Failed to update product. (Admin access required.)';
        }
      });
    } else {
      this.productService.createProduct(this.formModel).subscribe({
        next: () => {
          this.successMessage = 'Product created successfully.';
          this.showForm = false;
          this.loadProducts();
        },
        error: () => {
          this.errorMessage = 'Failed to create product. Check SKU is unique and all fields are valid.';
        }
      });
    }
  }

  deleteProduct(product: Product): void {
    if (!confirm(`Delete "${product.name}"? This cannot be undone.`)) {
      return;
    }
    this.productService.deleteProduct(product.id).subscribe({
      next: () => {
        this.successMessage = 'Product deleted.';
        this.loadProducts();
      },
      error: () => {
        this.errorMessage = 'Failed to delete product. (Admin access required.)';
      }
    });
  }
}