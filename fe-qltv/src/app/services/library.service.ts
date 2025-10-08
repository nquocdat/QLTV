import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from './auth.service';

export interface LibraryStats {
  totalBooks: number;
  totalUsers: number;
  activeLoans: number;
  totalLoans: number;
}

@Injectable({
  providedIn: 'root',
})
export class LibraryService {
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

  getLibraryStats(): Observable<LibraryStats> {
    return this.http.get<any>(`${this.apiUrl}/dashboard`, this.getHttpOptions()).pipe(
      map((backendStats: any) => ({
        totalBooks: backendStats.totalBooks || 0,
        totalUsers: backendStats.totalUsers || backendStats.totalPatrons || 0, // Use totalUsers first, fallback to totalPatrons
        activeLoans: backendStats.activeLoans || 0,
        totalLoans: backendStats.totalLoans || 0,
      }))
    );
  }
}
