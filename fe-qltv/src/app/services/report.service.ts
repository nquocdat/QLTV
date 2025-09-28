import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private apiUrl = 'http://localhost:8081/api/reports';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHttpOptions() {
    const token = this.authService.getToken();
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
      }),
    };
  }

  getDashboardStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/dashboard`, this.getHttpOptions());
  }

  getMonthlyLoanReport(startDate: string, endDate: string): Observable<any> {
    const params = `?startDate=${startDate}&endDate=${endDate}`;
    return this.http.get(`${this.apiUrl}/loans/monthly${params}`, this.getHttpOptions());
  }

  getPopularBooksReport(limit: number = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/books/popular?limit=${limit}`, this.getHttpOptions());
  }

  getActivePatronsReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/patrons/active`, this.getHttpOptions());
  }

  getOverdueReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/overdue`, this.getHttpOptions());
  }

  getFinesReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/fines`, this.getHttpOptions());
  }

  getGenreDistributionReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/books/genre-distribution`, this.getHttpOptions());
  }

  getDailyLoanReport(date: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/loans/daily?date=${date}`, this.getHttpOptions());
  }

  getInventoryReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/inventory`, this.getHttpOptions());
  }
}
