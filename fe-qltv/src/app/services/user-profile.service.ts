import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { MembershipTier, UserMembership, UserRating } from '../models/advanced-features.model';

@Injectable({ providedIn: 'root' })
export class UserProfileService {
  getMembership(userId: number): Observable<{ tier: MembershipTier; progress: UserMembership }> {
    // TODO: Replace with real API call
    return of({
      tier: {
        id: 1,
        name: 'Cơ bản',
        level: 'BASIC',
        benefits: [],
        requirements: { minLoans: 0 },
        color: 'bg-gray-100',
        icon: 'user',
      },
      progress: {
        userId,
        tierId: 1,
        currentPoints: 120,
        totalLoans: 10,
        violations: 0,
        joinDate: new Date(),
        nextTierProgress: 60,
      },
    });
  }

  getRating(userId: number): Observable<UserRating> {
    // TODO: Replace with real API call
    return of({
      userId,
      rating: 'GOOD',
      score: 85,
      violations: [],
      totalLoans: 10,
      onTimeReturns: 8,
      lateReturns: 2,
    });
  }

  updateNotification(userId: number, setting: string, value: boolean): Observable<any> {
    // TODO: Replace with real API call
    return of({ success: true });
  }

  updatePrivacy(userId: number, setting: string, value: boolean): Observable<any> {
    // TODO: Replace with real API call
    return of({ success: true });
  }
}
