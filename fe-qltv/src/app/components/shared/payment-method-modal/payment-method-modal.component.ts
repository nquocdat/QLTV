import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-payment-method-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment-method-modal.component.html',
  styleUrls: ['./payment-method-modal.component.css'],
})
export class PaymentMethodModalComponent {
  @Input() show = false;
  @Input() bookTitle = '';
  @Input() depositAmount = 50000;
  @Output() closeModal = new EventEmitter<void>();
  @Output() confirmPayment = new EventEmitter<'CASH' | 'VNPAY'>();

  selectedMethod: 'CASH' | 'VNPAY' = 'CASH';

  onClose() {
    this.closeModal.emit();
  }

  onConfirm() {
    this.confirmPayment.emit(this.selectedMethod);
  }

  formatCurrency(amount: number): string {
    return amount.toLocaleString('vi-VN') + ' VND';
  }
}
