import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { ProductService } from '../../services/product.service';
import { SearchService } from '../../services/search.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit {
  cartCount = 0;
  currentUser: any;
  searchTerm = '';
  categories: string[] = [];
  selectedCategory = 'all';

  constructor(
    private authService: AuthService,
    private cartService: CartService,
    private productService: ProductService,
    private searchService: SearchService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.cartService.cart$.subscribe(() => {
      this.cartCount = this.cartService.getTotalItems();
    });
    
this.productService.getCategories().subscribe(categories => {
  this.categories = ['all', ...categories];
});

    this.searchService.category$.subscribe((cat: string) => this.selectedCategory = cat);
  }

  onSearch(): void {
    this.searchService.setSearchTerm(this.searchTerm);
    if (this.router.url !== '/products') {
      this.router.navigate(['/products']);
    }
  }

  selectCategory(category: string): void {
    this.searchService.setCategory(category);
    if (this.router.url !== '/products') {
      this.router.navigate(['/products']);
    }
  }

  logout(): void {
    this.authService.logout();
    this.cartService.clearCart();
    this.router.navigate(['/products']);
  }
}