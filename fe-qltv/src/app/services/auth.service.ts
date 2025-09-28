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

  login(credentials: LoginRequest): Observable<JwtResponse> {
    console.log('AuthService sending to backend:', credentials);
    return this.http.post<JwtResponse>(`${this.apiUrl}/login`, credentials, this.httpOptions).pipe(
      tap((response) => {
        console.log('AuthService received from backend:', response);
        this.saveToken(response.token);
        this.saveUser(response);
        this.currentUserSubject.next(response);
      })
    );
  }

  register(userData: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData, this.httpOptions);
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      window.sessionStorage.clear();
    }
    this.currentUserSubject.next(null);
  }

  public saveToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      window.sessionStorage.removeItem(TOKEN_KEY);
      window.sessionStorage.setItem(TOKEN_KEY, token);
    }
  }

  public getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return window.sessionStorage.getItem(TOKEN_KEY);
    }
    return null;
  }

  public saveUser(user: JwtResponse): void {
    if (isPlatformBrowser(this.platformId)) {
      window.sessionStorage.removeItem(USER_KEY);
      window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    }
  }

  public getUser(): JwtResponse | null {
    if (isPlatformBrowser(this.platformId)) {
      const user = window.sessionStorage.getItem(USER_KEY);
      if (user) {
        return JSON.parse(user);
      }
    }
    return null;
  }

  public isLoggedIn(): boolean {
    return !!this.getToken();
  }

  public hasRole(role: string): boolean {
    const user = this.getUser();
    return user ? user.role === role : false;
  }

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
