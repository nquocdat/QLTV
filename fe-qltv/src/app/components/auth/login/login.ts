import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      rememberMe: [false],
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const { email, password, rememberMe } = this.loginForm.value;
      const loginRequest = { email, password };

      this.authService.login(loginRequest).subscribe({
        next: (response) => {
          this.isLoading = false;

          // Lưu token và thông tin user
          if (rememberMe) {
            localStorage.setItem('rememberMe', 'true');
          }

          // Điều hướng dựa trên role
          if (this.authService.isAdmin()) {
            this.router.navigate(['/admin/dashboard']);
          } else if (this.authService.isLibrarian()) {
            this.router.navigate(['/librarian/dashboard']);
          } else {
            this.router.navigate(['/library/home']);
          }
        },
        error: (error) => {
          this.isLoading = false;
          if (error.status === 401) {
            this.errorMessage = 'Email hoặc mật khẩu không chính xác';
          } else if (error.status === 0) {
            this.errorMessage = 'Không thể kết nối đến máy chủ';
          } else {
            this.errorMessage = 'Đã xảy ra lỗi. Vui lòng thử lại sau.';
          }
        },
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach((key) => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }
}
