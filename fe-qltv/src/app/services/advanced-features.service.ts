import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
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
  private apiUrl = 'http://localhost:8081/api/membership';

  constructor(private http: HttpClient) {}

  // Membership Management - Call Real API
  getMembershipTiers(): Observable<MembershipTier[]> {
    return this.http.get<MembershipTier[]>(`${this.apiUrl}/tiers`);
  }

  getUserMembership(userId: number): Observable<UserMembership> {
    return this.http.get<UserMembership>(`${this.apiUrl}/users/${userId}`);
  }

  updateMembership(userId: number, data: Partial<UserMembership>): Observable<UserMembership> {
    return this.http.put<UserMembership>(`${this.apiUrl}/users/${userId}`, data);
  }

  upgradeMembership(userId: number, tierId: number): Observable<UserMembership> {
    return this.http.post<UserMembership>(`${this.apiUrl}/users/${userId}/upgrade`, { tierId });
  }

  addPoints(userId: number, points: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/users/${userId}/points`, { points });
  }

  getAllUserMemberships(): Observable<UserMembership[]> {
    return this.http.get<UserMembership[]>(`${this.apiUrl}/users`);
  }

  calculateMembershipUpgrade(
    userId: number
  ): Observable<{ eligible: boolean; nextTier?: MembershipTier; requirements?: any }> {
    // Mock implementation - can be replaced with real API
    return of({
      eligible: true,
      nextTier: undefined,
      requirements: {
        needMoreLoans: 25,
        needMorePoints: 150,
        maxViolationsAllowed: 1,
      },
    });
  }

  // Mock Analytics Data
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
    // Call real API instead of mock data
    return this.http.get<LibraryAnalytics>('http://localhost:8081/api/analytics/library');
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
