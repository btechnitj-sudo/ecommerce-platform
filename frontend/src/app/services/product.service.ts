import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  sku: string;
  category: string;
  averageRating: number;
  reviewCount: number;
  imageUrl?: string;
}
export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  sku: string;
  category: string;
}

export interface UpdateProductRequest {
  name: string;
  description: string;
  price: number;
  category: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = `${environment.apiUrl}/api/products`;

  constructor(private http: HttpClient) {}

  getAllProducts(page: number = 0, size: number = 6, search?: string, category?: string): Observable<PagedResponse<Product>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }
    if (category && category !== 'all') {
      params = params.set('category', category);
    }

    return this.http.get<PagedResponse<Product>>(this.apiUrl, { params }).pipe(
      map(response => ({
        ...response,
        content: response.content.map(p => ({
          ...p,
          imageUrl: `https://loremflickr.com/400/300/${this.getImageKeyword(p)}?lock=${p.id}`
        }))
      }))
    );
  }

  private getImageKeyword(product: Product): string {
    const name = product.name.toLowerCase();

    if (name.includes('keyboard')) return 'keyboard';
    if (name.includes('mouse')) return 'computer-mouse';
    if (name.includes('webcam')) return 'webcam';
    if (name.includes('headphone')) return 'headphones';
    if (name.includes('monitor')) return 'monitor,screen';
    if (name.includes('hub')) return 'usb-cable';
    if (name.includes('stand')) return 'laptop-stand';
    if (name.includes('speaker')) return 'speaker';
    if (name.includes('charger')) return 'charger';
    if (name.includes('cable')) return 'cable';

    return product.category.toLowerCase();
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/categories`);
  }

  createProduct(request: CreateProductRequest): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, request);
  }

  updateProduct(id: number, request: UpdateProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, request);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}