import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaymentService, PaymentTransaction } from '../../../services/payment.service';

@Component({
  selector: 'app-payment-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment-management.component.html',
  styleUrls: ['./payment-management.component.css'],
})
export class PaymentManagementComponent implements OnInit {
  payments: PaymentTransaction[] = [];
  filterMethod: string = '';
  filterStatus: string = '';

  constructor(private paymentService: PaymentService) {}

  ngOnInit(): void {
    this.loadPayments();
  }

  loadPayments(): void {
    this.paymentService.getAllPayments().subscribe((data) => {
      this.payments = data;
    });
  }

  get filteredPayments(): PaymentTransaction[] {
    return this.payments.filter(
      (p) =>
        (!this.filterMethod || p.paymentMethod === this.filterMethod) &&
        (!this.filterStatus ||
          p.paymentStatus === this.filterStatus ||
          p.status === this.filterStatus)
    );
  }

  get totalPaid(): number {
    return this.filteredPayments
      .filter((p) => p.paymentStatus === 'CONFIRMED' || p.status === 'SUCCESS')
      .reduce((sum, p) => sum + p.amount, 0);
  }

  confirmCash(payment: PaymentTransaction): void {
    if (!payment.loanId) {
      alert('Lỗi: Không tìm thấy Loan ID');
      return;
    }

    this.paymentService
      .confirmCashPayment(payment.amount, payment.loanId.toString(), payment.description || '')
      .subscribe((updated) => {
        payment.paymentStatus = updated.paymentStatus;
        payment.status = updated.status;
        payment.confirmedDate = updated.confirmedDate;
        payment.paidDate = updated.paidDate;
        this.loadPayments(); // Reload để cập nhật danh sách
      });
  }
}
