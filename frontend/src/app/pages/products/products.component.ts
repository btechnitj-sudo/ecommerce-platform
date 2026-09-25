import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { combineLatest } from 'rxjs';
import { ProductService, Product } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { SearchService } from '../../services/search.service';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { Review, ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, NavbarComponent,FormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss'
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  currentPage = 0;
  totalPages = 0;
  pageSize = 6;

  currentSearch = '';
  currentCategory = 'all';

  errorMessage = '';
  isLoading = true;
  quantities: { [productId: number]: number } = {};
  addedMessage = '';

  expandedProductId: number | null = null;
reviews: { [productId: number]: Review[] } = {};
newReview: { rating: number; comment: string } = { rating: 5, comment: '' };


  constructor(
  private productService: ProductService,
  private cartService: CartService,
  private searchService: SearchService,
  private reviewService: ReviewService,
  private authService: AuthService
) {}

  ngOnInit(): void {
    combineLatest([
      this.searchService.searchTerm$,
      this.searchService.category$
    ]).subscribe(([term, category]) => {
      this.currentSearch = term;
      this.currentCategory = category;
      this.currentPage = 0;
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.isLoading = true;
    this.productService.getAllProducts(this.currentPage, this.pageSize, this.currentSearch, this.currentCategory).subscribe({
      next: (response) => {
        this.products = response.content;
        this.totalPages = response.totalPages;
        this.products.forEach(p => {
          if (!this.quantities[p.id]) {
            this.quantities[p.id] = 1;
          }
        });
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load products.';
        this.isLoading = false;
      }
    });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadProducts();
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  incrementQty(product: Product): void {
    if (this.quantities[product.id] < product.stockQuantity) {
      this.quantities[product.id]++;
    }
  }

  decrementQty(product: Product): void {
    if (this.quantities[product.id] > 1) {
      this.quantities[product.id]--;
    }
  }

  addToCart(product: Product): void {
    this.cartService.addToCart(product, this.quantities[product.id]);
    this.addedMessage = `Added ${this.quantities[product.id]} × ${product.name} to cart`;
    setTimeout(() => this.addedMessage = '', 2000);
  }

  toggleReviews(product: Product): void {
  if (this.expandedProductId === product.id) {
    this.expandedProductId = null;
    return;
  }
  this.expandedProductId = product.id;
  this.newReview = { rating: 5, comment: '' };

  if (!this.reviews[product.id]) {
    this.reviewService.getReviews(product.id).subscribe(reviews => {
      this.reviews[product.id] = reviews;
    });
  }
}

submitReview(product: Product): void {
  const currentUser = this.authService.getCurrentUser();
  if (!currentUser) {
    this.errorMessage = 'Please log in to leave a review.';
    return;
  }

  this.reviewService.addReview(product.id, {
    userId: currentUser.id,
    reviewerName: currentUser.fullName,
    rating: this.newReview.rating,
    comment: this.newReview.comment
  }).subscribe({
    next: (review) => {
      this.reviews[product.id] = [review, ...(this.reviews[product.id] || [])];
      product.reviewCount = (product.reviewCount || 0) + 1;
      this.newReview = { rating: 5, comment: '' };
    },
    error: () => {
      this.errorMessage = 'Failed to submit review.';
    }
  });
}

get starArray(): number[] {
  return [1, 2, 3, 4, 5];
}
}