import { Component, OnInit, inject, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import {
  DashboardService,
  DashboardStats,
  PopularBook,
  RecentActivity,
} from '../../../services/dashboard.service';
import { Chart, registerables } from 'chart.js';

// Register Chart.js components
Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit, AfterViewInit, OnDestroy {
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

  // Chart instances
  private loanTrendChart?: Chart;
  private categoryChart?: Chart;
  private topBooksChart?: Chart;

  ngOnInit(): void {
    this.loadDashboardData();
  }

  ngAfterViewInit(): void {
    // Wait for DOM to be ready before creating charts
    setTimeout(() => {
      this.createCharts();
    }, 100);
  }

  ngOnDestroy(): void {
    // Cleanup charts to prevent memory leaks
    this.destroyCharts();
  }

  private createCharts(): void {
    this.createLoanTrendChart();
    this.createCategoryChart();
    this.createTopBooksChart();
  }

  private destroyCharts(): void {
    if (this.loanTrendChart) {
      this.loanTrendChart.destroy();
      this.loanTrendChart = undefined;
    }
    if (this.categoryChart) {
      this.categoryChart.destroy();
      this.categoryChart = undefined;
    }
    if (this.topBooksChart) {
      this.topBooksChart.destroy();
      this.topBooksChart = undefined;
    }
  }

  private createLoanTrendChart(): void {
    const canvas = document.getElementById('loanTrendChart') as HTMLCanvasElement;
    if (!canvas) {
      console.warn('Loan trend chart canvas not found');
      return;
    }

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Destroy existing chart if any
    if (this.loanTrendChart) {
      this.loanTrendChart.destroy();
    }

    // Get data from service
    this.dashboardService.getMonthlyLoanStats(6).subscribe({
      next: (data) => {
        console.log('Loan trend data received:', data);

        // Check if data is empty
        if (!data || !data.labels || !data.values || data.labels.length === 0) {
          console.warn('No loan trend data available');
          return;
        }

        this.loanTrendChart = new Chart(ctx, {
          type: 'line',
          data: {
            labels: data.labels,
            datasets: [
              {
                label: 'Lượt mượn sách',
                data: data.values,
                borderColor: 'rgb(99, 102, 241)',
                backgroundColor: 'rgba(99, 102, 241, 0.1)',
                tension: 0.4,
                fill: true,
                pointRadius: 4,
                pointHoverRadius: 6,
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: true,
                position: 'top',
              },
              tooltip: {
                mode: 'index',
                intersect: false,
                callbacks: {
                  label: function (context) {
                    return `${context.dataset.label}: ${context.parsed.y} lượt`;
                  },
                },
              },
            },
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  precision: 0,
                },
              },
            },
          },
        });
      },
      error: (error) => {
        console.error('Error loading loan trend chart:', error);
      },
    });
  }

  private createCategoryChart(): void {
    const canvas = document.getElementById('categoryChart') as HTMLCanvasElement;
    if (!canvas) {
      console.warn('Category chart canvas not found');
      return;
    }

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    if (this.categoryChart) {
      this.categoryChart.destroy();
    }

    this.dashboardService.getCategoryDistribution().subscribe({
      next: (data) => {
        this.categoryChart = new Chart(ctx, {
          type: 'pie',
          data: {
            labels: data.labels,
            datasets: [
              {
                data: data.values,
                backgroundColor: [
                  '#EF4444', // Red
                  '#F59E0B', // Amber
                  '#10B981', // Green
                  '#3B82F6', // Blue
                  '#8B5CF6', // Purple
                  '#EC4899', // Pink
                  '#14B8A6', // Teal
                  '#F97316', // Orange
                ],
                borderWidth: 2,
                borderColor: '#ffffff',
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'right',
                labels: {
                  padding: 15,
                  font: {
                    size: 12,
                  },
                },
              },
              tooltip: {
                callbacks: {
                  label: function (context) {
                    const label = context.label || '';
                    const value = context.parsed || 0;
                    const dataset = context.dataset;
                    const total = dataset.data.reduce((acc: number, val: number) => acc + val, 0);
                    const percentage = ((value / total) * 100).toFixed(1);
                    return `${label}: ${value} (${percentage}%)`;
                  },
                },
              },
            },
          },
        });
      },
      error: (error) => {
        console.error('Error loading category chart:', error);
      },
    });
  }

  private createTopBooksChart(): void {
    const canvas = document.getElementById('topBooksChart') as HTMLCanvasElement;
    if (!canvas) {
      console.warn('Top books chart canvas not found');
      return;
    }

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    if (this.topBooksChart) {
      this.topBooksChart.destroy();
    }

    // Wait for popular books to load
    if (this.popularBooks && this.popularBooks.length > 0) {
      const labels = this.popularBooks.map((book) => {
        // Truncate long titles
        return book.title.length > 30 ? book.title.substring(0, 27) + '...' : book.title;
      });
      const data = this.popularBooks.map((book) => book.loanCount);

      this.topBooksChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [
            {
              label: 'Số lượt mượn',
              data: data,
              backgroundColor: 'rgba(99, 102, 241, 0.8)',
              borderColor: 'rgb(99, 102, 241)',
              borderWidth: 1,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          indexAxis: 'y', // Horizontal bars
          plugins: {
            legend: {
              display: false,
            },
            tooltip: {
              callbacks: {
                label: function (context) {
                  return `${context.parsed.x} lượt mượn`;
                },
              },
            },
          },
          scales: {
            x: {
              beginAtZero: true,
              ticks: {
                precision: 0,
              },
            },
          },
        },
      });
    }
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
        // Recreate top books chart with new data
        setTimeout(() => {
          this.createTopBooksChart();
        }, 100);
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
