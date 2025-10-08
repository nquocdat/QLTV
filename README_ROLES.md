# 📚 QLTV - Hệ thống Quản lý Thư viện

## 🔐 Hệ thống Phân quyền 3 cấp

### Cập nhật mới nhất: 07/10/2025

Hệ thống đã được cập nhật để hỗ trợ **3 vai trò** với quyền hạn phân cấp rõ ràng:

---

## 👥 Vai trò và Quyền hạn

### 🔴 ADMIN - Quản trị viên (Toàn quyền)

**Đăng nhập mặc định:**

- Email: `admin@qltv.com`
- Password: `admin123`

**Quyền hạn:** Toàn quyền truy cập (14 modules)

- ✅ Quản lý người dùng (tạo, sửa, xóa, nâng cấp role)
- ✅ Quản lý sách & bản sao
- ✅ Quản lý mượn trả
- ✅ Quản lý thể loại, tác giả, nhà xuất bản
- ✅ Quản lý thành viên
- ✅ Quản lý đánh giá (duyệt, xóa reviews)
- ✅ Quản lý thanh toán VNPay (lịch sử, thống kê)
- ✅ Xác nhận thanh toán tiền mặt
- ✅ Phân tích dữ liệu & Báo cáo thống kê

**URL:** `/admin/*`

---

### 🟢 LIBRARIAN - Thủ thư (Quản lý nghiệp vụ)

**Đăng nhập demo:**

- Email: `librarian1@qltv.com` hoặc `librarian2@qltv.com`
- Password: `password`

**Quyền hạn:** Quản lý nghiệp vụ thư viện (11 modules)

- ✅ Quản lý sách & bản sao
- ✅ Quản lý mượn trả
- ✅ Quản lý thể loại, tác giả, nhà xuất bản
- ✅ Quản lý thành viên
- ✅ Xác nhận thanh toán tiền mặt
- ✅ Phân tích dữ liệu & Báo cáo thống kê
- ❌ **KHÔNG có quyền** quản lý người dùng
- ❌ **KHÔNG có quyền** quản lý đánh giá
- ❌ **KHÔNG có quyền** quản lý thanh toán VNPay

**URL:** `/librarian/*`

**Khác biệt với Admin:**

- Sidebar hiển thị: "QLTV Thủ thư" (thay vì "QLTV Admin")
- Role badge: "Thủ thư" (màu xanh)
- 3 menu bị ẩn: Users, Reviews, Payments

---

### 🔵 USER - Người dùng/Độc giả

**Đăng ký tự do tại:** `/register`

**Quyền hạn:** Sử dụng dịch vụ thư viện

- ✅ Xem danh sách sách
- ✅ Mượn sách (tối đa theo gói membership)
- ✅ Xem lịch sử mượn trả
- ✅ Thanh toán đặt cọc/phạt (VNPay hoặc Tiền mặt)
- ✅ Đánh giá sách (sau khi trả sách)
- ✅ Quản lý thông tin cá nhân
- ❌ Không truy cập được `/admin/*` và `/librarian/*`

**URL:** `/library/*`

---

## 🚀 Hướng dẫn cài đặt

### 1. Tạo Database

```bash
mysql -u root -p
CREATE DATABASE qltv_db;
USE qltv_db;
SOURCE qltv_db.sql;
```

### 2. Tạo tài khoản Thủ thư (Optional)

```bash
mysql -u root -p qltv_db < create-librarian-accounts.sql
```

### 3. Chạy Backend

```bash
cd be-qltv
mvn clean install
mvn spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8080`

### 4. Chạy Frontend

```bash
cd fe-qltv
npm install
ng serve
```

Frontend sẽ chạy tại: `http://localhost:4200`

---

## 🧪 Test Phân quyền

### Test 1: Đăng nhập Admin

1. Vào `http://localhost:4200/login`
2. Nhập: `admin@qltv.com` / `admin123`
3. Kiểm tra: Redirect về `/admin/dashboard`
4. Sidebar: Logo "QLTV Admin", 14 menu items

### Test 2: Đăng nhập Librarian

1. Vào `http://localhost:4200/login`
2. Nhập: `librarian1@qltv.com` / `password`
3. Kiểm tra: Redirect về `/librarian/dashboard`
4. Sidebar: Logo "QLTV Thủ thư", 11 menu items (không có Users/Reviews/Payments)

### Test 3: Truy cập trực tiếp

```
Admin:
✅ /admin/users → OK
✅ /admin/reviews → OK
✅ /admin/payments → OK

Librarian:
❌ /admin/users → 403 Forbidden
❌ /admin/reviews → 403 Forbidden
✅ /librarian/books → OK
✅ /librarian/loans → OK

User:
❌ /admin/dashboard → Redirect to login
❌ /librarian/dashboard → Redirect to login
✅ /library/books → OK
```

---

## 📂 Tài liệu kỹ thuật

### File quan trọng

**Backend:**

- `Patron.java` - Entity chứa enum Role (ADMIN, LIBRARIAN, USER)
- `SecurityConfig.java` - Cấu hình Spring Security
- Controllers - Sử dụng `@PreAuthorize` để phân quyền

**Frontend:**

- `app.routes.ts` - Config routes cho cả 3 roles
- `auth.service.ts` - Service kiểm tra role
- `role.guard.ts` - Guard kiểm tra quyền truy cập
- `admin-layout.html/ts` - Sidebar động theo role

### Tài liệu chi tiết

1. **LIBRARIAN_ROLE_GUIDE.md** - Hướng dẫn đầy đủ về vai trò Thủ thư
2. **LIBRARIAN_UPDATE_SUMMARY.md** - Tóm tắt các thay đổi mới nhất
3. **create-librarian-accounts.sql** - Script tạo tài khoản thủ thư

---

## 🔧 Cấu trúc Phân quyền

### Backend (Spring Security)

```java
// Admin only
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public ResponseEntity<?> getUsers() { ... }

// Admin hoặc Librarian
@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
@GetMapping("/books")
public ResponseEntity<?> getBooks() { ... }

// Tất cả người dùng đã đăng nhập
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'LIBRARIAN')")
@GetMapping("/profile")
public ResponseEntity<?> getProfile() { ... }
```

### Frontend (Angular)

```typescript
// Route guard
{
  path: 'admin',
  canActivate: [AuthGuard, RoleGuard],
  data: { roles: ['ROLE_ADMIN'] },
  ...
}

// Template conditional
<a *ngIf="isAdmin()" routerLink="/admin/users">
  Quản lý người dùng
</a>

// Dynamic routing
<a [routerLink]="getBasePath() + '/books'">
  Quản lý sách
</a>
```

---

## 📊 Thống kê Hệ thống

| Module       | Admin | Librarian | User        |
| ------------ | ----- | --------- | ----------- |
| Dashboard    | ✅    | ✅        | ❌          |
| Users        | ✅    | ❌        | ❌          |
| Books        | ✅    | ✅        | View only   |
| Copies       | ✅    | ✅        | ❌          |
| Loans        | ✅    | ✅        | Own only    |
| Categories   | ✅    | ✅        | View only   |
| Authors      | ✅    | ✅        | View only   |
| Publishers   | ✅    | ✅        | View only   |
| Memberships  | ✅    | ✅        | Own only    |
| Analytics    | ✅    | ✅        | ❌          |
| Reports      | ✅    | ✅        | ❌          |
| Reviews      | ✅    | ❌        | Create only |
| Payments     | ✅    | ❌        | Own only    |
| Pending Cash | ✅    | ✅        | ❌          |

---

## 🎯 Cập nhật gần đây

### 07/10/2025

- ✅ Xóa section "Khám phá theo thể loại" khỏi trang chủ
- ✅ Cập nhật sidebar động hỗ trợ ADMIN & LIBRARIAN
- ✅ Ẩn menu không phù hợp cho Librarian
- ✅ Thêm menu "Xác nhận thanh toán" cho Librarian
- ✅ Cập nhật hiển thị role bằng tiếng Việt
- ✅ Tạo tài liệu hướng dẫn đầy đủ

---

## 🆘 Troubleshooting

### 1. Không đăng nhập được

- Kiểm tra database có tài khoản chưa
- Kiểm tra password đã được hash đúng chưa
- Xem console log backend để debug

### 2. Truy cập route bị 403

- Kiểm tra role của user trong token
- Kiểm tra `@PreAuthorize` trong controller
- Kiểm tra route config trong `app.routes.ts`

### 3. Sidebar không hiển thị đúng

- Kiểm tra `currentUser.role` trong localStorage
- Clear cache và reload page
- Kiểm tra `isAdmin()` function trong component

---

## 📞 Liên hệ & Hỗ trợ

**Developer:** GitHub Copilot

**Email:** support@qltv.com

**Documentation:** `/docs` folder

**Version:** 2.0 (Phân quyền 3 cấp)

---

© 2025 QLTV - Hệ thống Quản lý Thư viện
