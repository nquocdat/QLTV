import { Component, OnInit, AfterViewInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Chart, registerables } from 'chart.js';

import {
  ReportsService,
  OverviewStats,
  BookReport,
  LoanReport,
  UserReport,
  ReportFilter,
} from '../../../services/reports.service';
import { DashboardService } from '../../../services/dashboard.service';

// Register Chart.js components
Chart.register(...registerables);

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.html',
  styleUrl: './reports.css',
})
export class Reports implements OnInit, AfterViewInit, OnDestroy {
  private reportsService = inject(ReportsService);
  private dashboardService = inject(DashboardService);

  selectedReportType = 'overview';
  dateFrom = '';
  dateTo = '';
  isLoading = false;

  overviewStats: OverviewStats = {
    totalBooks: 0,
    totalUsers: 0,
    totalLoans: 0,
    overdueLoans: 0,
    activeLoans: 0,
    totalRevenue: 0,
    totalCategories: 0,
    totalAuthors: 0,
  };

  booksReport: BookReport[] = [];
  loansReport: LoanReport[] = [];
  usersReport: UserReport[] = [];

  // Chart instances
  private loanTrendChart?: Chart;
  private categoryChart?: Chart;

  constructor() {
    this.initializeDateRange();
  }

  ngOnInit(): void {
    this.generateReport();
  }

  ngAfterViewInit(): void {
    // Wait for DOM and data to be ready before creating charts
    setTimeout(() => {
      if (this.selectedReportType === 'overview') {
        this.createCharts();
      }
    }, 500);
  }

  ngOnDestroy(): void {
    // Cleanup charts to prevent memory leaks
    this.destroyCharts();
  }

  initializeDateRange(): void {
    const today = new Date();
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);

    this.dateFrom = firstDayOfMonth.toISOString().split('T')[0];
    this.dateTo = today.toISOString().split('T')[0];
  }

  onReportTypeChange(): void {
    this.generateReport();

    // Recreate charts if switching to overview
    if (this.selectedReportType === 'overview') {
      setTimeout(() => {
        this.destroyCharts();
        this.createCharts();
      }, 300);
    }
  }

  onDateChange(): void {
    this.generateReport();

    // Refresh charts with new date range
    if (this.selectedReportType === 'overview') {
      setTimeout(() => {
        this.destroyCharts();
        this.createCharts();
      }, 300);
    }
  }

  generateReport(): void {
    console.log(
      `Generating ${this.selectedReportType} report from ${this.dateFrom} to ${this.dateTo}`
    );

    // Load real data from backend APIs
    this.loadReportData();
  }

  private loadReportData(): void {
    // Reset data arrays
    this.booksReport = [];
    this.loansReport = [];
    this.usersReport = [];

    // Load data based on report type
    switch (this.selectedReportType) {
      case 'books':
        this.loadBooksReport();
        break;
      case 'loans':
        this.loadLoansReport();
        break;
      case 'users':
        this.loadUsersReport();
        break;
      case 'overview':
        this.loadOverviewReport();
        break;
    }
  }

  private loadBooksReport(): void {
    this.isLoading = true;
    const filter: ReportFilter = {
      dateFrom: this.dateFrom,
      dateTo: this.dateTo,
    };

    this.reportsService.getBookReports(filter).subscribe({
      next: (response) => {
        this.booksReport = response.content || [];
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading books report:', error);
        this.booksReport = [];
        this.isLoading = false;
      },
    });
  }

  private loadLoansReport(): void {
    this.isLoading = true;
    const filter: ReportFilter = {
      dateFrom: this.dateFrom,
      dateTo: this.dateTo,
    };

    this.reportsService.getLoanReports(filter).subscribe({
      next: (response) => {
        this.loansReport = response.content || [];
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading loans report:', error);
        this.loansReport = [];
        this.isLoading = false;
      },
    });
  }

  private loadUsersReport(): void {
    this.isLoading = true;
    const filter: ReportFilter = {
      dateFrom: this.dateFrom,
      dateTo: this.dateTo,
    };

    this.reportsService.getUserReports(filter).subscribe({
      next: (response) => {
        this.usersReport = response.content || [];
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading users report:', error);
        this.usersReport = [];
        this.isLoading = false;
      },
    });
  }

  private loadOverviewReport(): void {
    this.isLoading = true;
    const filter: ReportFilter = {
      dateFrom: this.dateFrom,
      dateTo: this.dateTo,
    };

    this.reportsService.getOverviewStats(filter).subscribe({
      next: (data: OverviewStats) => {
        this.overviewStats = data;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading overview report:', error);
        // Keep default values on error
        this.isLoading = false;
      },
    });
  }

  exportReport(): void {
    const reportData = this.getReportData();
    console.log('Exporting report:', reportData);

    // Create CSV content
    let csvContent = this.generateCSV(reportData);

    // Download CSV file
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `${this.selectedReportType}_report_${new Date().getTime()}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  getReportData(): any[] {
    switch (this.selectedReportType) {
      case 'books':
        return this.booksReport;
      case 'loans':
        return this.loansReport;
      case 'users':
        return this.usersReport;
      default:
        return [];
    }
  }

  generateCSV(data: any[]): string {
    if (data.length === 0) return '';

    const headers = Object.keys(data[0]);
    const csvRows = [];

    // Add headers
    csvRows.push(headers.join(','));

    // Add data rows
    for (const row of data) {
      const values = headers.map((header) => {
        const value = row[header];
        return typeof value === 'string' ? `"${value}"` : value;
      });
      csvRows.push(values.join(','));
    }

    return csvRows.join('\n');
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-blue-100 text-blue-800';
      case 'RETURNED':
        return 'bg-green-100 text-green-800';
      case 'OVERDUE':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusDisplayName(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Đang mượn';
      case 'RETURNED':
        return 'Đã trả';
      case 'OVERDUE':
        return 'Quá hạn';
      default:
        return status;
    }
  }

  // ============ CHART METHODS ============

  private createCharts(): void {
    this.createLoanTrendChart();
    this.createCategoryChart();
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

    // Calculate months based on date range
    const months = this.calculateMonthsDifference();

    // Get data from service
    this.dashboardService.getMonthlyLoanStats(months).subscribe({
      next: (data) => {
        this.loanTrendChart = new Chart(ctx, {
          type: 'line',
          data: {
            labels: data.labels,
            datasets: [
              {
                label: 'Lượt mượn sách',
                data: data.values,
                borderColor: 'rgb(59, 130, 246)',
                backgroundColor: 'rgba(59, 130, 246, 0.1)',
                tension: 0.4,
                fill: true,
                pointRadius: 4,
                pointHoverRadius: 6,
                pointBackgroundColor: 'rgb(59, 130, 246)',
                pointBorderColor: '#fff',
                pointBorderWidth: 2,
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
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                titleColor: '#fff',
                bodyColor: '#fff',
                borderColor: 'rgba(59, 130, 246, 0.5)',
                borderWidth: 1,
                padding: 12,
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
                grid: {
                  color: 'rgba(0, 0, 0, 0.05)',
                },
              },
              x: {
                grid: {
                  display: false,
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
          type: 'doughnut',
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
                  '#6366F1', // Indigo
                  '#84CC16', // Lime
                ],
                borderWidth: 2,
                borderColor: '#ffffff',
                hoverBorderWidth: 3,
                hoverBorderColor: '#ffffff',
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
                  usePointStyle: true,
                  pointStyle: 'circle',
                },
              },
              tooltip: {
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                titleColor: '#fff',
                bodyColor: '#fff',
                borderColor: 'rgba(255, 255, 255, 0.3)',
                borderWidth: 1,
                padding: 12,
                callbacks: {
                  label: function (context) {
                    const label = context.label || '';
                    const value = context.parsed || 0;
                    const dataset = context.dataset;
                    const total = dataset.data.reduce((acc: number, val: number) => acc + val, 0);
                    const percentage = ((value / total) * 100).toFixed(1);
                    return `${label}: ${value} sách (${percentage}%)`;
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

  private calculateMonthsDifference(): number {
    if (!this.dateFrom || !this.dateTo) return 6;

    const from = new Date(this.dateFrom);
    const to = new Date(this.dateTo);

    const months = (to.getFullYear() - from.getFullYear()) * 12 + (to.getMonth() - from.getMonth());

    return Math.max(1, months + 1); // At least 1 month
  }
}
