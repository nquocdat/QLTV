import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Patron } from '../models/patron.model';
import { PageResponse } from '../models/pagination.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private apiUrl = 'http://localhost:8081/api/admin';

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

  // User Management
  getAllUsers(): Observable<Patron[]> {
    return this.http.get<Patron[]>(`${this.apiUrl}/users`, this.getHttpOptions());
  }

  getAllUsersWithPagination(
    page: number,
    size: number,
    sortBy: string = 'name',
    sortDir: string = 'asc'
  ): Observable<PageResponse<Patron>> {
    const params = `page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
    return this.http.get<PageResponse<Patron>>(
      `${this.apiUrl}/users/paginated?${params}`,
      this.getHttpOptions()
    );
  }

  searchUsersWithPagination(
    searchTerm: string,
    page: number,
    size: number,
    sortBy: string = 'name',
    sortDir: string = 'asc'
  ): Observable<PageResponse<Patron>> {
    const params = `q=${encodeURIComponent(searchTerm)}&page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
    return this.http.get<PageResponse<Patron>>(
      `${this.apiUrl}/users/search/paginated?${params}`,
      this.getHttpOptions()
    );
  }

  getUsersByRole(role: string): Observable<Patron[]> {
    return this.http.get<Patron[]>(
      `${this.apiUrl}/users/role/${role}`,
      this.getHttpOptions()
    );
  }

  getUsersByRoleWithPagination(
    role: string,
    page: number,
    size: number,
    sortBy: string = 'name',
    sortDir: string = 'asc'
  ): Observable<PageResponse<Patron>> {
    const params = `page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
    return this.http.get<PageResponse<Patron>>(
      `${this.apiUrl}/users/role/${role}/paginated?${params}`,
      this.getHttpOptions()
    );
  }

  getActiveUsers(): Observable<Patron[]> {
    return this.http.get<Patron[]>(
      `${this.apiUrl}/users/active`,
      this.getHttpOptions()
    );
  }

  updateUserRole(userId: number, role: string): Observable<any> {
    return this.http.put(
      `${this.apiUrl}/users/${userId}/role`,
      { role },
      this.getHttpOptions()
    );
  }

  activateUser(userId: number): Observable<any> {
    return this.http.put(
      `${this.apiUrl}/users/${userId}/activate`,
      {},
      this.getHttpOptions()
    );
  }

  deactivateUser(userId: number): Observable<any> {
    return this.http.put(
      `${this.apiUrl}/users/${userId}/deactivate`,
      {},
      this.getHttpOptions()
    );
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(
      `${this.apiUrl}/users/${userId}`,
      this.getHttpOptions()
    );
  }

  // System Statistics
  getSystemStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats`, this.getHttpOptions());
  }
}
