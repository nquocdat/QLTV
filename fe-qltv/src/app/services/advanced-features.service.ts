import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import {
  MembershipTier,
  UserMembership,
  UserRating,
  Notification,
  LibraryAnalytics,
  BookStatistics,
  UserViolation,
} from '../models/advanced-features.model';

@Injectable({
  providedIn: 'root',
})
export class AdvancedFeaturesService {
  private membershipTiers: MembershipTier[] = [
    {
      id: 1,
      name: 'Thành viên Cơ bản',
      level: 'BASIC',
      color: 'bg-gray-100 text-gray-800',
      icon: 'user',
      requirements: { minLoans: 0, minPoints: 0, maxViolations: 5 },
      benefits: [
        { type: 'MAX_BOOKS', value: 3, description: 'Mượn tối đa 3 cuốn cùng lúc' },
        { type: 'LOAN_DURATION', value: 14, description: 'Thời gian mượn 14 ngày' },
        { type: 'LATE_FEE_DISCOUNT', value: 0, description: 'Không giảm phí phạt' },
      ],
    },
    {
      id: 2,
      name: 'Thành viên VIP',
      level: 'VIP',
      color: 'bg-blue-100 text-blue-800',
      icon: 'star',
      requirements: { minLoans: 20, minPoints: 100, maxViolations: 2 },
      benefits: [
        { type: 'MAX_BOOKS', value: 5, description: 'Mượn tối đa 5 cuốn cùng lúc' },
        { type: 'LOAN_DURATION', value: 21, description: 'Thời gian mượn 21 ngày' },
        { type: 'LATE_FEE_DISCOUNT', value: 20, description: 'Giảm 20% phí phạt trễ hạn' },
        { type: 'RESERVATION_PRIORITY', value: true, description: 'Ưu tiên đặt trước sách' },
      ],
    },
    {
      id: 3,
      name: 'Thành viên Premium',
      level: 'PREMIUM',
      color: 'bg-gold-100 text-gold-800',
      icon: 'crown',
      requirements: { minLoans: 50, minPoints: 300, maxViolations: 1 },
      benefits: [
        { type: 'MAX_BOOKS', value: 10, description: 'Mượn tối đa 10 cuốn cùng lúc' },
        { type: 'LOAN_DURATION', value: 30, description: 'Thời gian mượn 30 ngày' },
        { type: 'LATE_FEE_DISCOUNT', value: 50, description: 'Giảm 50% phí phạt trễ hạn' },
        { type: 'RESERVATION_PRIORITY', value: true, description: 'Ưu tiên đặt trước sách' },
        { type: 'EARLY_ACCESS', value: true, description: 'Truy cập sớm sách mới' },
      ],
    },
  ];

  private mockAnalytics: LibraryAnalytics = {
    bookStatusStats: {
      available: 850,
      borrowed: 280,
      reserved: 45,
      damaged: 12,
      lost: 8,
      maintenance: 5,
    },
    topReaders: [
      { userId: 1, userName: 'Nguyễn Văn A', totalLoans: 45, currentTier: 'Premium' },
      { userId: 2, userName: 'Trần Thị B', totalLoans: 38, currentTier: 'VIP' },
      { userId: 3, userName: 'Lê Văn C', totalLoans: 32, currentTier: 'VIP' },
    ],
    frequentLateReturners: [
      { userId: 4, userName: 'Phạm Thị D', lateReturns: 8, totalLoans: 20, rating: 'POOR' },
      { userId: 5, userName: 'Hoàng Văn E', lateReturns: 5, totalLoans: 15, rating: 'AVERAGE' },
    ],
    loanTrends: [
      { period: '2024-09', totalLoans: 234, newMembers: 15, returnRate: 95.2 },
      { period: '2024-08', totalLoans: 198, newMembers: 12, returnRate: 97.1 },
      { period: '2024-07', totalLoans: 245, newMembers: 18, returnRate: 94.8 },
    ],
    popularBooks: [
      {
        bookId: 1,
        title: 'Nhà Giả Kim',
        author: 'Paulo Coelho',
        loanCount: 28,
        rating: 4.8,
        category: 'Tiểu thuyết',
      },
      {
        bookId: 2,
        title: 'Sapiens',
        author: 'Yuval Harari',
        loanCount: 25,
        rating: 4.7,
        category: 'Lịch sử',
      },
    ],
    membershipDistribution: [
      { tierName: 'Cơ bản', memberCount: 180, percentage: 60, color: '#6B7280' },
      { tierName: 'VIP', memberCount: 90, percentage: 30, color: '#3B82F6' },
      { tierName: 'Premium', memberCount: 30, percentage: 10, color: '#F59E0B' },
    ],
  };

  constructor() {}

  // Membership Management
  getMembershipTiers(): Observable<MembershipTier[]> {
    return of(this.membershipTiers);
  }

  getUserMembership(userId: number): Observable<UserMembership> {
    // Mock user membership data
    const mockMembership: UserMembership = {
      userId: userId,
      tierId: 2, // VIP
      currentPoints: 150,
      totalLoans: 25,
      violations: 1,
      joinDate: new Date('2024-01-15'),
      nextTierProgress: 75, // 75% to next tier
    };
    return of(mockMembership);
  }

  calculateMembershipUpgrade(
    userId: number
  ): Observable<{ eligible: boolean; nextTier?: MembershipTier; requirements?: any }> {
    // Logic to check if user can upgrade membership
    return of({
      eligible: true,
      nextTier: this.membershipTiers[2], // Premium
      requirements: {
        needMoreLoans: 25,
        needMorePoints: 150,
        maxViolationsAllowed: 1,
      },
    });
  }

  // User Rating System
  getUserRating(userId: number): Observable<UserRating> {
    const mockRating: UserRating = {
      userId: userId,
      rating: 'GOOD',
      score: 85,
      violations: [],
      totalLoans: 25,
      onTimeReturns: 22,
      lateReturns: 3,
    };
    return of(mockRating);
  }

  addViolation(
    userId: number,
    violation: Omit<UserViolation, 'id' | 'userId'>
  ): Observable<boolean> {
    // Add violation to user record
    console.log('Adding violation for user', userId, violation);
    return of(true);
  }

  // Notification System
  getNotifications(userId: number): Observable<Notification[]> {
    const mockNotifications: Notification[] = [
      {
        id: 1,
        userId: userId,
        type: 'REMINDER',
        title: 'Nhắc nhở trả sách',
        message: 'Sách "Nhà Giả Kim" sẽ đến hạn trả vào ngày mai',
        sentDate: new Date(),
        emailSent: true,
      },
      {
        id: 2,
        userId: userId,
        type: 'BOOK_AVAILABLE',
        title: 'Sách đã sẵn sàng',
        message: 'Sách "Sapiens" mà bạn đặt trước đã có sẵn',
        sentDate: new Date(Date.now() - 86400000),
        emailSent: true,
      },
    ];
    return of(mockNotifications);
  }

  sendNotification(notification: Omit<Notification, 'id' | 'sentDate'>): Observable<boolean> {
    console.log('Sending notification:', notification);
    // In real implementation, this would send email/SMS
    return of(true);
  }

  // Analytics and Reports
  getLibraryAnalytics(): Observable<LibraryAnalytics> {
    return of(this.mockAnalytics);
  }

  getBookStatistics(bookId: number): Observable<BookStatistics> {
    const mockStats: BookStatistics = {
      bookId: bookId,
      totalLoans: 28,
      currentStatus: 'available',
      averageRating: 4.7,
      lastLoanDate: new Date('2024-09-20'),
      damageReports: 0,
      popularityScore: 85,
    };
    return of(mockStats);
  }

  // Auto notification scheduling
  scheduleReminderNotifications(): Observable<boolean> {
    // This would typically run as a background job
    console.log('Scheduling reminder notifications for due books');
    return of(true);
  }

  generateMonthlyReport(month: string, year: number): Observable<any> {
    // Generate comprehensive monthly report
    const report = {
      period: `${month}/${year}`,
      totalLoans: 234,
      newRegistrations: 15,
      returnRate: 95.2,
      topCategories: ['Văn học', 'Khoa học', 'Công nghệ'],
      membershipUpgrades: 8,
      violations: 12,
      revenue: 2500000,
    };
    return of(report);
  }
}
