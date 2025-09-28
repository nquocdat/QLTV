import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DashboardStats {
  totalBooks: number;
  totalUsers: number;
  activeLoans: number;
  overdueLoans: number;
  revenue: number;
  newBooksThisMonth: number;
  newUsersThisMonth: number;
  totalCategories: number;
  totalAuthors: number;
  totalPublishers: number;
}

export interface PopularBook {
  id: number;
  title: string;
  author: string;
  loanCount: number;
}

export interface RecentActivity {
  id: number;
  type: 'loan' | 'return' | 'user' | 'book';
  description: string;
  timestamp: Date;
  userId?: number;
  userName?: string;
}

export interface ChartData {
  labels: string[];
  values: number[];
}

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/api';

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard/stats`);
  }

  getPopularBooks(limit: number = 5): Observable<PopularBook[]> {
    return this.http.get<PopularBook[]>(`${this.apiUrl}/dashboard/popular-books?limit=${limit}`);
  }

  getRecentActivities(limit: number = 10): Observable<RecentActivity[]> {
    return this.http.get<RecentActivity[]>(
      `${this.apiUrl}/dashboard/recent-activities?limit=${limit}`
    );
  }

  getMonthlyLoanStats(months: number = 12): Observable<ChartData> {
    return this.http.get<ChartData>(`${this.apiUrl}/dashboard/monthly-loans?months=${months}`);
  }

  getCategoryDistribution(): Observable<ChartData> {
    return this.http.get<ChartData>(`${this.apiUrl}/dashboard/category-distribution`);
  }

  getUserRegistrationStats(months: number = 12): Observable<ChartData> {
    return this.http.get<ChartData>(`${this.apiUrl}/dashboard/user-registrations?months=${months}`);
  }

  getRevenueStats(months: number = 12): Observable<ChartData> {
    return this.http.get<ChartData>(`${this.apiUrl}/dashboard/revenue?months=${months}`);
  }
}
