import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { LoanService } from '../../../services/loan.service';
import { ReviewService } from '../../../services/review.service';
import { JwtResponse } from '../../../models/patron.model';
import { Loan } from '../../../models/loan.model';
import {
  MembershipTier,
  UserMembership,
  UserRating,
} from '../../../models/advanced-features.model';
import { ReviewModal } from '../../modals/review-modal/review-modal';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, FormsModule, RouterModule, ReviewModal],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  returnLoan(loan: Loan): void {
    if (!loan.id) return;
    if (confirm(`Bạn có chắc muốn trả sách "${loan.bookTitle}"?`)) {
      this.loanService.returnBook(loan.id).subscribe({
        next: () => {
          alert('Trả sách thành công!');
          this.loadUserLoans();
        },
        error: (error) => {
          alert('Có lỗi khi trả sách!');
        },
      });
    }
  }
  currentUser: JwtResponse | null = null;
  userLoans: Loan[] = [];
  membershipTier: MembershipTier | null = null;
  userMembership: UserMembership | null = null;
  userRating: UserRating | null = null;

  // Review modal
  showReviewModal = false;
  selectedLoanForReview: Loan | null = null;
  loanReviewStatus: Map<number, boolean> = new Map(); // Track which loans have reviews

  // Tab management
  activeTab = 'loans'; // 'loans', 'history', 'membership'

  // Statistics
  stats = {
    totalLoans: 0,
    activeLoans: 0,
    onTimeReturns: 0,
    lateReturns: 0,
    points: 0,
  };

  constructor(
    private authService: AuthService,
    private loanService: LoanService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    this.loadUserData();
  }

  private loadUserData(): void {
    // Get current user
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      if (user) {
        this.loadUserLoans();
        this.loadUserMembership();
        this.loadUserRating();
      }
    });
  }

  private loadUserLoans(): void {
    if (this.currentUser?.id) {
      this.loanService.getLoansByPatronId(this.currentUser.id).subscribe({
        next: (loans: Loan[]) => {
          this.userLoans = loans;
          this.calculateStats();
          this.checkReviewStatus(); // Check which loans have reviews
        },
        error: (error) => {
          console.error('Error loading user loans:', error);
          this.userLoans = [];
          this.calculateStats();
        },
      });
    }
  }

  private loadUserMembership(): void {
    // TODO: Load from API
    this.membershipTier = {
      id: 1,
      name: 'Cơ bản',
      level: 'BASIC',
      benefits: [
        { type: 'MAX_BOOKS', value: 3, description: 'Tối đa 3 cuốn sách' },
        { type: 'LOAN_DURATION', value: 14, description: '14 ngày mượn sách' },
      ],
      requirements: { minLoans: 0 },
      color: 'bg-gray-100',
      icon: 'user',
    };

    this.userMembership = {
      userId: this.currentUser?.id || 1,
      tierId: 1,
      currentPoints: 120,
      totalLoans: this.stats.totalLoans,
      violations: 0,
      joinDate: new Date(),
      nextTierProgress: 60,
    };
  }

  private loadUserRating(): void {
    // TODO: Load from API
    this.userRating = {
      userId: this.currentUser?.id || 1,
      rating: 'GOOD',
      score: 85,
      violations: [],
      totalLoans: this.stats.totalLoans,
      onTimeReturns: this.stats.onTimeReturns,
      lateReturns: this.stats.lateReturns,
    };
  }

  private calculateStats(): void {
    this.stats.totalLoans = this.userLoans.length;
    this.stats.activeLoans = this.userLoans.filter(
      (loan) => loan.status === 'BORROWED' || loan.status === 'OVERDUE' || loan.status === 'RENEWED'
    ).length;
    this.stats.onTimeReturns = this.userLoans.filter(
      (loan) => loan.returnDate && new Date(loan.returnDate) <= new Date(loan.dueDate)
    ).length;
    this.stats.lateReturns = this.userLoans.filter(
      (loan) =>
        loan.status === 'OVERDUE' ||
        (loan.returnDate && new Date(loan.returnDate) > new Date(loan.dueDate))
    ).length;
    this.stats.points = this.userMembership?.currentPoints || 0;
  }

  getActiveLoans(): Loan[] {
    return this.userLoans.filter(
      (l) => l.status === 'BORROWED' || l.status === 'OVERDUE' || l.status === 'RENEWED'
    );
  }

  hasNoActiveLoans(): boolean {
    return this.getActiveLoans().length === 0;
  }

  getCompletedLoans(): Loan[] {
    return this.userLoans.filter((l) => l.status === 'RETURNED');
  }

  isReturnedOnTime(loan: Loan): boolean {
    if (!loan.returnDate || !loan.dueDate) return false;
    return new Date(loan.returnDate) <= new Date(loan.dueDate);
  }

  getReturnStatusClass(loan: Loan): string {
    return this.isReturnedOnTime(loan) ? 'text-green-600' : 'text-red-600';
  }

  getReturnStatusText(loan: Loan): string {
    return this.isReturnedOnTime(loan) ? 'Trả đúng hạn' : 'Trả trễ';
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  renewLoan(loan: Loan): void {
    if (loan.id) {
      this.loanService.renewLoan(loan.id).subscribe({
        next: () => {
          loan.renewalCount = (loan.renewalCount || 0) + 1;
          const currentDueDate = new Date(loan.dueDate);
          loan.dueDate = new Date(
            currentDueDate.getTime() + 14 * 24 * 60 * 60 * 1000
          ).toISOString();
          alert('Gia hạn thành công!');
        },
        error: (error) => {
          console.error('Error renewing loan:', error);
          alert('Không thể gia hạn sách này!');
        },
      });
    }
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      BORROWED: 'bg-green-100 text-green-800',
      RENEWED: 'bg-blue-100 text-blue-800',
      OVERDUE: 'bg-red-100 text-red-800',
      RETURNED: 'bg-gray-100 text-gray-800',
    };
    return classes[status] || 'bg-gray-100 text-gray-800';
  }

  getStatusText(status: string): string {
    const texts: { [key: string]: string } = {
      BORROWED: 'Đang mượn',
      RENEWED: 'Đã gia hạn',
      OVERDUE: 'Quá hạn',
      RETURNED: 'Đã trả',
    };
    return texts[status] || status;
  }

  getDaysUntilDue(dueDate: string): number {
    const today = new Date();
    const due = new Date(dueDate);
    const diffTime = due.getTime() - today.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  formatDate(date: string): string {
    return new Intl.DateTimeFormat('vi-VN').format(new Date(date));
  }

  // Review functionality
  private checkReviewStatus(): void {
    // Check review status for returned loans
    const returnedLoans = this.userLoans.filter((loan) => loan.status === 'RETURNED' && loan.id);

    returnedLoans.forEach((loan) => {
      if (loan.id) {
        this.reviewService.getReviewByLoanId(loan.id).subscribe({
          next: (review) => {
            if (review && loan.id) {
              this.loanReviewStatus.set(loan.id, true);
            }
          },
          error: () => {
            // No review exists for this loan
            if (loan.id) {
              this.loanReviewStatus.set(loan.id, false);
            }
          },
        });
      }
    });
  }

  canReviewLoan(loan: Loan): boolean {
    if (!loan.id) return false;
    return loan.status === 'RETURNED' && !this.loanReviewStatus.get(loan.id);
  }

  hasReview(loan: Loan): boolean {
    if (!loan.id) return false;
    return this.loanReviewStatus.get(loan.id) || false;
  }

  openReviewModal(loan: Loan): void {
    this.selectedLoanForReview = loan;
    this.showReviewModal = true;
  }

  closeReviewModal(): void {
    this.showReviewModal = false;
    this.selectedLoanForReview = null;
  }

  onReviewSubmitted(): void {
    this.closeReviewModal();
    // Refresh review status
    this.checkReviewStatus();
    alert('Đánh giá của bạn đã được gửi và đang chờ phê duyệt!');
  }
}
