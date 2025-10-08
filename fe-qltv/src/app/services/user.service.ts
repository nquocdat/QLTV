// ...existing code...
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Patron } from '../models/patron.model';

export interface UserCreateRequest {
  name: string;
  email: string;
  phoneNumber: string;
  address: string;
  role: string;
  isActive: boolean;
}

export interface UserUpdateRequest extends UserCreateRequest {
  id: number;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ChangePasswordResponse {
  message?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  // Lấy thông tin user đang đăng nhập
  getCurrentUser(): Observable<Patron | null> {
    const userJson = sessionStorage.getItem('auth-user');
    if (userJson) {
      const currentUser = JSON.parse(userJson);
      return this.getUserById(Number(currentUser.id));
    }
    return new Observable<Patron | null>((observer) => {
      observer.next(null);
      observer.complete();
    });
  }
  private apiUrl = 'http://localhost:8081/api/patrons';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<Patron[]> {
    return this.http.get<Patron[]>(this.apiUrl);
  }

  getUserById(id: number): Observable<Patron> {
    return this.http.get<Patron>(`${this.apiUrl}/${id}`);
  }

  createUser(user: UserCreateRequest): Observable<Patron> {
    return this.http.post<Patron>(this.apiUrl, user);
  }

  updateUser(id: number, user: UserUpdateRequest): Observable<Patron> {
    return this.http.put<Patron>(`${this.apiUrl}/${id}`, user);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  activateUser(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/activate`, {});
  }

  deactivateUser(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  toggleUserStatus(id: number): Observable<Patron> {
    return this.http.put<Patron>(`${this.apiUrl}/${id}/toggle-status`, {});
  }

  searchUsers(searchTerm: string): Observable<Patron[]> {
    const params = new HttpParams().set('q', searchTerm);
    return this.http.get<Patron[]>(`${this.apiUrl}/search`, { params });
  }

  getUsersByRole(role: string): Observable<Patron[]> {
    return this.http.get<Patron[]>(`${this.apiUrl}/role/${role}`);
  }

  getActiveUsers(): Observable<Patron[]> {
    return this.http.get<Patron[]>(`${this.apiUrl}/active`);
  }

  changePassword(
    userId: number,
    request: ChangePasswordRequest
  ): Observable<ChangePasswordResponse> {
    return this.http.put<ChangePasswordResponse>(
      `${this.apiUrl}/${userId}/change-password`,
      request
    );
  }
}
