import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OverviewStats {
  totalBooks: number;
  totalUsers: number;
  totalLoans: number;
  overdueLoans: number;
  activeLoans: number;
  totalRevenue: number;
  totalCategories: number;
  totalAuthors: number;
}

export interface BookReport {
  id: number;
  title: string;
  author: string;
  category: string;
  borrowCount: number;
  status: string;
  rating?: number;
  isbn?: string;
  publishYear?: number;
}

export interface LoanReport {
  id: number;
  loanCode: string;
  patronName: string;
  bookTitle: string;
  loanDate: Date;
  dueDate: Date;
  returnDate?: Date;
  status: 'ACTIVE' | 'RETURNED' | 'OVERDUE';
  fine?: number;
}

export interface UserReport {
  id: number;
  name: string;
  email: string;
  role: string;
  totalBorrows: number;
  activeBorrows: number;
  joinDate: Date;
  isActive: boolean;
  phone?: string;
}

export interface ReportFilter {
  dateFrom?: string;
  dateTo?: string;
  status?: string;
  category?: string;
  author?: string;
  page?: number;
  size?: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root',
})
export class ReportsService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/api/reports';

  getOverviewStats(filter?: ReportFilter): Observable<OverviewStats> {
    let params = new HttpParams();
    if (filter?.dateFrom) params = params.set('dateFrom', filter.dateFrom);
    if (filter?.dateTo) params = params.set('dateTo', filter.dateTo);

    return this.http.get<OverviewStats>(`${this.apiUrl}/overview`, { params });
  }

  getBookReports(filter?: ReportFilter): Observable<PagedResponse<BookReport>> {
    let params = new HttpParams();
    if (filter?.dateFrom) params = params.set('dateFrom', filter.dateFrom);
    if (filter?.dateTo) params = params.set('dateTo', filter.dateTo);
    if (filter?.category) params = params.set('category', filter.category);
    if (filter?.author) params = params.set('author', filter.author);
    if (filter?.page !== undefined) params = params.set('page', filter.page.toString());
    if (filter?.size !== undefined) params = params.set('size', filter.size.toString());

    return this.http.get<PagedResponse<BookReport>>(`${this.apiUrl}/books`, { params });
  }

  getLoanReports(filter?: ReportFilter): Observable<PagedResponse<LoanReport>> {
    let params = new HttpParams();
    if (filter?.dateFrom) params = params.set('dateFrom', filter.dateFrom);
    if (filter?.dateTo) params = params.set('dateTo', filter.dateTo);
    if (filter?.status) params = params.set('status', filter.status);
    if (filter?.page !== undefined) params = params.set('page', filter.page.toString());
    if (filter?.size !== undefined) params = params.set('size', filter.size.toString());

    return this.http.get<PagedResponse<LoanReport>>(`${this.apiUrl}/loans`, { params });
  }

  getUserReports(filter?: ReportFilter): Observable<PagedResponse<UserReport>> {
    let params = new HttpParams();
    if (filter?.dateFrom) params = params.set('dateFrom', filter.dateFrom);
    if (filter?.dateTo) params = params.set('dateTo', filter.dateTo);
    if (filter?.page !== undefined) params = params.set('page', filter.page.toString());
    if (filter?.size !== undefined) params = params.set('size', filter.size.toString());

    return this.http.get<PagedResponse<UserReport>>(`${this.apiUrl}/users`, { params });
  }

  exportBookReport(filter?: ReportFilter, format: 'excel' | 'pdf' = 'excel'): Observable<Blob> {
    let params = new HttpParams();
    if (filter?.dateFrom) params = params.set('dateFrom', filter.dateFrom);
    if (filter?.dateTo) params = params.set('dateTo', filter.dateTo);
    if (filter?.category) params = params.set('category', filter.category);
    params = params.set('format', format);

    return this.http.get(`${this.apiUrl}/books/export`, {
      params,
      responseType: 'blob',
    });
  }

  exportLoanReport(filter?: ReportFilter, format: 'excel' | 'pdf' = 'excel'): Observable<Blob> {
    let params = new HttpParams();
    if (filter?.dateFrom) params = params.set('dateFrom', filter.dateFrom);
    if (filter?.dateTo) params = params.set('dateTo', filter.dateTo);
    if (filter?.status) params = params.set('status', filter.status);
    params = params.set('format', format);

    return this.http.get(`${this.apiUrl}/loans/export`, {
      params,
      responseType: 'blob',
    });
  }

  exportUserReport(filter?: ReportFilter, format: 'excel' | 'pdf' = 'excel'): Observable<Blob> {
    let params = new HttpParams();
    if (filter?.dateFrom) params = params.set('dateFrom', filter.dateFrom);
    if (filter?.dateTo) params = params.set('dateTo', filter.dateTo);
    params = params.set('format', format);

    return this.http.get(`${this.apiUrl}/users/export`, {
      params,
      responseType: 'blob',
    });
  }
}
