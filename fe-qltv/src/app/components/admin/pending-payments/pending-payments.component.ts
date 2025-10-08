import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoanPaymentService, LoanPayment } from '../../../services/loan-payment.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-pending-payments',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pending-payments.component.html',
  styleUrls: ['./pending-payments.component.css'],
})
export class PendingPaymentsComponent implements OnInit {
  pendingPayments: LoanPayment[] = [];
  loading = false;
  errorMessage = '';

  constructor(private loanPaymentService: LoanPaymentService, private authService: AuthService) {}

  ngOnInit() {
    this.loadPendingPayments();
  }

  loadPendingPayments() {
    this.loading = true;
    this.errorMessage = '';

    this.loanPaymentService.getPendingCashPayments().subscribe({
      next: (payments) => {
        this.pendingPayments = payments;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading payments:', error);
        this.errorMessage = 'Không thể tải danh sách thanh toán';
        this.loading = false;
      },
    });
  }

  confirmPayment(payment: LoanPayment) {
    if (
      !confirm(
        `Xác nhận đã nhận tiền mặt ${payment.amount.toLocaleString('vi-VN')} VND từ ${
          payment.patronName
        }?`
      )
    ) {
      return;
    }

    // Get current user from localStorage
    const currentUser = this.authService.getUser();
    if (!currentUser || !currentUser.id) {
      alert('❌ Không tìm thấy thông tin người dùng!');
      return;
    }

    this.loanPaymentService.confirmCashPayment(payment.id, currentUser.id).subscribe({
      next: (response) => {
        if (response.success) {
          alert('✅ ' + response.message);
          this.loadPendingPayments(); // Reload list
        } else {
          alert('❌ ' + (response.error || 'Xác nhận thất bại'));
        }
      },
      error: (error) => {
        alert('❌ Lỗi: ' + (error.error?.error || 'Không thể xác nhận thanh toán'));
      },
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  formatCurrency(amount: number): string {
    return amount.toLocaleString('vi-VN') + ' VND';
  }

  getTotalAmount(): string {
    const total = this.pendingPayments.reduce((sum, p) => sum + p.amount, 0);
    return this.formatCurrency(total);
  }
}
