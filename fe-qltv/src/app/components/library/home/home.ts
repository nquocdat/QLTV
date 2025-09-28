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
import {
  MembershipTier,
  UserMembership,
  UserRating,
} from '../../../models/advanced-features.model';
import { JwtResponse } from '../../../models/patron.model';
import { Book } from '../../../models/book.model';
import { PublisherService } from '../../../services/publisher.service';

@Component({
  selector: 'app-home',
  imports: [CommonModule, RouterModule, FormsModule],
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

  constructor(
    private router: Router,
    private authService: AuthService,
    private bookService: BookService,
    private libraryService: LibraryService,
    private categoryService: CategoryService,
    private loanService: LoanService,
    private publisherService: PublisherService
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

  openBookDetail(book: any) {
    this.selectedBook = { ...book };
    this.showBookDetailModal = true;
  }

  closeBookDetailModal() {
    this.showBookDetailModal = false;
    this.selectedBook = null;
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
    if (book.status?.toLowerCase() !== 'available') {
      alert('Sách này hiện không có sẵn để mượn');
      return;
    }
    if (confirm(`Bạn có chắc muốn mượn sách "${book.title}"?`)) {
      const patronId = Number(this.currentUser?.id);
      if (!patronId) {
        alert('Không xác định được người dùng.');
        return;
      }
      this.bookService.borrowBook(book.id, patronId).subscribe({
        next: () => {
          book.status = 'borrowed';
          const featuredBookIndex = this.featuredBooks.findIndex((b) => b.id === book.id);
          if (featuredBookIndex !== -1) {
            this.featuredBooks[featuredBookIndex].status = 'borrowed';
          }
          alert(`Bạn đã mượn thành công sách "${book.title}"`);
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

  private loadUserMembership(): void {
    this.membershipTier = {
      id: 1,
      name: 'VIP',
      level: 'VIP',
      benefits: [
        {
          type: 'MAX_BOOKS',
          value: 5,
          description: 'Mượn tối đa 5 cuốn sách',
        },
        {
          type: 'LOAN_DURATION',
          value: 21,
          description: 'Thời gian mượn 21 ngày',
        },
      ],
      requirements: {
        minLoans: 10,
        minPoints: 100,
      },
      color: 'purple',
      icon: 'star',
    };
    this.userMembership = {
      userId: this.currentUser?.id || 1,
      tierId: 1,
      currentPoints: 150,
      totalLoans: 15,
      violations: 0,
      joinDate: new Date('2024-01-15'),
      nextTierProgress: 75,
    };
    this.userStats = {
      totalLoans: 15,
      onTimeReturns: 14,
      lateReturns: 1,
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
