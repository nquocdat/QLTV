import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class RoleGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return false;
    }

    const requiredRoles = route.data['roles'] as Array<string>;

    if (requiredRoles && requiredRoles.length > 0) {
      const hasRequiredRole = requiredRoles.some((role) => {
        switch (role) {
          case 'ROLE_ADMIN':
            return this.authService.isAdmin();
          case 'ROLE_LIBRARIAN':
            return this.authService.isLibrarian();
          case 'ROLE_USER':
            return this.authService.isUser();
          default:
            return false;
        }
      });

      if (!hasRequiredRole) {
        console.log(
          'Access denied - User role:',
          this.authService.getUser()?.role,
          'Required roles:',
          requiredRoles
        );
        this.router.navigate(['/library/home']);
        return false;
      }
    }

    return true;
  }
}
