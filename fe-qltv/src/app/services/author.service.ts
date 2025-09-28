import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Author } from '../models/author.model';

export interface AuthorPageResponse {
  content: Author[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root',
})
export class AuthorService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/api/authors';

  getAllAuthors(
    page: number = 0,
    size: number = 10,
    search: string = ''
  ): Observable<AuthorPageResponse> {
    if (search) {
      // Gọi API /search với page và size
      let params = new HttpParams()
        .set('q', search)
        .set('page', page.toString())
        .set('size', size.toString());
      return this.http.get<AuthorPageResponse>(`${this.apiUrl}/search`, { params });
    } else {
      let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
      return this.http.get<AuthorPageResponse>(`${this.apiUrl}/paginated`, { params });
    }
  }

  getAuthorById(id: number): Observable<Author> {
    return this.http.get<Author>(`${this.apiUrl}/${id}`);
  }

  createAuthor(author: Omit<Author, 'id'>): Observable<Author> {
    return this.http.post<Author>(`${this.apiUrl}`, author);
  }

  updateAuthor(id: number, author: Partial<Author>): Observable<Author> {
    return this.http.put<Author>(`${this.apiUrl}/${id}`, author);
  }

  deleteAuthor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  searchAuthors(query: string): Observable<Author[]> {
    return this.http.get<Author[]>(`${this.apiUrl}/search?q=${encodeURIComponent(query)}`);
  }

  getAuthorSuggestions(query: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/suggestions?q=${encodeURIComponent(query)}`);
  }
}
