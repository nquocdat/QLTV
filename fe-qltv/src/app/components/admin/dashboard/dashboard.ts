import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import {
  DashboardService,
  DashboardStats,
  PopularBook,
  RecentActivity,
} from '../../../services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private dashboardService = inject(DashboardService);
  private router = inject(Router);

  stats: DashboardStats = {
    totalBooks: 0,
    totalUsers: 0,
    activeLoans: 0,
    overdueLoans: 0,
    revenue: 0,
    newBooksThisMonth: 0,
    newUsersThisMonth: 0,
    totalCategories: 0,
    totalAuthors: 0,
    totalPublishers: 0,
  };

  popularBooks: PopularBook[] = [];

  recentActivities: RecentActivity[] = [];
  isLoading = false;

  ngOnInit(): void {
    this.loadDashboardData();
  }

  getActivityIconClass(type: string): string {
    const classes = {
      loan: 'bg-blue-500',
      return: 'bg-green-500',
      user: 'bg-purple-500',
    };
    return classes[type as keyof typeof classes] || 'bg-gray-500';
  }

  openAddBookModal(): void {
    // Navigate to book management with trigger to open add modal
    this.router.navigate(['/admin/books']).then(() => {
      // Dispatch custom event to trigger add book modal
      window.dispatchEvent(new CustomEvent('triggerAddBook'));
    });
  }

  private loadDashboardData(): void {
    this.isLoading = true;

    // Load dashboard statistics
    this.dashboardService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (error) => {
        console.error('Error loading dashboard stats:', error);
        // Fallback to default values
        this.stats = {
          totalBooks: 0,
          totalUsers: 0,
          activeLoans: 0,
          overdueLoans: 0,
          revenue: 0,
          newBooksThisMonth: 0,
          newUsersThisMonth: 0,
          totalCategories: 0,
          totalAuthors: 0,
          totalPublishers: 0,
        };
      },
    });

    // Load popular books
    this.dashboardService.getPopularBooks(5).subscribe({
      next: (books) => {
        this.popularBooks = books;
      },
      error: (error) => {
        console.error('Error loading popular books:', error);
        this.popularBooks = [];
      },
    });

    // Load recent activities
    this.dashboardService.getRecentActivities(5).subscribe({
      next: (activities) => {
        this.recentActivities = activities;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading recent activities:', error);
        this.recentActivities = [];
        this.isLoading = false;
      },
    });
    this.recentActivities = [];
  }
}
