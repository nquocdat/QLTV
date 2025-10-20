import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';
import { LoginRequest, RegisterRequest, JwtResponse } from '../models/patron.model';

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/auth';
  private currentUserSubject = new BehaviorSubject<JwtResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private http: HttpClient, @Inject(PLATFORM_ID) private platformId: Object) {
    const user = this.getUser();
    if (user) {
      this.currentUserSubject.next(user);
    }
  }

  /** Đăng nhập */
  login(credentials: LoginRequest): Observable<JwtResponse> {
    console.log('AuthService sending to backend:', credentials);
    return this.http.post<JwtResponse>(`${this.apiUrl}/login`, credentials, this.httpOptions).pipe(
      tap((response) => {
        console.log('AuthService received from backend:', response);

        // ✅ Kiểm tra field token từ backend (thường là 'token' hoặc 'accessToken')
        const token = (response as any).token || (response as any).accessToken;
        if (token) {
          this.saveToken(token);
          this.saveUser(response);
          this.currentUserSubject.next(response);
        } else {
          console.error('⚠️ Backend response missing token field:', response);
        }
      })
    );
  }

  /** Đăng ký */
  register(userData: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData, this.httpOptions);
  }

  /** Đăng xuất */
  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
    this.currentUserSubject.next(null);
  }

  /** ✅ Lưu token (dùng localStorage để không mất khi reload) */
  public saveToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(TOKEN_KEY, token);
    }
  }

  /** ✅ Lấy token */
  public getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(TOKEN_KEY);
    }
    return null;
  }

  /** ✅ Lưu thông tin user */
  public saveUser(user: JwtResponse): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    }
  }

  /** ✅ Lấy thông tin user */
  public getUser(): JwtResponse | null {
    if (isPlatformBrowser(this.platformId)) {
      const user = localStorage.getItem(USER_KEY);
      if (user) {
        return JSON.parse(user);
      }
    }
    return null;
  }

  /** Kiểm tra đã đăng nhập chưa */
  public isLoggedIn(): boolean {
    return !!this.getToken();
  }

  /** Kiểm tra vai trò */
  public hasRole(role: string): boolean {
    const user = this.getUser();
    return user ? user.role === role : false;
  }

  /** Kiểm tra role cụ thể */
  public isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  public isLibrarian(): boolean {
    return this.hasRole('ROLE_LIBRARIAN');
  }

  public isUser(): boolean {
    return this.hasRole('ROLE_USER');
  }
}
