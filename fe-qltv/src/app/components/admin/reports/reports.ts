import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  ReportsService,
  OverviewStats,
  BookReport,
  LoanReport,
  UserReport,
  ReportFilter,
} from '../../../services/reports.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.html',
  styleUrl: './reports.css',
})
export class Reports implements OnInit {
  private reportsService = inject(ReportsService);

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

  constructor() {
    this.initializeDateRange();
  }

  ngOnInit(): void {
    this.generateReport();
  }

  initializeDateRange(): void {
    const today = new Date();
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);

    this.dateFrom = firstDayOfMonth.toISOString().split('T')[0];
    this.dateTo = today.toISOString().split('T')[0];
  }

  onReportTypeChange(): void {
    this.generateReport();
  }

  onDateChange(): void {
    this.generateReport();
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
}
