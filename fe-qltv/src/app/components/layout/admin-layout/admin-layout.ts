import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { JwtResponse } from '../../../models/patron.model';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-admin-layout',
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout implements OnInit {
  currentUser: JwtResponse | null = null;
  currentRoute = '';

  private pageTitles: { [key: string]: string } = {
    '/admin/dashboard': 'Tổng quan hệ thống',
    '/admin/users': 'Quản lý người dùng',
    '/admin/books': 'Quản lý sách',
    '/admin/loans': 'Quản lý mượn trả',
    '/admin/categories': 'Quản lý thể loại',
    '/admin/authors': 'Quản lý tác giả',
    '/admin/publishers': 'Quản lý nhà xuất bản',
    '/admin/memberships': 'Quản lý thành viên',
    '/admin/analytics': 'Phân tích dữ liệu',
    '/admin/reports': 'Báo cáo thống kê',
    '/admin/reviews': 'Quản lý đánh giá',
    '/admin/payments': 'Quản lý thanh toán',
    '/admin/pending-payments': 'Xác nhận thanh toán',
    '/admin/copies': 'Quản lý bản sao',
    '/librarian/dashboard': 'Tổng quan hệ thống',
    '/librarian/books': 'Quản lý sách',
    '/librarian/loans': 'Quản lý mượn trả',
    '/librarian/categories': 'Quản lý thể loại',
    '/librarian/authors': 'Quản lý tác giả',
    '/librarian/publishers': 'Quản lý nhà xuất bản',
    '/librarian/memberships': 'Quản lý thành viên',
    '/librarian/analytics': 'Phân tích dữ liệu',
    '/librarian/reports': 'Báo cáo thống kê',
    '/librarian/pending-payments': 'Xác nhận thanh toán',
    '/librarian/copies': 'Quản lý bản sao',
  };

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    // Get current user
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });

    // Track route changes
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.currentRoute = event.url;
      });
  }

  getPageTitle(): string {
    return this.pageTitles[this.currentRoute] || 'Quản trị hệ thống';
  }

  getBasePath(): string {
    return this.currentRoute.startsWith('/admin') ? '/admin' : '/librarian';
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ROLE_ADMIN' || false;
  }

  getUserInitials(): string {
    if (this.currentUser?.name) {
      return this.currentUser.name
        .split(' ')
        .map((name) => name.charAt(0).toUpperCase())
        .slice(0, 2)
        .join('');
    }
    return 'A';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/library/home']);
  }

  navigateToHome(): void {
    this.router.navigate(['/library/home']);
  }
}
