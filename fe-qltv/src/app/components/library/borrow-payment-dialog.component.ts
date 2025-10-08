import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Book } from '../../models/book.model';
import { CommonModule, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-borrow-payment-dialog',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './borrow-payment-dialog.component.html',
  styleUrls: ['./borrow-payment-dialog.component.css'],
})
export class BorrowPaymentDialogComponent {
  @Input() book!: Book;
  @Input() visible: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() pay = new EventEmitter<'online' | 'cash'>();

  get price(): number {
    return this.book.fee || 10000;
  }

  choose(method: 'online' | 'cash') {
    this.pay.emit(method);
    this.close.emit();
  }
}
