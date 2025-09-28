import { Routes } from '@angular/router';
import { Login } from './components/auth/login/login';
import { Register } from './components/auth/register/register';
import { Home } from './components/library/home/home';
import { Profile } from './components/library/profile/profile';
import { MainLayout } from './components/layout/main-layout/main-layout';
import { AdminLayout } from './components/layout/admin-layout/admin-layout';
import { Dashboard } from './components/admin/dashboard/dashboard';
import { BookManagement } from './components/admin/book-management/book-management';
import { UserManagement } from './components/admin/user-management/user-management';
import { LoanManagement } from './components/admin/loan-management/loan-management';
import { CategoryManagement } from './components/admin/category-management/category-management';
import { AuthorManagement } from './components/admin/author-management/author-management';
import { PublisherManagementComponent } from './components/admin/publisher-management/publisher-management';
import { Reports } from './components/admin/reports/reports';
import { MembershipManagement } from './components/admin/membership-management/membership-management';
import { AnalyticsDashboard } from './components/admin/analytics-dashboard/analytics-dashboard';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { BooksComponent } from './components/library/books/books.component';

export const routes: Routes = [
  { path: '', redirectTo: '/library/home', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },

  // Public routes
  {
    path: 'library',
    component: MainLayout,
    children: [
      { path: 'home', component: Home },
      { path: 'books', component: BooksComponent },
      { path: 'profile', component: Profile, canActivate: [AuthGuard] },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
    ],
  },

  // Admin routes
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_ADMIN'] },
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'books', component: BookManagement },
      { path: 'users', component: UserManagement },
      { path: 'loans', component: LoanManagement },
      { path: 'categories', component: CategoryManagement },
      { path: 'authors', component: AuthorManagement },
      { path: 'publishers', component: PublisherManagementComponent },
      { path: 'memberships', component: MembershipManagement },
      { path: 'analytics', component: AnalyticsDashboard },
      { path: 'reports', component: Reports },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },

  // Librarian routes
  {
    path: 'librarian',
    component: AdminLayout,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_LIBRARIAN', 'ROLE_ADMIN'] },
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'books', component: BookManagement },
      { path: 'loans', component: LoanManagement },
      { path: 'categories', component: CategoryManagement },
      { path: 'authors', component: AuthorManagement },
      { path: 'publishers', component: PublisherManagementComponent },
      { path: 'memberships', component: MembershipManagement },
      { path: 'analytics', component: AnalyticsDashboard },
      { path: 'reports', component: Reports },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: '/library/home' },
];
