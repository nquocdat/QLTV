import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ReviewDTO {
  id: number;
  bookId: number;
  bookTitle: string;
  patronId: number;
  patronName: string;
  loanId?: number;
  rating: number;
  comment: string;
  approved: boolean;
  createdDate: string;
  updatedDate: string;
}

export interface ReviewStats {
  averageRating: number;
  reviewCount: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private apiUrl = 'http://localhost:8081/api/reviews';

  constructor(private http: HttpClient) {}

  /**
   * Check if current user can review a book
   */
  canReview(bookId: number): Observable<{ canReview: boolean }> {
    return this.http.get<{ canReview: boolean }>(`${this.apiUrl}/can-review/${bookId}`);
  }

  /**
   * Create a new review
   */
  createReview(data: { bookId: number; rating: number; comment: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}`, data);
  }

  /**
   * Get approved reviews for a book (public)
   */
  getBookReviews(bookId: number): Observable<ReviewDTO[]> {
    return this.http.get<ReviewDTO[]>(`${this.apiUrl}/book/${bookId}`);
  }

  /**
   * Get book rating statistics (public)
   */
  getBookRatingStats(bookId: number): Observable<ReviewStats> {
    return this.http.get<ReviewStats>(`${this.apiUrl}/book/${bookId}/stats`);
  }

  /**
   * Get current user's reviews
   */
  getMyReviews(): Observable<ReviewDTO[]> {
    return this.http.get<ReviewDTO[]>(`${this.apiUrl}/my-reviews`);
  }

  /**
   * Get review for a specific loan
   */
  getReviewByLoanId(loanId: number): Observable<ReviewDTO> {
    return this.http.get<ReviewDTO>(`${this.apiUrl}/loan/${loanId}`);
  }

  // ============ ADMIN METHODS ============

  /**
   * Get all reviews (admin only)
   */
  getAllReviews(page: number = 0, size: number = 10): Observable<Page<ReviewDTO>> {
    return this.http.get<Page<ReviewDTO>>(`${this.apiUrl}/admin/all?page=${page}&size=${size}`);
  }

  /**
   * Get pending reviews (admin only)
   */
  getPendingReviews(page: number = 0, size: number = 10): Observable<Page<ReviewDTO>> {
    return this.http.get<Page<ReviewDTO>>(`${this.apiUrl}/admin/pending?page=${page}&size=${size}`);
  }

  /**
   * Get approved reviews (admin only)
   */
  getApprovedReviews(page: number = 0, size: number = 10): Observable<Page<ReviewDTO>> {
    return this.http.get<Page<ReviewDTO>>(
      `${this.apiUrl}/admin/approved?page=${page}&size=${size}`
    );
  }

  /**
   * Approve a review (admin only)
   */
  approveReview(reviewId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/${reviewId}/approve`, {});
  }

  /**
   * Delete a review (admin only)
   */
  deleteReview(reviewId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/admin/${reviewId}`);
  }
}
