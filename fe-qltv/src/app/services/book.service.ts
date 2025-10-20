import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book, BookCreateRequest } from '../models/book.model';
import { PageResponse, PageRequest } from '../models/pagination.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  private apiUrl = 'http://localhost:8081/api/books';

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

  getAllBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(this.apiUrl);
  }

  // Get books with pagination
  getBooksWithPagination(pageRequest: PageRequest): Observable<PageResponse<Book>> {
    let params = new HttpParams()
      .set('page', pageRequest.page.toString())
      .set('size', pageRequest.size.toString());

    if (pageRequest.sortBy) {
      params = params.set('sortBy', pageRequest.sortBy);
    }
    if (pageRequest.sortDir) {
      params = params.set('sortDir', pageRequest.sortDir);
    }
    if (pageRequest.status) {
      params = params.set('status', pageRequest.status);
    }

    return this.http.get<PageResponse<Book>>(`${this.apiUrl}/paginated`, { params });
  }

  // Search books with pagination
  searchBooksWithPagination(
    keyword: string,
    pageRequest: PageRequest
  ): Observable<PageResponse<Book>> {
    let params = new HttpParams()
      .set('q', keyword)
      .set('page', pageRequest.page.toString())
      .set('size', pageRequest.size.toString());

    if (pageRequest.sortBy) {
      params = params.set('sortBy', pageRequest.sortBy);
    }
    if (pageRequest.sortDir) {
      params = params.set('sortDir', pageRequest.sortDir);
    }

    return this.http.get<PageResponse<Book>>(`${this.apiUrl}/search/paginated`, { params });
  }

  // Get available books with pagination
  getAvailableBooksWithPagination(pageRequest: PageRequest): Observable<PageResponse<Book>> {
    let params = new HttpParams()
      .set('page', pageRequest.page.toString())
      .set('size', pageRequest.size.toString());

    if (pageRequest.sortBy) {
      params = params.set('sortBy', pageRequest.sortBy);
    }
    if (pageRequest.sortDir) {
      params = params.set('sortDir', pageRequest.sortDir);
    }

    return this.http.get<PageResponse<Book>>(`${this.apiUrl}/available/paginated`, { params });
  }

  // Get books by genre with pagination
  getBooksByGenreWithPagination(
    genre: string,
    pageRequest: PageRequest
  ): Observable<PageResponse<Book>> {
    let params = new HttpParams()
      .set('page', pageRequest.page.toString())
      .set('size', pageRequest.size.toString());

    if (pageRequest.sortBy) {
      params = params.set('sortBy', pageRequest.sortBy);
    }
    if (pageRequest.sortDir) {
      params = params.set('sortDir', pageRequest.sortDir);
    }

    return this.http.get<PageResponse<Book>>(`${this.apiUrl}/genre/${genre}/paginated`, { params });
  }

  getBookById(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.apiUrl}/${id}`);
  }

  createBook(book: BookCreateRequest): Observable<Book> {
    return this.http.post<Book>(this.apiUrl, book, this.getHttpOptions());
  }

  updateBook(id: number, book: Book): Observable<Book> {
    return this.http.put<Book>(`${this.apiUrl}/${id}`, book, this.getHttpOptions());
  }

  deleteBook(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, this.getHttpOptions());
  }

  /**
   * Upload book cover image
   */
  uploadCoverImage(file: File): Observable<{ imageUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);

    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      Authorization: token ? `Bearer ${token}` : '',
    });

    return this.http.post<{ imageUrl: string }>(`${this.apiUrl}/upload-cover`, formData, {
      headers,
    });
  }

  // Search books by keyword (simple)
  searchBooks(keyword: string): Observable<Book[]> {
    const params = keyword ? { params: new HttpParams().set('q', keyword) } : {};
    return this.http.get<Book[]>(`${this.apiUrl}/search`, params);
  }

  getAvailableBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/available`);
  }

  getBooksByGenre(genre: string): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/genre/${genre}`);
  }

  getBooksByAuthor(author: string): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/author/${author}`);
  }

  // Get books by category ID
  getBooksByCategory(
    categoryId: number,
    pageRequest?: PageRequest
  ): Observable<PageResponse<Book>> {
    let params = new HttpParams();

    if (pageRequest) {
      params = params
        .set('page', pageRequest.page.toString())
        .set('size', pageRequest.size.toString());

      if (pageRequest.sortBy) {
        params = params.set('sortBy', pageRequest.sortBy);
      }
      if (pageRequest.sortDir) {
        params = params.set('sortDir', pageRequest.sortDir);
      }
    } else {
      params = params.set('page', '0').set('size', '100');
    }

    return this.http.get<PageResponse<Book>>(`${this.apiUrl}/by-category/${categoryId}`, {
      params,
    });
  }

  // Get books by publisher ID
  getBooksByPublisher(
    publisherId: number,
    pageRequest?: PageRequest
  ): Observable<PageResponse<Book>> {
    let params = new HttpParams();

    if (pageRequest) {
      params = params
        .set('page', pageRequest.page.toString())
        .set('size', pageRequest.size.toString());

      if (pageRequest.sortBy) {
        params = params.set('sortBy', pageRequest.sortBy);
      }
      if (pageRequest.sortDir) {
        params = params.set('sortDir', pageRequest.sortDir);
      }
    } else {
      params = params.set('page', '0').set('size', '100');
    }

    return this.http.get<PageResponse<Book>>(`${this.apiUrl}/by-publisher/${publisherId}`, {
      params,
    });
  }

  // Autocomplete suggestions
  getAuthorSuggestions(query: string): Observable<string[]> {
    if (!query || query.length < 2) {
      return new Observable((observer) => {
        observer.next([]);
        observer.complete();
      });
    }
    const params = new HttpParams().set('q', query);
    return this.http.get<string[]>(`${this.apiUrl}/authors/suggestions`, { params });
  }

  getPublisherSuggestions(query: string): Observable<string[]> {
    if (!query || query.length < 2) {
      return new Observable((observer) => {
        observer.next([]);
        observer.complete();
      });
    }
    const params = new HttpParams().set('q', query);
    return this.http.get<string[]>(`${this.apiUrl}/publishers/suggestions`, { params });
  }

  getCategorySuggestions(query: string): Observable<string[]> {
    if (!query || query.length < 2) {
      return new Observable((observer) => {
        observer.next([]);
        observer.complete();
      });
    }
    const params = new HttpParams().set('q', query);
    return this.http.get<string[]>(`${this.apiUrl}/categories/suggestions`, { params });
  }

  getGenreSuggestions(query: string): Observable<string[]> {
    if (!query || query.length < 2) {
      return new Observable((observer) => {
        observer.next([]);
        observer.complete();
      });
    }
    const params = new HttpParams().set('q', query);
    return this.http.get<string[]>(`${this.apiUrl}/genres/suggestions`, { params });
  }

  getFeaturedBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/featured`, this.getHttpOptions());
  }

  // Get most borrowed books
  getMostBorrowedBooks(pageRequest: {
    page: number;
    size: number;
  }): Observable<PageResponse<Book>> {
    let params = new HttpParams()
      .set('page', pageRequest.page.toString())
      .set('size', pageRequest.size.toString());
    return this.http.get<PageResponse<Book>>(`${this.apiUrl}/most-borrowed`, { params });
  }

  // Mượn sách (tạo loan)
  borrowBook(bookId: number, patronId: number): Observable<any> {
    return this.http.post<any>('http://localhost:8081/api/loans/borrow', null, {
      params: new HttpParams()
        .set('bookId', bookId.toString())
        .set('patronId', patronId.toString()),
      ...this.getHttpOptions(),
    });
  }
}
