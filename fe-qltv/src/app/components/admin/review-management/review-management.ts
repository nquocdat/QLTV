import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewService, ReviewDTO, Page } from '../../../services/review.service';

@Component({
  selector: 'app-review-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-management.html',
  styleUrls: ['./review-management.css'],
})
export class ReviewManagement implements OnInit {
  Math = Math; // Expose Math to template

  reviews: ReviewDTO[] = [];
  totalElements: number = 0;
  totalPages: number = 0;
  currentPage: number = 0;
  pageSize: number = 10;

  activeTab: 'pending' | 'approved' | 'all' = 'pending'; // Renamed from currentTab
  pendingCount: number = 0; // New property

  isLoading: boolean = false; // Renamed from loading
  message: string = '';
  messageType: 'success' | 'error' = 'success';

  constructor(private reviewService: ReviewService) {}

  ngOnInit(): void {
    this.loadReviews();
    this.loadPendingCount();
  }

  loadReviews(): void {
    this.isLoading = true;
    this.message = '';

    let request;
    switch (this.activeTab) {
      case 'pending':
        request = this.reviewService.getPendingReviews(this.currentPage, this.pageSize);
        break;
      case 'approved':
        request = this.reviewService.getApprovedReviews(this.currentPage, this.pageSize);
        break;
      case 'all':
        request = this.reviewService.getAllReviews(this.currentPage, this.pageSize);
        break;
    }

    request.subscribe({
      next: (data: Page<ReviewDTO>) => {
        this.reviews = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.currentPage = data.number;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading reviews:', err);
        this.showMessage('Lỗi khi tải danh sách đánh giá', 'error');
        this.isLoading = false;
      },
    });
  }

  loadPendingCount(): void {
    this.reviewService.getPendingReviews(0, 1).subscribe({
      next: (data) => {
        this.pendingCount = data.totalElements;
      },
      error: () => {
        this.pendingCount = 0;
      },
    });
  }

  onTabChange(tab: 'pending' | 'approved' | 'all'): void {
    this.activeTab = tab;
    this.currentPage = 0;
    this.loadReviews();
  }

  switchTab(tab: 'pending' | 'approved' | 'all'): void {
    this.onTabChange(tab);
  }

  approveReview(reviewId: number): void {
    if (!confirm('Bạn có chắc chắn muốn phê duyệt đánh giá này?')) {
      return;
    }

    this.reviewService.approveReview(reviewId).subscribe({
      next: () => {
        this.showMessage('Đã phê duyệt đánh giá thành công', 'success');
        this.loadReviews();
        this.loadPendingCount(); // Update pending count
      },
      error: (err) => {
        console.error('Error approving review:', err);
        this.showMessage('Lỗi khi phê duyệt đánh giá', 'error');
      },
    });
  }

  deleteReview(reviewId: number): void {
    if (!confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) {
      return;
    }

    this.reviewService.deleteReview(reviewId).subscribe({
      next: () => {
        this.showMessage('Đã xóa đánh giá thành công', 'success');
        this.loadReviews();
        this.loadPendingCount(); // Update pending count
      },
      error: (err) => {
        console.error('Error deleting review:', err);
        this.showMessage('Lỗi khi xóa đánh giá', 'error');
      },
    });
  }

  getRatingStars(rating: number): string[] {
    const stars: string[] = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;

    for (let i = 0; i < fullStars; i++) {
      stars.push('full');
    }

    if (hasHalfStar) {
      stars.push('half');
    }

    while (stars.length < 5) {
      stars.push('empty');
    }

    return stars;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadReviews();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadReviews();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadReviews();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPagesToShow = 5;
    let startPage = Math.max(0, this.currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + maxPagesToShow - 1);

    if (endPage - startPage < maxPagesToShow - 1) {
      startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  }

  goToPage(page: number): void {
    this.changePage(page);
  }

  getStatusBadgeClass(approved: boolean): string {
    return approved
      ? 'px-3 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800'
      : 'px-3 py-1 text-xs font-medium rounded-full bg-yellow-100 text-yellow-800';
  }

  getStatusText(approved: boolean): string {
    return approved ? 'Đã duyệt' : 'Chờ duyệt';
  }

  private showMessage(message: string, type: 'success' | 'error'): void {
    this.message = message;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 3000);
  }
}
