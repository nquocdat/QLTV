import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BookCopyDTO {
  id: number;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  bookIsbn: string;
  bookCoverImage: string;
  copyNumber: number;
  barcode: string;
  conditionStatus: string;
  conditionStatusDisplay: string;
  status: string;
  statusDisplay: string;
  location: string;
  acquisitionDate: string;
  price: number;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface AvailableCountResponse {
  availableCount: number;
}

export interface CreateCopyRequest {
  barcode?: string;
  conditionStatus?: string;
  status?: string;
  location?: string;
  acquisitionDate?: string;
  price?: number;
  notes?: string;
}

export interface BulkCreateRequest {
  quantity: number;
  location?: string;
  price?: number;
}

@Injectable({
  providedIn: 'root',
})
export class BookCopyService {
  private apiUrl = 'http://localhost:8081/api/book-copies';

  constructor(private http: HttpClient) {}

  /**
   * Lấy tất cả copies của một book
   */
  getCopiesByBookId(bookId: number): Observable<BookCopyDTO[]> {
    return this.http.get<BookCopyDTO[]>(`${this.apiUrl}/book/${bookId}`);
  }

  /**
   * Lấy chi tiết một copy
   */
  getCopyById(id: number): Observable<BookCopyDTO> {
    return this.http.get<BookCopyDTO>(`${this.apiUrl}/${id}`);
  }

  /**
   * Lấy copy available đầu tiên
   */
  getFirstAvailableCopy(bookId: number): Observable<BookCopyDTO> {
    return this.http.get<BookCopyDTO>(`${this.apiUrl}/book/${bookId}/available`);
  }

  /**
   * Đếm số lượng copies available
   */
  countAvailableCopies(bookId: number): Observable<AvailableCountResponse> {
    return this.http.get<AvailableCountResponse>(`${this.apiUrl}/book/${bookId}/available/count`);
  }

  /**
   * Tìm copy theo barcode
   */
  getCopyByBarcode(barcode: string): Observable<BookCopyDTO> {
    return this.http.get<BookCopyDTO>(`${this.apiUrl}/barcode/${barcode}`);
  }

  /**
   * Tạo copy mới (Admin/Librarian)
   */
  createCopy(bookId: number, copyData: CreateCopyRequest): Observable<BookCopyDTO> {
    return this.http.post<BookCopyDTO>(`${this.apiUrl}/book/${bookId}`, copyData);
  }

  /**
   * Tạo nhiều copies cùng lúc (Admin/Librarian)
   */
  createMultipleCopies(bookId: number, request: BulkCreateRequest): Observable<BookCopyDTO[]> {
    return this.http.post<BookCopyDTO[]>(`${this.apiUrl}/book/${bookId}/bulk`, request);
  }

  /**
   * Cập nhật copy (Admin/Librarian)
   */
  updateCopy(id: number, copyData: Partial<CreateCopyRequest>): Observable<BookCopyDTO> {
    return this.http.put<BookCopyDTO>(`${this.apiUrl}/${id}`, copyData);
  }

  /**
   * Xóa copy (Admin)
   */
  deleteCopy(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  /**
   * Lấy tất cả copies (Admin/Librarian)
   */
  getAllCopies(): Observable<BookCopyDTO[]> {
    return this.http.get<BookCopyDTO[]>(this.apiUrl);
  }

  /**
   * Lấy copies theo status (Admin/Librarian)
   */
  getCopiesByStatus(status: string): Observable<BookCopyDTO[]> {
    return this.http.get<BookCopyDTO[]>(`${this.apiUrl}/status/${status}`);
  }

  /**
   * Lấy copies cần bảo trì (Admin/Librarian)
   */
  getCopiesNeedingMaintenance(): Observable<BookCopyDTO[]> {
    return this.http.get<BookCopyDTO[]>(`${this.apiUrl}/maintenance`);
  }

  // Helper methods

  /**
   * Get CSS class cho status badge
   */
  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'AVAILABLE':
        return 'bg-green-100 text-green-800';
      case 'BORROWED':
        return 'bg-yellow-100 text-yellow-800';
      case 'RESERVED':
        return 'bg-blue-100 text-blue-800';
      case 'LOST':
        return 'bg-red-100 text-red-800';
      case 'REPAIRING':
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  /**
   * Get CSS class cho condition badge
   */
  getConditionClass(condition: string): string {
    switch (condition?.toUpperCase()) {
      case 'NEW':
        return 'bg-emerald-100 text-emerald-800';
      case 'GOOD':
        return 'bg-green-100 text-green-800';
      case 'FAIR':
        return 'bg-yellow-100 text-yellow-800';
      case 'POOR':
        return 'bg-orange-100 text-orange-800';
      case 'DAMAGED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  /**
   * Get icon cho status
   */
  getStatusIcon(status: string): string {
    switch (status?.toUpperCase()) {
      case 'AVAILABLE':
        return '✅';
      case 'BORROWED':
        return '📚';
      case 'RESERVED':
        return '🔖';
      case 'LOST':
        return '❌';
      case 'REPAIRING':
        return '🔧';
      default:
        return '❓';
    }
  }
}
