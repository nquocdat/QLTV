import { Component, OnInit } from '@angular/core';
import { CategoryService, Category } from '../../../services/category.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { BookService } from '../../../services/book.service';
import { LibraryService, LibraryStats } from '../../../services/library.service';
import { LoanService } from '../../../services/loan.service';
import { ReviewService, ReviewDTO, ReviewStats } from '../../../services/review.service';
import {
  MembershipTier,
  UserMembership,
  UserRating,
} from '../../../models/advanced-features.model';
import { JwtResponse } from '../../../models/patron.model';
import { Book } from '../../../models/book.model';
import { PublisherService } from '../../../services/publisher.service';
import { PaymentMethodModalComponent } from '../../shared/payment-method-modal/payment-method-modal.component';
import { BookCopiesListComponent } from '../../shared/book-copies-list/book-copies-list.component';
import { AdvancedFeaturesService } from '../../../services/advanced-features.service';
import { StarRating } from '../../shared/star-rating/star-rating';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    PaymentMethodModalComponent,
    BookCopiesListComponent,
    StarRating,
  ],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  recentBooks: Book[] = [];
  // Slides for carousel
  slides = [
    {
      image: '/assets/slide1.jpg',
      title: 'Khám phá kho sách đa dạng',
      description:
        'Hàng ngàn đầu sách thuộc nhiều thể loại, từ văn học, khoa học đến kỹ năng sống.',
    },
    {
      image: '/assets/slide2.jpg',
      title: 'Mượn sách dễ dàng',
      description:
        'Chỉ với vài thao tác, bạn có thể mượn sách yêu thích và quản lý lịch sử mượn trả.',
    },
    {
      image: '/assets/slide3.jpg',
      title: 'Cộng đồng bạn đọc',
      description: 'Tham gia đánh giá, bình luận và chia sẻ cảm nhận về sách cùng mọi người.',
    },
  ];
  isLoggedIn: boolean = false;
  searchQuery: string = '';
  currentUser: JwtResponse | null = null;
  showBookDetailModal: boolean = false;
  selectedBook: any = null;

  // Review data
  bookReviews: ReviewDTO[] = [];
  reviewStats: ReviewStats | null = null;

  membershipTier: MembershipTier | null = null;
  userMembership: UserMembership | null = null;
  userStats = {
    totalLoans: 0,
    onTimeReturns: 0,
    lateReturns: 0,
    rating: 'GOOD',
  };
  stats: LibraryStats = {
    totalBooks: 0,
    totalUsers: 0,
    activeLoans: 0,
    totalLoans: 0,
  };
  featuredBooks: Book[] = [];
  categories: Category[] = [];
  loanList: any[] = [];
  publishers: any[] = [];
  showPaymentDialog: boolean = false;
  selectedBookForPayment: any = null;
  showPaymentModal = false;
  isProcessingPayment = false;

  constructor(
    private router: Router,
    private authService: AuthService,
    private bookService: BookService,
    private libraryService: LibraryService,
    private categoryService: CategoryService,
    private loanService: LoanService,
    private publisherService: PublisherService,
    private advancedFeaturesService: AdvancedFeaturesService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.isLoggedIn = !!user;
      this.currentUser = user;
      if (user) {
        this.loadUserMembership();
        this.loadStats();
        this.loadLoanList();
      }
    });
    this.loadRecentBooks();
    this.loadFavoriteBooks();
    this.loadCategories();
    this.loadPublishers();
  }

  loadRecentBooks(): void {
    this.bookService
      .getBooksWithPagination({ page: 0, size: 5, sortBy: 'createdDate', sortDir: 'desc' })
      .subscribe({
        next: (res: any) => {
          this.recentBooks = res.content;
        },
        error: () => {
          this.recentBooks = [];
        },
      });
  }

  favoriteBooks: Book[] = [];

  loadFavoriteBooks(): void {
    // Gọi endpoint riêng cho top sách mượn nhiều nhất
    this.bookService.getMostBorrowedBooks({ page: 0, size: 5 }).subscribe({
      next: (res: any) => {
        this.favoriteBooks = res.content;
      },
      error: () => {
        this.favoriteBooks = [];
      },
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories: Category[]) => {
        this.categories = categories;
      },
      error: (error: any) => {
        console.error('Error loading categories:', error);
        this.categories = [];
      },
    });
  }

  loadPublishers(): void {
    // Gọi service lấy danh sách nhà xuất bản
    this.publisherService.getAllPublishers(0, 20).subscribe({
      next: (res: any) => {
        this.publishers = res.content || [];
      },
      error: () => {
        this.publishers = [];
      },
    });
  }

  loadLoanList(): void {
    if (!this.currentUser?.id) {
      this.loanList = [];
      return;
    }
    // Gọi API lấy danh sách sách đã mượn của user
    // Sử dụng LoanService nếu đã import, nếu chưa thì import vào constructor
    (this as any).loanService.getLoansByPatronId(this.currentUser.id).subscribe({
      next: (loans: any[]) => {
        this.loanList = loans;
      },
      error: () => {
        this.loanList = [];
      },
    });
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/library/books'], {
        queryParams: { search: this.searchQuery.trim() },
      });
    }
  }

  loadFeaturedBooks(): void {
    // Đã thay bằng loadRecentBooks và loadCategoriesWithFeatured
    // Không dùng nữa
  }

  viewBookDetails(book: any): void {
    console.log('Viewing book details:', book);
    this.router.navigate(['/library/books'], {
      queryParams: { search: book.title },
    });
  }

  browseCategory(categoryId: number): void {
    this.router.navigate(['/library/books'], {
      queryParams: { category: categoryId },
    });
  }

  browsePublisher(publisherId: number): void {
    this.router.navigate(['/library/books'], {
      queryParams: { publisher: publisherId },
    });
  }

  viewAllBooks(): void {
    this.router.navigate(['/library/books']);
  }

  openBookDetail(book: any) {
    this.selectedBook = { ...book };
    this.showBookDetailModal = true;

    // Load reviews and stats for this book
    if (book.id) {
      this.loadBookReviews(book.id);
      this.loadReviewStats(book.id);
    }
  }

  closeBookDetailModal() {
    this.showBookDetailModal = false;
    this.selectedBook = null;
    this.bookReviews = [];
    this.reviewStats = null;
  }

  loadBookReviews(bookId: number): void {
    this.reviewService.getBookReviews(bookId).subscribe({
      next: (reviews) => {
        this.bookReviews = reviews;
      },
      error: (err) => {
        console.error('Error loading reviews:', err);
        this.bookReviews = [];
      },
    });
  }

  loadReviewStats(bookId: number): void {
    this.reviewService.getBookRatingStats(bookId).subscribe({
      next: (stats) => {
        this.reviewStats = stats;
      },
      error: (err) => {
        console.error('Error loading review stats:', err);
        this.reviewStats = null;
      },
    });
  }

  formatReviewDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  }

  openPaymentDialog(book: any): void {
    this.selectedBookForPayment = book;
    this.showPaymentDialog = true;
  }

  handlePayment(method: 'online' | 'cash') {
    const patronId = Number(this.currentUser?.id);
    if (!patronId) {
      alert('Không xác định được người dùng.');
      return;
    }
    if (method === 'online') {
      this.payAndBorrowVNPay(this.selectedBookForPayment, patronId);
    } else {
      this.bookService.borrowBook(this.selectedBookForPayment.id, patronId).subscribe({
        next: () => {
          this.selectedBookForPayment.status = 'borrowed';
          alert(
            `Bạn đã mượn thành công sách "${this.selectedBookForPayment.title}" (thanh toán tiền mặt)`
          );
          this.showPaymentDialog = false;
        },
        error: () => {
          alert('Có lỗi xảy ra khi mượn sách.');
        },
      });
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'available':
        return 'bg-green-100 text-green-800';
      case 'borrowed':
        return 'bg-yellow-100 text-yellow-800';
      case 'reserved':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusText(status: string): string {
    switch (status?.toLowerCase()) {
      case 'available':
        return 'Có sẵn';
      case 'borrowed':
        return 'Đã mượn';
      case 'reserved':
        return 'Đã đặt trước';
      default:
        return 'Không xác định';
    }
  }

  borrowBook(book: any): void {
    if (!this.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    if (book.availableCopies <= 0) {
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
    if (!currentUser || !this.selectedBookForPayment) {
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
            // Reload data
            this.loadRecentBooks();
            this.loadFavoriteBooks();
            if (this.currentUser?.id) {
              this.loadLoanList();
            }
          }
        },
        error: (error) => {
          this.isProcessingPayment = false;
          alert('❌ Lỗi: ' + (error.error?.error || 'Không thể tạo phiếu mượn'));
        },
      });
  }

  payAndBorrowVNPay(book: Book, patronId: number) {
    const amount = book.fee || 10000; // Sửa: lấy giá mượn từ book
    const orderId = 'ORDER_' + new Date().getTime();
    const orderInfo = `Thanh toán mượn sách: ${book.title}`;
    this.loanService.getVNPayUrl(amount, orderId, orderInfo).subscribe({
      next: (url: string) => {
        window.location.href = url;
      },
      error: () => {
        alert('Không thể tạo giao dịch thanh toán!');
      },
    });
  }

  choosePaymentMethod(book: any): void {
    if (!this.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    if (book.status?.toLowerCase() !== 'available') {
      alert('Sách này hiện không có sẵn để mượn');
      return;
    }
    if (confirm(`Bạn có muốn thanh toán khi mượn sách "${book.title}"?`)) {
      const method = prompt('Chọn phương thức thanh toán: "online" hoặc "cash"', 'cash');
      const patronId = Number(this.currentUser?.id);
      if (!patronId) {
        alert('Không xác định được người dùng.');
        return;
      }
      if (method === 'online') {
        this.payAndBorrowVNPay(book, patronId);
      } else {
        this.bookService.borrowBook(book.id, patronId).subscribe({
          next: () => {
            book.status = 'borrowed';
            const featuredBookIndex = this.featuredBooks.findIndex((b) => b.id === book.id);
            if (featuredBookIndex !== -1) {
              this.featuredBooks[featuredBookIndex].status = 'borrowed';
            }
            alert(`Bạn đã mượn thành công sách "${book.title}" (thanh toán tiền mặt)`);
            if (this.showBookDetailModal) {
              this.closeBookDetailModal();
            }
          },
          error: () => {
            alert('Có lỗi xảy ra khi mượn sách.');
          },
        });
      }
    }
  }

  private loadUserMembership(): void {
    if (!this.currentUser?.id) return;

    this.advancedFeaturesService.getUserMembership(this.currentUser.id).subscribe({
      next: (membership) => {
        this.userMembership = membership;

        // Get tier details
        if (membership.tier) {
          this.membershipTier = membership.tier;
        }

        // Update user stats from membership
        this.userStats = {
          totalLoans: membership.totalLoans || 0,
          onTimeReturns: Math.max(
            0,
            (membership.totalLoans || 0) - (membership.violationCount || 0)
          ),
          lateReturns: membership.violationCount || 0,
          rating: 'GOOD',
        };
      },
      error: (error) => {
        console.error('Error loading membership:', error);
        // Use default values if API fails
        this.setDefaultMembership();
      },
    });
  }

  private setDefaultMembership(): void {
    this.membershipTier = {
      id: 1,
      name: 'Cơ bản',
      level: 'BASIC',
      benefits: [
        {
          type: 'MAX_BOOKS',
          value: 3,
          description: 'Mượn tối đa 3 cuốn sách',
        },
        {
          type: 'LOAN_DURATION',
          value: 14,
          description: 'Thời gian mượn 14 ngày',
        },
      ],
      requirements: {
        minLoans: 0,
        minPoints: 0,
      },
      color: '#6B7280',
      icon: 'user',
    };
    this.userMembership = {
      userId: this.currentUser?.id || 0,
      tierId: 1,
      currentPoints: 0,
      totalLoans: 0,
      violations: 0,
      joinDate: new Date(),
      nextTierProgress: 0,
    };
    this.userStats = {
      totalLoans: 0,
      onTimeReturns: 0,
      lateReturns: 0,
      rating: 'GOOD',
    };
  }

  private loadStats(): void {
    this.libraryService.getLibraryStats().subscribe({
      next: (stats: LibraryStats) => {
        this.stats = stats;
      },
      error: (error: any) => {
        console.error('Error loading library stats:', error);
      },
    });
  }
}
