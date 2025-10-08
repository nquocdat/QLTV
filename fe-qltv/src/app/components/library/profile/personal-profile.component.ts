import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, ChangePasswordRequest } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-personal-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './personal-profile.component.html',
  styleUrls: ['./personal-profile.component.css'],
})
export class PersonalProfileComponent implements OnInit {
  currentUser: any = {};
  editMode = false;
  editedUser: any = {};

  // Change password fields
  showChangePasswordModal = false;
  passwordData: ChangePasswordRequest = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  };

  constructor(private userService: UserService, private authService: AuthService) {}

  ngOnInit() {
    // Lấy user từ AuthService trước
    this.authService.currentUser$.subscribe((user) => {
      if (user && user.id) {
        // Load full user data từ backend
        this.userService.getUserById(user.id).subscribe({
          next: (userData: any) => {
            this.currentUser = userData;
            this.editedUser = { ...userData };
          },
          error: (error) => {
            console.error('Error loading user data:', error);
            // Fallback: dùng data từ AuthService
            this.currentUser = {
              id: user.id,
              name: user.name,
              email: user.email,
              phoneNumber: '',
              address: '',
            };
            this.editedUser = { ...this.currentUser };
          },
        });
      }
    });
  }

  enableEdit() {
    this.editMode = true;
    this.editedUser = { ...this.currentUser };
  }

  cancelEdit() {
    this.editMode = false;
    this.editedUser = { ...this.currentUser };
  }

  saveEdit() {
    if (!this.editedUser.id) {
      alert('Không tìm thấy ID người dùng!');
      return;
    }
    this.userService.updateUser(this.editedUser.id, this.editedUser).subscribe({
      next: (res) => {
        this.currentUser = { ...res };
        this.editedUser = { ...res };
        this.editMode = false;
        alert('Cập nhật thành công!');
      },
      error: (error) => {
        console.error('Error updating user:', error);
        alert('Có lỗi khi cập nhật thông tin!');
      },
    });
  }

  // Change Password Methods
  openChangePasswordModal() {
    this.showChangePasswordModal = true;
    this.resetPasswordForm();
  }

  closeChangePasswordModal() {
    this.showChangePasswordModal = false;
    this.resetPasswordForm();
  }

  resetPasswordForm() {
    this.passwordData = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    };
  }

  submitChangePassword() {
    // Validation
    if (
      !this.passwordData.currentPassword ||
      !this.passwordData.newPassword ||
      !this.passwordData.confirmPassword
    ) {
      alert('Vui lòng điền đầy đủ thông tin!');
      return;
    }

    if (this.passwordData.newPassword !== this.passwordData.confirmPassword) {
      alert('Mật khẩu mới và xác nhận mật khẩu không khớp!');
      return;
    }

    if (this.passwordData.newPassword.length < 6) {
      alert('Mật khẩu mới phải có ít nhất 6 ký tự!');
      return;
    }

    if (!this.currentUser.id) {
      alert('Không tìm thấy thông tin người dùng!');
      return;
    }

    // Call API
    this.userService.changePassword(this.currentUser.id, this.passwordData).subscribe({
      next: (response) => {
        if (response.message) {
          alert('✅ ' + response.message);
          this.closeChangePasswordModal();
        } else if (response.error) {
          alert('❌ ' + response.error);
        }
      },
      error: (error) => {
        console.error('Error changing password:', error);
        if (error.error && error.error.error) {
          alert('❌ ' + error.error.error);
        } else {
          alert('❌ Có lỗi xảy ra khi đổi mật khẩu!');
        }
      },
    });
  }
}
