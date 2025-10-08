import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-result.component.html',
  styleUrls: ['./payment-result.component.css'],
})
export class PaymentResultComponent implements OnInit {
  isSuccess = false;
  message = '';
  loading = true;
  loanId: number | null = null;

  constructor(private route: ActivatedRoute, private router: Router, private http: HttpClient) {}

  ngOnInit() {
    // Lấy params từ VNPay callback
    this.route.queryParams.subscribe((params) => {
      if (Object.keys(params).length === 0) {
        this.isSuccess = false;
        this.message = 'Không có thông tin thanh toán';
        this.loading = false;
        return;
      }

      // Call backend để verify payment
      this.http
        .get<any>('http://localhost:8081/api/loan-payments/vnpay-callback', {
          params: params as any,
        })
        .subscribe({
          next: (response) => {
            this.isSuccess = response.success;
            this.message = response.message || 'Thanh toán thành công';
            this.loanId = response.loanId;
            this.loading = false;
          },
          error: (error) => {
            this.isSuccess = false;
            this.message = error.error?.error || 'Lỗi xử lý thanh toán';
            this.loading = false;
          },
        });
    });
  }

  goToMyLoans() {
    this.router.navigate(['/library/profile']);
  }

  goToHome() {
    this.router.navigate(['/library/home']);
  }
}
