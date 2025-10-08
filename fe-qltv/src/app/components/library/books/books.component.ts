import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BookService } from '../../../services/book.service';
import { LoanService } from '../../../services/loan.service';
import { AuthService } from '../../../services/auth.service';
import { Book } from '../../../models/book.model';
import { PageRequest, PageResponse } from '../../../models/pagination.model';
import { CommonModule } from '@angular/common';
import { PaymentMethodModalComponent } from '../../shared/payment-method-modal/payment-method-modal.component';
import { BookCopiesListComponent } from '../../shared/book-copies-list/book-copies-list.component';

@Component({
  selector: 'app-books',
  standalone: true,
  templateUrl: './books.component.html',
  styleUrls: ['./books.component.css'],
  imports: [CommonModule, PaymentMethodModalComponent, BookCopiesListComponent],
})
export class BooksComponent implements OnInit {
  books: Book[] = [];
  searchQuery: string = '';
  categoryId: number | null = null;
  publisherId: number | null = null;
  loading = false;
  error = '';
  showBookDetailModal = false;
  selectedBook: Book | null = null;
  showPaymentModal = false;
  selectedBookForPayment: Book | null = null;
  isProcessingPayment = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private loanService: LoanService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.searchQuery = params['search'] || '';
      this.categoryId = params['category'] ? +params['category'] : null;
      this.publisherId = params['publisher'] ? +params['publisher'] : null;
      this.fetchBooks();
    });
  }

  fetchBooks(): void {
    this.loading = true;
    this.error = '';
    const pageRequest: PageRequest = {
      page: 0,
      size: 20,
      sortBy: 'title',
      sortDir: 'asc',
    };

    // If categoryId is provided, fetch books by category
    if (this.categoryId) {
      this.bookService.getBooksByCategory(this.categoryId, pageRequest).subscribe({
        next: (res: PageResponse<Book>) => {
          this.books = res.content;
          this.loading = false;
        },
        error: () => {
          this.error = 'Không tìm thấy sách trong thể loại này.';
          this.books = [];
          this.loading = false;
        },
      });
    }
    // If publisherId is provided, fetch books by publisher
    else if (this.publisherId) {
      this.bookService.getBooksByPublisher(this.publisherId, pageRequest).subscribe({
        next: (res: PageResponse<Book>) => {
          this.books = res.content;
          this.loading = false;
        },
        error: () => {
          this.error = 'Không tìm thấy sách của nhà xuất bản này.';
          this.books = [];
          this.loading = false;
        },
      });
    }
    // Otherwise, search books
    else {
      this.bookService.searchBooksWithPagination(this.searchQuery, pageRequest).subscribe({
        next: (res) => {
          this.books = res.content;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Không tìm thấy sách phù hợp.';
          this.books = [];
          this.loading = false;
        },
      });
    }
  }

  openBookDetail(book: Book) {
    this.selectedBook = { ...book };
    this.showBookDetailModal = true;
  }

  closeBookDetailModal() {
    this.showBookDetailModal = false;
    this.selectedBook = null;
  }

  borrowBook(book: Book): void {
    const user = this.authService.getUser();
    if (!user) {
      this.router.navigate(['/login']);
      return;
    }
    if ((book?.availableCopies || 0) <= 0) {
      alert('Sách này hiện không có sẵn để mượn');
      return;
    }
    // Mở payment modal
    this.selectedBookForPayment = book;
    this.showPaymentModal = true;
  }

  closePaymentModal() {
    this.showPaymentModal = false;
    this.selectedBookForPayment = null;
  }

  handleConfirmPayment(paymentMethod: 'CASH' | 'VNPAY') {
    const currentUser = this.authService.getUser();
    if (
      !currentUser ||
      !currentUser.id ||
      !this.selectedBookForPayment ||
      !this.selectedBookForPayment.id
    ) {
      alert('❌ Có lỗi xảy ra. Vui lòng thử lại.');
      return;
    }

    this.isProcessingPayment = true;

    this.loanService
      .borrowBookWithPayment(this.selectedBookForPayment.id, currentUser.id, paymentMethod)
      .subscribe({
        next: (response) => {
          this.isProcessingPayment = false;
          this.closePaymentModal();

          if (paymentMethod === 'VNPAY' && response.paymentUrl) {
            // Redirect to VNPay
            window.location.href = response.paymentUrl;
          } else if (paymentMethod === 'CASH') {
            // Show success message for cash payment
            alert('✅ ' + response.message);
            // Reload books
            this.fetchBooks();
          }
        },
        error: (error) => {
          this.isProcessingPayment = false;
          alert('❌ Lỗi: ' + (error.error?.error || 'Không thể tạo phiếu mượn'));
        },
      });
  }

  getStatusText(status: string): string {
    switch (status?.toUpperCase()) {
      case 'AVAILABLE':
        return 'Có sẵn';
      case 'BORROWED':
        return 'Đã mượn';
      case 'RESERVED':
        return 'Đã đặt trước';
      default:
        return 'Không xác định';
    }
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'AVAILABLE':
        return 'bg-green-100 text-green-800';
      case 'BORROWED':
        return 'bg-yellow-100 text-yellow-800';
      case 'RESERVED':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}
