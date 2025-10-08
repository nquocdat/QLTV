import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoanPayment {
  id: number;
  loanId: number;
  patronId: number;
  patronName: string;
  patronEmail: string;
  bookId: number;
  bookTitle: string;
  amount: number;
  paymentMethod: 'CASH' | 'VNPAY';
  paymentStatus: 'PENDING' | 'CONFIRMED' | 'FAILED' | 'REFUNDED';
  transactionNo?: string;
  bankCode?: string;
  vnpayResponseCode?: string;
  description?: string;
  createdDate: string;
  confirmedDate?: string;
  confirmedBy?: number;
  confirmedByName?: string;
  updatedDate: string;
}

export interface ConfirmPaymentResponse {
  success: boolean;
  message?: string;
  error?: string;
  payment?: LoanPayment;
}

@Injectable({
  providedIn: 'root',
})
export class LoanPaymentService {
  private apiUrl = 'http://localhost:8081/api/loan-payments';

  constructor(private http: HttpClient) {}

  /**
   * Lấy tất cả payments
   */
  getAllPayments(): Observable<LoanPayment[]> {
    return this.http.get<LoanPayment[]>(this.apiUrl);
  }

  /**
   * Lấy payment theo ID
   */
  getPaymentById(id: number): Observable<LoanPayment> {
    return this.http.get<LoanPayment>(`${this.apiUrl}/${id}`);
  }

  /**
   * Lấy payment theo loan ID
   */
  getPaymentByLoanId(loanId: number): Observable<LoanPayment> {
    return this.http.get<LoanPayment>(`${this.apiUrl}/loan/${loanId}`);
  }

  /**
   * Lấy payments của patron
   */
  getPaymentsByPatronId(patronId: number): Observable<LoanPayment[]> {
    return this.http.get<LoanPayment[]>(`${this.apiUrl}/patron/${patronId}`);
  }

  /**
   * Lấy danh sách cash payments chờ xác nhận (Admin/Librarian)
   */
  getPendingCashPayments(): Observable<LoanPayment[]> {
    return this.http.get<LoanPayment[]>(`${this.apiUrl}/pending-cash`);
  }

  /**
   * Lấy pending payments của patron
   */
  getPendingPaymentsByPatronId(patronId: number): Observable<LoanPayment[]> {
    return this.http.get<LoanPayment[]>(`${this.apiUrl}/patron/${patronId}/pending`);
  }

  /**
   * Xác nhận thanh toán tiền mặt
   */
  confirmCashPayment(paymentId: number, confirmedBy: number): Observable<ConfirmPaymentResponse> {
    return this.http.put<ConfirmPaymentResponse>(`${this.apiUrl}/${paymentId}/confirm-cash`, null, {
      params: { confirmedBy: confirmedBy.toString() },
    });
  }

  /**
   * Hủy payment
   */
  cancelPayment(paymentId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${paymentId}`);
  }

  /**
   * Đếm số lượng payments chờ xác nhận
   */
  countPendingPayments(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/pending-count`);
  }
}
