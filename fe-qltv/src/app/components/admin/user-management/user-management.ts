import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../services/user.service';
import { Patron } from '../../../models/patron.model';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './user-management.html',
  styleUrl: './user-management.css',
})
export class UserManagement implements OnInit {
  users: Patron[] = [];
  filteredUsers: Patron[] = [];
  userForm!: FormGroup;

  // Modal state
  showUserModal = false;
  modalMode: 'add' | 'edit' | 'view' = 'add';
  // Selected user
  selectedUser: Patron | null = null;

  // Loading state
  isLoading = false;

  // Filters
  searchQuery = '';
  selectedRole = '';
  selectedStatus = '';

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;

  // Math for template
  Math = Math;

  constructor(private fb: FormBuilder, private userService: UserService) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  initializeForm(): void {
    this.userForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: [''],
      phoneNumber: [''],
      address: [''],
      role: ['USER', Validators.required],
      isActive: [true],
    });
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (users: Patron[]) => {
        this.users = users;
        this.totalItems = this.users.length;
        this.calculateTotalPages();
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.users = [];
        this.totalItems = 0;
        this.calculateTotalPages();
        this.applyFilters();
      },
    });
  }

  onSearch(): void {
    this.currentPage = 1;
    this.applyFilters();
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }

  applyFilters(): void {
    let filtered = [...this.users];

    // Search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (user) =>
          user.name.toLowerCase().includes(query) ||
          user.email.toLowerCase().includes(query) ||
          (user.phoneNumber && user.phoneNumber.includes(query))
      );
    }

    // Role filter
    if (this.selectedRole) {
      filtered = filtered.filter((user) => user.role === this.selectedRole);
    }

    // Status filter
    if (this.selectedStatus !== '') {
      const isActive = this.selectedStatus === 'true';
      filtered = filtered.filter((user) => user.isActive === isActive);
    }

    // Update total count and pages
    this.totalItems = filtered.length;
    this.calculateTotalPages();

    // Apply pagination
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.filteredUsers = filtered.slice(startIndex, endIndex);
  }

  calculateTotalPages(): void {
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.applyFilters();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.applyFilters();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.applyFilters();
  }

  getVisiblePages(): number[] {
    const pages: number[] = [];
    const start = Math.max(1, this.currentPage - 2);
    const end = Math.min(this.totalPages, this.currentPage + 2);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }

  openAddUserModal(): void {
    this.modalMode = 'add';
    this.selectedUser = null;
    this.initializeForm();
    // Password is optional - backend will use default "password" if not provided
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
    this.showUserModal = true;
  }

  editUser(user: Patron): void {
    this.modalMode = 'edit';
    this.selectedUser = user;
    this.userForm.patchValue({
      name: user.name,
      email: user.email,
      phoneNumber: user.phoneNumber,
      address: user.address,
      role: user.role,
      isActive: user.isActive,
    });
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
    this.showUserModal = true;
  }

  viewUser(user: Patron): void {
    this.modalMode = 'view';
    this.selectedUser = user;
    this.showUserModal = true;
  }

  closeModal(): void {
    this.showUserModal = false;
    this.selectedUser = null;
    this.initializeForm();
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.isLoading = true;
      const formData = this.userForm.value;

      if (this.modalMode === 'add') {
        this.userService.createUser(formData).subscribe({
          next: (newUser: Patron) => {
            console.log('Thêm người dùng mới thành công:', newUser);
            this.loadUsers(); // Reload the user list
            this.isLoading = false;
            this.closeModal();

            // Show success message with default password info
            const password = formData.password || 'password';
            const message = formData.password
              ? `✅ Tạo người dùng thành công!\n\nEmail: ${formData.email}\nMật khẩu: ${password}\n\nVui lòng thông báo cho người dùng.`
              : `✅ Tạo người dùng thành công!\n\nEmail: ${formData.email}\nMật khẩu mặc định: password\n\nVui lòng thông báo cho người dùng đăng nhập và đổi mật khẩu.`;
            alert(message);
          },
          error: (error) => {
            console.error('Lỗi khi thêm người dùng:', error);
            alert('❌ Lỗi khi tạo người dùng! Vui lòng thử lại.');
            this.isLoading = false;
          },
        });
      } else if (this.modalMode === 'edit' && this.selectedUser?.id) {
        this.userService.updateUser(this.selectedUser.id, formData).subscribe({
          next: (updatedUser: Patron) => {
            console.log('Cập nhật người dùng thành công:', updatedUser);
            this.loadUsers(); // Reload the user list
            this.isLoading = false;
            this.closeModal();
          },
          error: (error) => {
            console.error('Lỗi khi cập nhật người dùng:', error);
            this.isLoading = false;
          },
        });
      }
    }
  }

  toggleUserStatus(user: Patron): void {
    if (
      confirm(`Bạn có chắc muốn ${user.isActive ? 'khóa' : 'kích hoạt'} tài khoản ${user.name}?`)
    ) {
      if (user.id) {
        this.userService.toggleUserStatus(user.id).subscribe({
          next: (updatedUser: Patron) => {
            console.log(
              `${user.isActive ? 'Đã khóa' : 'Đã kích hoạt'} tài khoản:`,
              updatedUser.name
            );
            this.loadUsers(); // Reload the user list
          },
          error: (error: any) => {
            console.error('Lỗi khi thay đổi trạng thái người dùng:', error);
          },
        });
      }
    }
  }

  getUserInitials(name: string): string {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'ADMIN':
      case 'ROLE_ADMIN':
        return 'bg-purple-100 text-purple-800';
      case 'LIBRARIAN':
      case 'ROLE_LIBRARIAN':
        return 'bg-blue-100 text-blue-800';
      case 'USER':
      case 'ROLE_USER':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getRoleDisplayName(role: string): string {
    switch (role) {
      case 'ADMIN':
        return 'Quản trị viên';
      case 'LIBRARIAN':
        return 'Thủ thư';
      case 'USER':
        return 'Người dùng';
      // Backward compatibility
      case 'ROLE_ADMIN':
        return 'Quản trị viên';
      case 'ROLE_LIBRARIAN':
        return 'Thủ thư';
      case 'ROLE_USER':
        return 'Người dùng';
      default:
        return role;
    }
  }

  getCurrentDateString(): string {
    return new Date().toLocaleDateString('vi-VN');
  }
}
