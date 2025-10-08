# 📚 Hướng dẫn Vai trò THỦ THƯ (LIBRARIAN)

## 🎯 Tổng quan

Hệ thống QLTV đã được cập nhật để hỗ trợ **3 vai trò chính**:

- **ADMIN** - Quản trị viên (toàn quyền)
- **LIBRARIAN** - Thủ thư (quản lý nghiệp vụ thư viện)
- **USER** - Người dùng (độc giả)

## ✅ Những gì đã hoàn thành

### 1. **Backend - Phân quyền đã có sẵn**

✅ Enum Role trong Patron.java:

```java
public enum Role {
    ADMIN, LIBRARIAN, USER
}
```

✅ Controllers đã được phân quyền đúng:

- `DashboardController`: `@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")`
- `AnalyticsController`: `@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")`
- `PatronController`: `@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")`

### 2. **Frontend - Cập nhật mới**

✅ **Routes** (`app.routes.ts`):

```typescript
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
    { path: 'pending-payments', component: PendingPaymentsComponent },
    { path: 'copies', component: AdminCopiesComponent },
  ]
}
```

✅ **AuthService** có sẵn:

```typescript
public isAdmin(): boolean {
  return this.hasRole('ROLE_ADMIN');
}

public isLibrarian(): boolean {
  return this.hasRole('ROLE_LIBRARIAN');
}
```

✅ **Login** tự động điều hướng:

```typescript
if (this.authService.isAdmin()) {
  this.router.navigate(['/admin/dashboard']);
} else if (this.authService.isLibrarian()) {
  this.router.navigate(['/librarian/dashboard']);
} else {
  this.router.navigate(['/library/home']);
}
```

✅ **Sidebar động** (`admin-layout.html`):

- Tiêu đề thay đổi: "QLTV Admin" hoặc "QLTV Thủ thư"
- Menu sử dụng `getBasePath()` để tự động chọn `/admin` hoặc `/librarian`
- Hiển thị role: "Quản trị viên" hoặc "Thủ thư"

### 3. **Xóa phần "Khám phá theo thể loại"**

✅ Đã xóa toàn bộ section Categories ở `home.html` (70+ dòng code)

## 🔐 Phân quyền chi tiết

### ADMIN (Quản trị viên)

Có **TOÀN QUYỀN** truy cập:

- ✅ Quản lý người dùng
- ✅ Quản lý sách
- ✅ Quản lý bản sao
- ✅ Quản lý mượn trả
- ✅ Quản lý thể loại, tác giả, nhà xuất bản
- ✅ Quản lý thành viên
- ✅ Phân tích dữ liệu & Báo cáo thống kê
- ✅ Quản lý đánh giá (**Admin only**)
- ✅ Quản lý thanh toán (**Admin only**)
- ✅ Xác nhận thanh toán tiền mặt

### LIBRARIAN (Thủ thư)

Có quyền **quản lý nghiệp vụ** (trừ User Management & Reviews):

- ✅ Quản lý sách
- ✅ Quản lý bản sao
- ✅ Quản lý mượn trả
- ✅ Quản lý thể loại, tác giả, nhà xuất bản
- ✅ Quản lý thành viên
- ✅ Phân tích dữ liệu & Báo cáo thống kê
- ✅ Xác nhận thanh toán tiền mặt
- ❌ **KHÔNG** quản lý người dùng (Admin only)
- ❌ **KHÔNG** quản lý đánh giá (Admin only)
- ❌ **KHÔNG** quản lý thanh toán VNPay (Admin only)

### USER (Người dùng/Độc giả)

Chỉ có quyền **sử dụng dịch vụ**:

- ✅ Xem danh sách sách
- ✅ Mượn sách
- ✅ Xem lịch sử mượn trả
- ✅ Thanh toán tiền đặt cọc/phạt
- ✅ Đánh giá sách (sau khi trả)
- ✅ Quản lý thông tin cá nhân

## 🧪 Cách kiểm tra

### 1. Tạo tài khoản Thủ thư trong Database

```sql
-- Thêm một tài khoản thủ thư mẫu
INSERT INTO patron (name, email, password, phone, address, role, created_date, updated_date)
VALUES (
  'Nguyễn Văn Thủ',
  'librarian@qltv.com',
  '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', -- password: password
  '0987654321',
  '123 Đường ABC',
  'LIBRARIAN',
  CURDATE(),
  CURDATE()
);
```

### 2. Đăng nhập với tài khoản Thủ thư

- Email: `librarian@qltv.com`
- Password: `password`
- Hệ thống sẽ tự động chuyển đến `/librarian/dashboard`

### 3. Kiểm tra Sidebar

- Logo: "QLTV Thủ thư"
- Menu **KHÔNG hiển thị**: "Quản lý người dùng", "Quản lý đánh giá", "Quản lý thanh toán"
- Menu **HIỂN THỊ**: Tất cả menu khác (10 mục)
- User info: Role hiển thị "Thủ thư"

### 4. Kiểm tra Navigation

- Click vào menu bất kỳ → URL sẽ là `/librarian/*` thay vì `/admin/*`
- Sidebar active state vẫn hoạt động bình thường

### 5. Kiểm tra quyền truy cập

- Thử truy cập trực tiếp `/admin/users` → Sẽ bị **chặn** bởi RoleGuard
- Thử truy cập `/librarian/books` → **Thành công**
- Thử truy cập `/admin/reviews` → **Chặn** (404 hoặc redirect)

## 📝 Cấu trúc Code

### Backend Controllers với PreAuthorize

```java
// Chỉ Admin
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> adminOnlyEndpoint() { ... }

// Admin hoặc Librarian
@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
public ResponseEntity<?> staffEndpoint() { ... }

// Tất cả đã đăng nhập
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'LIBRARIAN')")
public ResponseEntity<?> authenticatedEndpoint() { ... }
```

### Frontend Sidebar Conditional Rendering

```html
<!-- Admin Only -->
<a *ngIf="isAdmin()" routerLink="/admin/users"> Quản lý người dùng </a>

<!-- Admin & Librarian -->
<a [routerLink]="getBasePath() + '/books'"> Quản lý sách </a>
```

### TypeScript Role Checking

```typescript
isAdmin(): boolean {
  return this.currentUser?.role === 'ROLE_ADMIN' || false;
}

getBasePath(): string {
  return this.currentRoute.startsWith('/admin') ? '/admin' : '/librarian';
}
```

## 🚀 Cải tiến đề xuất (Optional)

### 1. Tạo trang Đăng ký với lựa chọn Role (cho Admin)

```typescript
registerForm = this.fb.group({
  name: ['', Validators.required],
  email: ['', [Validators.required, Validators.email]],
  password: ['', [Validators.required, Validators.minLength(6)]],
  role: ['USER', Validators.required], // Dropdown: USER, LIBRARIAN (admin tạo từ backend)
});
```

### 2. Admin có thể nâng cấp USER → LIBRARIAN

```java
@PutMapping("/patrons/{id}/role")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> updateUserRole(
    @PathVariable Long id,
    @RequestBody RoleUpdateRequest request
) {
    patronService.updateRole(id, request.getRole());
    return ResponseEntity.ok("Role updated successfully");
}
```

### 3. Thêm badge "Thủ thư" trong User Management

```html
<span
  *ngIf="user.role === 'ROLE_LIBRARIAN'"
  class="px-2 py-1 bg-blue-100 text-blue-700 rounded-full text-xs"
>
  Thủ thư
</span>
```

## 📊 Tóm tắt thay đổi

| File                | Thay đổi                                                     |
| ------------------- | ------------------------------------------------------------ |
| `home.html`         | ❌ Xóa section "Khám phá theo thể loại"                      |
| `admin-layout.html` | ✅ Cập nhật sidebar với `getBasePath()`, `*ngIf="isAdmin()"` |
| `admin-layout.ts`   | ✅ Thêm pageTitles cho `/librarian/*`, thêm `isAdmin()`      |
| `login.ts`          | ✅ Đã có sẵn logic redirect theo role                        |
| `app.routes.ts`     | ✅ Đã có sẵn routes `/librarian`                             |
| `auth.service.ts`   | ✅ Đã có sẵn `isLibrarian()`, `isAdmin()`                    |
| Backend Controllers | ✅ Đã có sẵn `@PreAuthorize` cho LIBRARIAN                   |

## ✅ Checklist hoàn thành

- [x] Xóa phần "Khám phá theo thể loại" ở trang chủ
- [x] Cập nhật sidebar hỗ trợ cả Admin và Librarian
- [x] Ẩn menu "Quản lý người dùng" cho Librarian
- [x] Ẩn menu "Quản lý đánh giá" cho Librarian
- [x] Ẩn menu "Quản lý thanh toán" cho Librarian
- [x] Hiển thị menu "Xác nhận thanh toán" cho cả Admin và Librarian
- [x] Thay đổi tiêu đề logo theo role
- [x] Hiển thị role đúng (Quản trị viên/Thủ thư)
- [x] Backend đã có phân quyền đầy đủ
- [x] Frontend đã có AuthService với isLibrarian()
- [x] Login tự động điều hướng theo role
- [x] Routes đã được cấu hình cho /librarian/\*

---

**Lưu ý**: Hệ thống đã sẵn sàng cho việc tạo tài khoản Thủ thư. Admin có thể tạo tài khoản Librarian trực tiếp trong database hoặc thông qua User Management nếu thêm chức năng chọn Role.
