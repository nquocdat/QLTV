import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Loan, BorrowRequest } from '../models/loan.model';
import { AuthService } from './auth.service';

export interface BorrowWithPaymentResponse {
  loanId: number;
  paymentUrl?: string;
  paymentMethod: 'CASH' | 'VNPAY';
  message: string;
  amount: number;
  error?: string;
}

@Injectable({
  providedIn: 'root',
})
export class LoanService {
  private apiUrl = 'http://localhost:8081/api/loans';

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

  getAllLoans(): Observable<Loan[]> {
    return this.http.get<Loan[]>(this.apiUrl, this.getHttpOptions());
  }

  getLoanById(id: number): Observable<Loan> {
    return this.http.get<Loan>(`${this.apiUrl}/${id}`, this.getHttpOptions());
  }

  getLoansByPatronId(patronId: number): Observable<Loan[]> {
    return this.http.get<Loan[]>(`${this.apiUrl}/patron/${patronId}`, this.getHttpOptions());
  }

  getLoansByBookId(bookId: number): Observable<Loan[]> {
    return this.http.get<Loan[]>(`${this.apiUrl}/book/${bookId}`, this.getHttpOptions());
  }

  getActiveLoans(): Observable<Loan[]> {
    return this.http.get<Loan[]>(`${this.apiUrl}/active`, this.getHttpOptions());
  }

  getOverdueLoans(): Observable<Loan[]> {
    return this.http.get<Loan[]>(`${this.apiUrl}/overdue`, this.getHttpOptions());
  }

  borrowBook(bookId: number, patronId: number): Observable<Loan> {
    return this.http.post<Loan>(
      `${this.apiUrl}/borrow?bookId=${bookId}&patronId=${patronId}`,
      {},
      this.getHttpOptions()
    );
  }

  /**
   * Mượn sách với thanh toán (CASH hoặc VNPAY)
   */
  borrowBookWithPayment(
    bookId: number,
    patronId: number,
    paymentMethod: 'CASH' | 'VNPAY'
  ): Observable<BorrowWithPaymentResponse> {
    return this.http.post<BorrowWithPaymentResponse>(
      `${this.apiUrl}/borrow-with-payment?bookId=${bookId}&patronId=${patronId}&paymentMethod=${paymentMethod}`,
      {},
      this.getHttpOptions()
    );
  }

  returnBook(loanId: number): Observable<Loan> {
    return this.http.put<Loan>(`${this.apiUrl}/${loanId}/return`, {}, this.getHttpOptions());
  }

  /**
   * Trả sách với phí phạt hỏng sách
   */
  returnBookWithDamage(loanId: number, damageFine: number, damageNotes?: string): Observable<Loan> {
    const params: any = { damageFine: damageFine.toString() };
    if (damageNotes) {
      params.damageNotes = damageNotes;
    }
    return this.http.put<Loan>(
      `${this.apiUrl}/${loanId}/return-with-damage`,
      {},
      { ...this.getHttpOptions(), params }
    );
  }

  renewLoan(loanId: number): Observable<Loan> {
    return this.http.put<Loan>(`${this.apiUrl}/${loanId}/renew`, {}, this.getHttpOptions());
  }

  getPatronLoanHistory(patronId: number): Observable<Loan[]> {
    return this.http.get<Loan[]>(
      `${this.apiUrl}/patron/${patronId}/history`,
      this.getHttpOptions()
    );
  }

  getLoansWithFines(): Observable<Loan[]> {
    return this.http.get<Loan[]>(`${this.apiUrl}/fines`, this.getHttpOptions());
  }

  confirmReturnBook(loanId: number): Observable<Loan> {
    return this.http.put<Loan>(
      `${this.apiUrl}/${loanId}/confirm-return`,
      {},
      this.getHttpOptions()
    );
  }

  getVNPayUrl(amount: number, orderId: string, orderInfo: string): Observable<string> {
    return this.http.post<string>(
      'http://localhost:8081/api/payments/vnpay/url',
      {},
      {
        params: {
          amount: amount.toString(),
          orderId,
          orderInfo,
        },
        ...this.getHttpOptions(),
      }
    );
  }
}
