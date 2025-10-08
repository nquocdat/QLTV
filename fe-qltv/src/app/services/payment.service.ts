import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PaymentTransaction {
  id: number;
  loanId: number;
  patronId: number;
  patronName: string;
  patronEmail: string;
  bookId: number;
  bookTitle: string;
  amount: number;
  paymentMethod: string; // CASH, VNPAY
  paymentStatus: string; // PENDING, CONFIRMED, FAILED, REFUNDED
  transactionNo?: string;
  bankCode?: string;
  vnpayResponseCode?: string;
  description?: string;
  createdDate: string;
  confirmedDate?: string;
  confirmedBy?: number;
  confirmedByName?: string;
  updatedDate?: string;

  // Legacy fields for backward compatibility
  orderId?: string;
  status?: string;
  paidDate?: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private apiUrl = 'http://localhost:8081/api/loan-payments';

  constructor(private http: HttpClient) {}

  getAllPayments(): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.apiUrl}`);
  }

  confirmCashPayment(
    amount: number,
    orderId: string,
    description: string
  ): Observable<PaymentTransaction> {
    return this.http.post<PaymentTransaction>(`${this.apiUrl}/cash`, null, {
      params: { amount: amount.toString(), orderId, description },
    });
  }
}
