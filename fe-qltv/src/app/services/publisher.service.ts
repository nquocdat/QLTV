import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Publisher } from '../models/publisher.model';

export interface PublisherPageResponse {
  content: Publisher[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface PublisherCreateRequest {
  name: string;
  address?: string;
  phoneNumber?: string;
  email?: string;
  website?: string;
}

@Injectable({
  providedIn: 'root',
})
export class PublisherService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/api';

  getAllPublishers(
    page: number = 0,
    size: number = 10,
    search: string = ''
  ): Observable<PublisherPageResponse> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (search) {
      params = params.set('q', search);
    }

    // Gọi đúng endpoint phân trang của backend
    return this.http.get<PublisherPageResponse>(`${this.apiUrl}/publishers/paginated`, { params });
  }

  getPublisherById(id: number): Observable<Publisher> {
    return this.http.get<Publisher>(`${this.apiUrl}/publishers/${id}`);
  }

  createPublisher(publisher: Omit<Publisher, 'id'>): Observable<Publisher> {
    return this.http.post<Publisher>(`${this.apiUrl}/publishers`, publisher);
  }

  updatePublisher(id: number, publisher: Partial<Publisher>): Observable<Publisher> {
    return this.http.put<Publisher>(`${this.apiUrl}/publishers/${id}`, publisher);
  }

  deletePublisher(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/publishers/${id}`);
  }

  searchPublishers(query: string): Observable<Publisher[]> {
    const params = { q: query };
    return this.http.get<Publisher[]>(`${this.apiUrl}/publishers/search`, { params });
  }

  getPublisherSuggestions(query: string): Observable<string[]> {
    const params = { q: query };
    return this.http.get<string[]>(`${this.apiUrl}/publishers/suggestions`, { params });
  }
}
