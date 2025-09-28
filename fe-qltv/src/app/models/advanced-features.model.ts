// User membership models
export interface MembershipTier {
  id: number;
  name: string;
  level: 'BASIC' | 'VIP' | 'PREMIUM';
  benefits: MembershipBenefit[];
  requirements: MembershipRequirement;
  color: string;
  icon: string;
}

export interface MembershipBenefit {
  type:
    | 'MAX_BOOKS'
    | 'LOAN_DURATION'
    | 'LATE_FEE_DISCOUNT'
    | 'RESERVATION_PRIORITY'
    | 'EARLY_ACCESS';
  value: number | boolean;
  description: string;
}

export interface MembershipRequirement {
  minLoans?: number;
  minPoints?: number;
  maxViolations?: number;
}

export interface UserMembership {
  userId: number;
  tierId: number;
  currentPoints: number;
  totalLoans: number;
  violations: number;
  joinDate: Date;
  nextTierProgress: number;
}

// Book status tracking
export interface BookStatus {
  id: number;
  bookId: number;
  status: 'AVAILABLE' | 'BORROWED' | 'RESERVED' | 'DAMAGED' | 'LOST' | 'MAINTENANCE';
  updateDate: Date;
  notes?: string;
  userId?: number; // For borrowed/reserved books
}

export interface BookStatistics {
  bookId: number;
  totalLoans: number;
  currentStatus: string;
  averageRating: number;
  lastLoanDate?: Date;
  damageReports: number;
  popularityScore: number;
}

// User rating and violation tracking
export interface UserRating {
  userId: number;
  rating: 'EXCELLENT' | 'GOOD' | 'AVERAGE' | 'POOR';
  score: number;
  violations: UserViolation[];
  totalLoans: number;
  onTimeReturns: number;
  lateReturns: number;
}

export interface UserViolation {
  id: number;
  userId: number;
  type: 'LATE_RETURN' | 'DAMAGE' | 'LOST' | 'OVERDUE_FINE';
  description: string;
  date: Date;
  penalty: number;
  resolved: boolean;
}

// Notification system
export interface Notification {
  id: number;
  userId: number;
  type: 'REMINDER' | 'DUE_TODAY' | 'OVERDUE' | 'BOOK_AVAILABLE' | 'NEW_BOOK' | 'MEMBERSHIP_UPDATE';
  title: string;
  message: string;
  sentDate: Date;
  readDate?: Date;
  emailSent: boolean;
}

// Advanced analytics
export interface LibraryAnalytics {
  bookStatusStats: { [status: string]: number };
  topReaders: TopReader[];
  frequentLateReturners: LateReturner[];
  loanTrends: LoanTrend[];
  popularBooks: PopularBook[];
  membershipDistribution: MembershipStats[];
}

export interface TopReader {
  userId: number;
  userName: string;
  totalLoans: number;
  currentTier: string;
  avatar?: string;
}

export interface LateReturner {
  userId: number;
  userName: string;
  lateReturns: number;
  totalLoans: number;
  rating: string;
}

export interface LoanTrend {
  period: string;
  totalLoans: number;
  newMembers: number;
  returnRate: number;
}

export interface PopularBook {
  bookId: number;
  title: string;
  author: string;
  loanCount: number;
  rating: number;
  category: string;
}

export interface MembershipStats {
  tierName: string;
  memberCount: number;
  percentage: number;
  color: string;
}
