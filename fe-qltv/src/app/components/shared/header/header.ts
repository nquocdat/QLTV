import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../../../services/category.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { JwtResponse } from '../../../models/patron.model';

@Component({
  selector: 'app-header',
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header implements OnInit {
  goToProfile(): void {
    this.closeUserMenu();
    this.router.navigate(['/library/profile']);
  }
  categories: any[] = [];
  showCategoryDropdown = false;
  isLoggedIn = false;
  isAdmin = false;
  isLibrarian = false;
  currentUser: JwtResponse | null = null;
  searchQuery = '';
  isUserMenuOpen = false;
  isMobileMenuOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    // Subscribe to authentication changes
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      this.isLoggedIn = !!user;
      this.isAdmin = this.authService.isAdmin();
      this.isLibrarian = this.authService.isLibrarian();
    });

    // Lấy danh sách thể loại sách
    this.categoryService.getAllCategories().subscribe({
      next: (categories: any[]) => {
        this.categories = categories;
      },
      error: () => {
        this.categories = [];
      },
    });
  }

  browseCategory(categoryId: number): void {
    this.router.navigate(['/library/books'], {
      queryParams: { category: categoryId },
    });
    this.showCategoryDropdown = false;
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      // Navigate to search results
      this.router.navigate(['/library/books'], {
        queryParams: { search: this.searchQuery.trim() },
      });
    }
  }

  toggleUserMenu(): void {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  closeUserMenu(): void {
    this.isUserMenuOpen = false;
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }

  getUserInitials(): string {
    if (this.currentUser?.name) {
      return this.currentUser.name
        .split(' ')
        .map((name) => name.charAt(0).toUpperCase())
        .slice(0, 2)
        .join('');
    }
    return 'U';
  }

  logout(): void {
    this.authService.logout();
    this.isUserMenuOpen = false;
    this.isMobileMenuOpen = false;
    this.router.navigate(['/login']);
  }
}
