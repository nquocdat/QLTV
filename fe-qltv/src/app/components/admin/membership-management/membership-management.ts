import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdvancedFeaturesService } from '../../../services/advanced-features.service';
import {
  MembershipTier,
  UserMembership,
  UserRating,
} from '../../../models/advanced-features.model';

@Component({
  selector: 'app-membership-management',
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './membership-management.html',
  styleUrl: './membership-management.css',
})
export class MembershipManagement implements OnInit {
  membershipTiers: MembershipTier[] = [];
  userMemberships: (UserMembership & { user: any; rating: UserRating })[] = [];
  notifications: any[] = [];

  loading = true;
  showNotificationModal = false;

  // Modal states
  selectedUser: any = null;
  showUserDetailModal = false;

  // Notification form
  notificationForm = {
    type: 'REMINDER',
    title: '',
    message: '',
    userIds: [] as number[],
  };

  constructor(private advancedFeaturesService: AdvancedFeaturesService) {}

  ngOnInit(): void {
    this.loadMembershipTiers();
    this.loadUserMemberships();
    this.loadNotifications();
  }

  private loadMembershipTiers(): void {
    this.advancedFeaturesService.getMembershipTiers().subscribe({
      next: (tiers) => {
        this.membershipTiers = tiers;
      },
      error: (error) => {
        console.error('Error loading membership tiers:', error);
        this.loading = false;
      },
    });
  }

  private loadUserMemberships(): void {
    // Load all user memberships directly from backend API
    this.advancedFeaturesService.getAllUserMemberships().subscribe({
      next: (memberships) => {
        // Map memberships with user rating
        memberships.forEach((membership) => {
          this.advancedFeaturesService.getUserRating(membership.userId).subscribe({
            next: (rating) => {
              this.userMemberships.push({
                ...membership,
                user: {
                  id: membership.userId,
                  name: membership.userName || 'Unknown',
                  email: membership.userEmail || 'No email',
                  joinDate: membership.joinDate,
                  totalLoans: membership.totalLoans,
                  currentLoans: 0,
                  violations: membership.violationCount || 0,
                  avatar: null,
                },
                rating,
              });

              if (this.userMemberships.length === memberships.length) {
                this.loading = false;
              }
            },
            error: () => {
              // If rating fails, still add membership without rating
              this.userMemberships.push({
                ...membership,
                user: {
                  id: membership.userId,
                  name: membership.userName || 'Unknown',
                  email: membership.userEmail || 'No email',
                  joinDate: membership.joinDate,
                  totalLoans: membership.totalLoans,
                  currentLoans: 0,
                  violations: membership.violationCount || 0,
                  avatar: null,
                },
                rating: {
                  userId: membership.userId,
                  rating: 'AVERAGE',
                  score: 70,
                  violations: [],
                  totalLoans: membership.totalLoans,
                  onTimeReturns: membership.totalLoans,
                  lateReturns: 0,
                },
              });

              if (this.userMemberships.length === memberships.length) {
                this.loading = false;
              }
            },
          });
        });
      },
      error: (error) => {
        console.error('Error loading user memberships:', error);
        this.loading = false;
      },
    });
  }

  private loadNotifications(): void {
    // Load recent notifications for all users
    this.advancedFeaturesService.getNotifications(1).subscribe({
      next: (notifications) => {
        this.notifications = notifications;
      },
    });
  }

  getMembershipTier(tierId: number): MembershipTier | undefined {
    return this.membershipTiers.find((tier) => tier.id === tierId);
  }

  upgradeMembership(userId: number, newTierId: number): void {
    if (confirm('Bạn có chắc muốn nâng hạng thành viên cho người dùng này?')) {
      // Find user membership
      const userMembership = this.userMemberships.find((m) => m.userId === userId);
      if (userMembership) {
        const oldTier = this.getMembershipTier(userMembership.tierId);
        const newTier = this.getMembershipTier(newTierId);

        // Update membership
        userMembership.tierId = newTierId;

        // Send notification
        const notification = {
          userId: userId,
          type: 'MEMBERSHIP_UPDATE' as const,
          title: 'Nâng hạng thành viên',
          message: `Chúc mừng! Bạn đã được nâng lên hạng ${newTier?.name}`,
          emailSent: false,
        };

        this.advancedFeaturesService.sendNotification(notification).subscribe({
          next: () => {
            alert(`Đã nâng hạng thành viên từ ${oldTier?.name} lên ${newTier?.name}`);
          },
        });
      }
    }
  }

  viewUserDetails(userId: number): void {
    const userMembership = this.userMemberships.find((m) => m.userId === userId);
    if (userMembership) {
      this.selectedUser = userMembership;
      this.showUserDetailModal = true;
    }
  }

  closeUserDetailModal(): void {
    this.showUserDetailModal = false;
    this.selectedUser = null;
  }

  openNotificationModal(): void {
    this.showNotificationModal = true;
  }

  closeNotificationModal(): void {
    this.showNotificationModal = false;
    this.resetNotificationForm();
  }

  sendBulkNotification(): void {
    if (!this.notificationForm.title || !this.notificationForm.message) {
      alert('Vui lòng nhập đầy đủ tiêu đề và nội dung');
      return;
    }

    const targetUsers =
      this.notificationForm.userIds.length > 0
        ? this.notificationForm.userIds
        : this.userMemberships.map((m) => m.userId);

    targetUsers.forEach((userId) => {
      const notification = {
        userId: userId,
        type: this.notificationForm.type as any,
        title: this.notificationForm.title,
        message: this.notificationForm.message,
        emailSent: false,
      };

      this.advancedFeaturesService.sendNotification(notification).subscribe();
    });

    alert(`Đã gửi thông báo đến ${targetUsers.length} người dùng`);
    this.closeNotificationModal();
  }

  private resetNotificationForm(): void {
    this.notificationForm = {
      type: 'REMINDER',
      title: '',
      message: '',
      userIds: [],
    };
  }

  scheduleReminders(): void {
    this.advancedFeaturesService.scheduleReminderNotifications().subscribe({
      next: () => {
        alert('Đã lên lịch gửi thông báo nhắc nhở tự động');
      },
    });
  }

  getRatingClass(rating: string): string {
    switch (rating) {
      case 'EXCELLENT':
        return 'bg-green-100 text-green-800';
      case 'GOOD':
        return 'bg-blue-100 text-blue-800';
      case 'AVERAGE':
        return 'bg-yellow-100 text-yellow-800';
      case 'POOR':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getRatingText(rating: string): string {
    switch (rating) {
      case 'EXCELLENT':
        return 'Xuất sắc';
      case 'GOOD':
        return 'Tốt';
      case 'AVERAGE':
        return 'Trung bình';
      case 'POOR':
        return 'Kém';
      default:
        return 'Chưa xác định';
    }
  }

  calculateNextTierProgress(membership: UserMembership): number {
    const currentTier = this.getMembershipTier(membership.tierId);
    const nextTierIndex = this.membershipTiers.findIndex((t) => t.id === membership.tierId) + 1;

    if (nextTierIndex >= this.membershipTiers.length) return 100;

    const nextTier = this.membershipTiers[nextTierIndex];
    const requiredLoans = nextTier.requirements.minLoans || 0;
    const requiredPoints = nextTier.requirements.minPoints || 0;

    const loanProgress = Math.min(100, (membership.totalLoans / requiredLoans) * 100);
    const pointProgress = Math.min(100, (membership.currentPoints / requiredPoints) * 100);

    return Math.min(loanProgress, pointProgress);
  }

  canUpgradeMembership(membership: UserMembership): boolean {
    // Check if already at max tier
    const nextTierIndex = this.membershipTiers.findIndex((t) => t.id === membership.tierId) + 1;
    if (nextTierIndex >= this.membershipTiers.length) return false;

    const nextTier = this.membershipTiers[nextTierIndex];
    if (!nextTier) return false;

    // Check all requirements
    const meetsLoanRequirement = membership.totalLoans >= (nextTier.requirements.minLoans || 0);
    const meetsPointRequirement =
      membership.currentPoints >= (nextTier.requirements.minPoints || 0);
    const meetsViolationRequirement =
      (membership.violationCount || 0) <= (nextTier.requirements.maxViolations || 999);

    return meetsLoanRequirement && meetsPointRequirement && meetsViolationRequirement;
  }
}
