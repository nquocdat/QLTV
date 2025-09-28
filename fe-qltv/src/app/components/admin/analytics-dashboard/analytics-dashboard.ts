import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdvancedFeaturesService } from '../../../services/advanced-features.service';
import { LibraryAnalytics, MembershipTier } from '../../../models/advanced-features.model';

@Component({
  selector: 'app-analytics-dashboard',
  imports: [CommonModule, RouterModule],
  templateUrl: './analytics-dashboard.html',
  styleUrl: './analytics-dashboard.css',
})
export class AnalyticsDashboard implements OnInit {
  analytics: LibraryAnalytics | null = null;
  membershipTiers: MembershipTier[] = [];
  loading = true;

  // Chart data
  bookStatusChartData: any[] = [];
  loanTrendChartData: any[] = [];
  membershipChartData: any[] = [];

  constructor(private advancedFeaturesService: AdvancedFeaturesService) {}

  ngOnInit(): void {
    this.loadAnalytics();
    this.loadMembershipTiers();
  }

  private loadAnalytics(): void {
    this.advancedFeaturesService.getLibraryAnalytics().subscribe({
      next: (data) => {
        this.analytics = data;
        this.prepareChartData();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading analytics:', error);
        this.loading = false;
      },
    });
  }

  private loadMembershipTiers(): void {
    this.advancedFeaturesService.getMembershipTiers().subscribe({
      next: (tiers) => {
        this.membershipTiers = tiers;
      },
    });
  }

  private prepareChartData(): void {
    if (!this.analytics) return;

    // Book status chart data
    this.bookStatusChartData = Object.entries(this.analytics.bookStatusStats).map(
      ([status, count]) => ({
        label: this.getStatusLabel(status),
        value: count,
        color: this.getStatusColor(status),
      })
    );

    // Loan trend chart data
    this.loanTrendChartData = this.analytics.loanTrends.map((trend) => ({
      period: trend.period,
      loans: trend.totalLoans,
      returnRate: trend.returnRate,
    }));

    // Membership distribution chart data
    this.membershipChartData = this.analytics.membershipDistribution;
  }

  private getStatusLabel(status: string): string {
    const statusLabels: { [key: string]: string } = {
      available: 'Có sẵn',
      borrowed: 'Đang mượn',
      reserved: 'Đã đặt',
      damaged: 'Hỏng',
      lost: 'Mất',
      maintenance: 'Bảo trì',
    };
    return statusLabels[status] || status;
  }

  private getStatusColor(status: string): string {
    const statusColors: { [key: string]: string } = {
      available: '#10B981', // green
      borrowed: '#F59E0B', // amber
      reserved: '#3B82F6', // blue
      damaged: '#EF4444', // red
      lost: '#DC2626', // dark red
      maintenance: '#6B7280', // gray
    };
    return statusColors[status] || '#6B7280';
  }

  getStatusIcon(status: string): string {
    const statusIcons: { [key: string]: string } = {
      available: 'check-circle',
      borrowed: 'clock',
      reserved: 'bookmark',
      damaged: 'exclamation-triangle',
      lost: 'x-circle',
      maintenance: 'cog',
    };
    return statusIcons[status] || 'question-mark-circle';
  }

  generateReport(): void {
    const currentDate = new Date();
    const month = (currentDate.getMonth() + 1).toString();
    const year = currentDate.getFullYear();

    this.advancedFeaturesService.generateMonthlyReport(month, year).subscribe({
      next: (report) => {
        console.log('Generated report:', report);
        // In a real app, this would download or display the report
        alert('Báo cáo đã được tạo thành công!');
      },
    });
  }

  exportAnalytics(): void {
    if (!this.analytics) return;

    // Create CSV data
    const csvData = this.convertToCSV(this.analytics);
    const blob = new Blob([csvData], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);

    // Download file
    const link = document.createElement('a');
    link.href = url;
    link.download = `library-analytics-${new Date().toISOString().split('T')[0]}.csv`;
    link.click();

    window.URL.revokeObjectURL(url);
  }

  private convertToCSV(data: LibraryAnalytics): string {
    let csv = 'Thống kê thư viện\n\n';

    // Book status stats
    csv += 'Trạng thái sách,Số lượng\n';
    Object.entries(data.bookStatusStats).forEach(([status, count]) => {
      csv += `${this.getStatusLabel(status)},${count}\n`;
    });

    csv += '\nTop độc giả\n';
    csv += 'Tên,Số sách mượn,Hạng thành viên\n';
    data.topReaders.forEach((reader) => {
      csv += `${reader.userName},${reader.totalLoans},${reader.currentTier}\n`;
    });

    return csv;
  }
}
