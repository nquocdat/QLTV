import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';
import { AuthService } from './auth.service';

export interface Category {
  id?: number;
  name: string;
  description?: string;
  createdDate?: string;
  bookCount?: number;
  favoriteBooks?: import('../models/book.model').Book[];
}

export interface CategoryCreateRequest {
  name: string;
  description?: string;
}

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private apiUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHttpOptions() {
    const token = this.authService.getToken();
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
      }),
    };
  }

  getAllCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories`, this.getHttpOptions());
  }

  getCategoryById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/categories/${id}`, this.getHttpOptions());
  }

  createCategory(category: CategoryCreateRequest): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/categories`, category, this.getHttpOptions());
  }

  updateCategory(id: number, category: CategoryCreateRequest): Observable<Category> {
    return this.http.put<Category>(
      `${this.apiUrl}/categories/${id}`,
      category,
      this.getHttpOptions()
    );
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`, this.getHttpOptions());
  }

  searchCategories(query: string): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories/search`, {
      ...this.getHttpOptions(),
      params: { q: query },
    });
  }

  getCategorySuggestions(query: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/categories/suggestions`, {
      ...this.getHttpOptions(),
      params: { q: query },
    });
  }
}
