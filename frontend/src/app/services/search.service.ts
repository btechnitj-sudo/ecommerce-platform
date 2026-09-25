import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private searchTermSubject = new BehaviorSubject<string>('');
  private categorySubject = new BehaviorSubject<string>('all');

  searchTerm$ = this.searchTermSubject.asObservable();
  category$ = this.categorySubject.asObservable();

  setSearchTerm(term: string): void {
    this.searchTermSubject.next(term);
  }

  setCategory(category: string): void {
    this.categorySubject.next(category);
  }
}