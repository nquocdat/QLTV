# ✅ CHECKLIST HOÀN CHỈNH - ADMIN CREATE USER FIX

## 📋 TỔNG QUAN

**Vấn đề**: Admin tạo user trong User Management → User không login được

**Nguyên nhân**: Backend hard-code password = "defaultPassword123"

**Giải pháp**:

- Backend: Thêm password field vào PatronDTO, dùng password từ request hoặc default "password"
- Frontend: Thêm password input (optional), hiển thị thông báo password

**Status**: 🟢 Code hoàn tất, chờ test

---

## 🔧 BACKEND CHANGES

### File 1: `PatronDTO.java`

**Location**: `be-qltv/src/main/java/com/example/be_qltv/dto/PatronDTO.java`

**Changes**:

- [x] Added field: `private String password;`
- [x] Added getter: `public String getPassword()`
- [x] Added setter: `public void setPassword(String password)`
- [x] No compilation errors

**Code**:

```java
// Line 23
private String password;

// Line 77
public String getPassword() {
    return password;
}

// Line 81
public void setPassword(String password) {
    this.password = password;
}
```

---

### File 2: `PatronService.java`

**Location**: `be-qltv/src/main/java/com/example/be_qltv/service/PatronService.java`

**Changes**:

- [x] Removed hard-coded `"defaultPassword123"`
- [x] Added logic to use `patronDTO.getPassword()` if provided
- [x] Default to `"password"` if not provided
- [x] Added console logging
- [x] No compilation errors

**Code**:

```java
// Lines 70-78
public PatronDTO createPatron(PatronDTO patronDTO) {
    // ...

    // For admin-created users, use provided password or default
    String password = (patronDTO.getPassword() != null && !patronDTO.getPassword().isEmpty())
        ? patronDTO.getPassword()
        : "password"; // Default password: "password"
    patron.setPassword(passwordEncoder.encode(password));

    System.out.println("Creating patron: " + patronDTO.getEmail() + " with password: " + password);

    // ...
}
```

---

## 🎨 FRONTEND CHANGES

### File 3: `user-management.html`

**Location**: `fe-qltv/src/app/components/admin/user-management/user-management.html`

**Changes**:

- [x] Updated label: "Mật khẩu (Tùy chọn)" instead of "Mật khẩu \*"
- [x] Added placeholder: "Nhập mật khẩu hoặc để trống"
- [x] Added hint text with emoji 💡
- [x] Highlighted default password with `<code>` tag
- [x] No compilation errors

**Code**:

```html
<!-- Lines 361-376 -->
<div *ngIf="modalMode === 'add'">
  <label class="block text-sm font-medium text-gray-700 mb-1">
    Mật khẩu
    <span class="text-gray-500 font-normal text-xs">(Tùy chọn)</span>
  </label>
  <input
    type="password"
    formControlName="password"
    placeholder="Nhập mật khẩu hoặc để trống"
    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
  />
  <p class="mt-1 text-xs text-gray-500">
    💡 Nếu để trống, hệ thống sẽ tự động đặt mật khẩu:
    <code class="bg-gray-100 px-1.5 py-0.5 rounded text-indigo-600 font-medium"
      >password</code
    >
  </p>
</div>
```

---

### File 4: `user-management.ts`

**Location**: `fe-qltv/src/app/components/admin/user-management/user-management.ts`

**Changes**:

- [x] Removed `Validators.required` from password field
- [x] Added comment explaining optional password
- [x] Added success alert with password info
- [x] Added error alert
- [x] No compilation errors

**Code**:

```typescript
// Lines 161-168 - openAddUserModal()
openAddUserModal(): void {
  this.modalMode = 'add';
  this.selectedUser = null;
  this.initializeForm();
  // Password is optional - backend will use default "password" if not provided
  this.userForm.get('password')?.clearValidators();
  this.userForm.get('password')?.updateValueAndValidity();
  this.showUserModal = true;
}

// Lines 204-221 - onSubmit() success callback
next: (newUser: Patron) => {
  console.log('Thêm người dùng mới thành công:', newUser);
  this.loadUsers();
  this.isLoading = false;
  this.closeModal();

  // Show success message with default password info
  const password = formData.password || 'password';
  const message = formData.password
    ? `✅ Tạo người dùng thành công!\n\nEmail: ${formData.email}\nMật khẩu: ${password}\n\nVui lòng thông báo cho người dùng.`
    : `✅ Tạo người dùng thành công!\n\nEmail: ${formData.email}\nMật khẩu mặc định: password\n\nVui lòng thông báo cho người dùng đăng nhập và đổi mật khẩu.`;
  alert(message);
}
```

---

## 🧪 TESTING CHECKLIST

### Pre-Test Setup

- [ ] **Restart Backend**:

  ```cmd
  cd be-qltv
  mvn clean install
  mvn spring-boot:run
  ```

- [ ] **Restart Frontend** (nếu đang chạy):

  ```cmd
  cd fe-qltv
  npm start
  ```

- [ ] **Check Console**: Backend đã khởi động thành công (port 8080)
- [ ] **Check Browser**: Frontend đã load (port 4200)

---

### Test 1: Create User WITHOUT Password (Default)

**Setup**:

- [ ] Login as admin: `admin@qltv.com` / `password`
- [ ] Navigate to User Management: `/admin/users`

**Steps**:

1. [ ] Click "Thêm người dùng"
2. [ ] Fill form:
   - Name: `Test User Default`
   - Email: `testdefault@qltv.com`
   - Password: **[LEAVE EMPTY]** ← Critical!
   - Role: `Người dùng`
   - Phone: `0901234567`
3. [ ] Click "Lưu"

**Expected Results**:

- [ ] Alert shows:

  ```
  ✅ Tạo người dùng thành công!

  Email: testdefault@qltv.com
  Mật khẩu mặc định: password

  Vui lòng thông báo cho người dùng đăng nhập và đổi mật khẩu.
  ```

- [ ] Backend console logs:
  ```
  Creating patron: testdefault@qltv.com with password: password
  ```
- [ ] User appears in table with name "Test User Default"

**Login Test**: 4. [ ] Logout admin 5. [ ] Login with:

- Email: `testdefault@qltv.com`
- Password: `password`

6. [ ] ✅ **Login successful**
7. [ ] Redirected to home page
8. [ ] User can browse books

---

### Test 2: Create User WITH Custom Password

**Setup**:

- [ ] Login as admin again
- [ ] Navigate to User Management

**Steps**:

1. [ ] Click "Thêm người dùng"
2. [ ] Fill form:
   - Name: `Test User Custom`
   - Email: `testcustom@qltv.com`
   - Password: `MySecure123` ← Custom password
   - Role: `Người dùng`
3. [ ] Click "Lưu"

**Expected Results**:

- [ ] Alert shows:

  ```
  ✅ Tạo người dùng thành công!

  Email: testcustom@qltv.com
  Mật khẩu: MySecure123

  Vui lòng thông báo cho người dùng.
  ```

- [ ] Backend console logs:
  ```
  Creating patron: testcustom@qltv.com with password: MySecure123
  ```

**Login Test**: 4. [ ] Logout admin 5. [ ] Login with:

- Email: `testcustom@qltv.com`
- Password: `MySecure123`

6. [ ] ✅ **Login successful**

---

### Test 3: Create LIBRARIAN WITHOUT Password

**Setup**:

- [ ] Login as admin
- [ ] Navigate to User Management

**Steps**:

1. [ ] Click "Thêm người dùng"
2. [ ] Fill form:
   - Name: `Thủ thư Test`
   - Email: `librarian-test@qltv.com`
   - Password: **[LEAVE EMPTY]**
   - Role: `Thủ thư` ← Important!
3. [ ] Click "Lưu"

**Expected Results**:

- [ ] Alert shows default password: `password`
- [ ] Backend logs: `Creating patron: librarian-test@qltv.com with password: password`

**Login Test**: 4. [ ] Logout admin 5. [ ] Login with:

- Email: `librarian-test@qltv.com`
- Password: `password`

6. [ ] ✅ **Login successful**
7. [ ] Logo shows: **"QLTV Thủ thư"**
8. [ ] URL redirects to: `/librarian/books`
9. [ ] Sidebar shows only 11 items (no Users/Reviews/Payments)
10. [ ] Can access Books, Loan Confirm, Categories, etc.

---

### Test 4: Create ADMIN WITH Custom Password

**Setup**:

- [ ] Login as existing admin
- [ ] Navigate to User Management

**Steps**:

1. [ ] Click "Thêm người dùng"
2. [ ] Fill form:
   - Name: `Admin Test`
   - Email: `admin-test@qltv.com`
   - Password: `Admin@2025`
   - Role: `Quản trị viên`
3. [ ] Click "Lưu"

**Login Test**: 4. [ ] Logout 5. [ ] Login with:

- Email: `admin-test@qltv.com`
- Password: `Admin@2025`

6. [ ] ✅ **Login successful**
7. [ ] Logo shows: **"QLTV Admin"**
8. [ ] URL redirects to: `/admin/books`
9. [ ] Sidebar shows all 14 items
10. [ ] Can access Users, Reviews, Payments

---

### Test 5: Empty Email (Validation)

**Steps**:

1. [ ] Click "Thêm người dùng"
2. [ ] Leave Email empty
3. [ ] Try to submit

**Expected**:

- [ ] Form validation prevents submission
- [ ] Email field shows red border
- [ ] No backend request sent

---

### Test 6: Duplicate Email

**Steps**:

1. [ ] Try to create user with email: `admin@qltv.com` (existing)
2. [ ] Click "Lưu"

**Expected**:

- [ ] Backend returns error
- [ ] Frontend shows alert: `❌ Lỗi khi tạo người dùng! Vui lòng thử lại.`
- [ ] User not created

---

### Test 7: Edit Existing User (Should NOT Show Password)

**Steps**:

1. [ ] Click edit icon (✏️) on any existing user
2. [ ] Check form fields

**Expected**:

- [ ] Password field **DOES NOT APPEAR** (only for add mode)
- [ ] Can update name, email, role, phone, address
- [ ] Cannot change password through this form

---

### Test 8: Self-Registration Still Works

**Setup**:

- [ ] Logout completely
- [ ] Go to registration page

**Steps**:

1. [ ] Fill registration form:
   - Name: `Self Registered User`
   - Email: `selfreg@qltv.com`
   - Password: `SelfPass123`
   - Phone: `0909999999`
2. [ ] Submit

**Expected**:

- [ ] Registration successful
- [ ] Login with `selfreg@qltv.com` / `SelfPass123`
- [ ] ✅ **Login successful**
- [ ] Role is USER by default

---

## 🔍 DATABASE VERIFICATION

### Check Password Hashes

**SQL Query**:

```sql
SELECT
    id,
    name,
    email,
    role,
    password,
    LENGTH(password) as hash_length
FROM patron
WHERE email IN (
    'testdefault@qltv.com',
    'testcustom@qltv.com',
    'librarian-test@qltv.com',
    'admin-test@qltv.com'
)
ORDER BY id DESC;
```

**Expected Results**:

- [ ] All passwords start with `$2a$10$` (BCrypt)
- [ ] `hash_length` = 60 characters
- [ ] Different hashes for different passwords
- [ ] Users with default password have hash matching:
  ```
  $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
  ```

---

## 🐛 ERROR SCENARIOS

### Scenario 1: Backend Not Running

**Steps**:

1. [ ] Stop backend
2. [ ] Try to create user in frontend

**Expected**:

- [ ] Alert: `❌ Lỗi khi tạo người dùng! Vui lòng thử lại.`
- [ ] Console error: Connection refused

---

### Scenario 2: Invalid Email Format

**Steps**:

1. [ ] Enter email: `notanemail`
2. [ ] Try to submit

**Expected**:

- [ ] Form validation prevents submission
- [ ] Email field shows validation error

---

### Scenario 3: Too Short Name

**Steps**:

1. [ ] Enter name: `A` (1 character)
2. [ ] Try to submit

**Expected**:

- [ ] Form validation prevents submission
- [ ] Name requires minimum 2 characters

---

## 📊 REGRESSION TESTS

### Existing Features Should Still Work

**Books Management**:

- [ ] Admin can view books
- [ ] Admin can add/edit/delete books
- [ ] Librarian can view/edit books
- [ ] User can view books only

**Loan Management**:

- [ ] Librarian can confirm loans
- [ ] Users can borrow books
- [ ] Late fees calculated correctly

**Categories**:

- [ ] Admin/Librarian can manage categories
- [ ] Users can browse by category

**Reviews**:

- [ ] Users can leave reviews
- [ ] Admin can manage reviews

---

## 🎯 SUCCESS CRITERIA

**Must Pass**:

- [x] Backend compiles without errors
- [x] Frontend compiles without errors
- [ ] Test 1: Default password works ✅
- [ ] Test 2: Custom password works ✅
- [ ] Test 3: Librarian login with default password ✅
- [ ] Test 4: Admin login with custom password ✅
- [ ] All passwords properly hashed in database
- [ ] Success alerts display correct password
- [ ] No regression in existing features

**Nice to Have**:

- [ ] Console logs show correct passwords
- [ ] Database hashes verified
- [ ] All edge cases tested

---

## 📝 DEPLOYMENT STEPS

### 1. Backend Deployment

```bash
cd be-qltv

# Clean and build
mvn clean install

# Run tests
mvn test

# Package JAR
mvn package

# Run application
java -jar target/be-qltv-0.0.1-SNAPSHOT.jar
```

---

### 2. Frontend Deployment

```bash
cd fe-qltv

# Install dependencies (if needed)
npm install

# Build for production
npm run build

# Files will be in dist/ folder
```

---

### 3. Database Migration (Optional)

**If you have existing users with wrong passwords**:

```sql
-- Fix existing librarian accounts
UPDATE patron
SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE role = 'LIBRARIAN'
  AND email IN ('thuthu@gmail.com', 'librarian1@qltv.com', 'librarian2@qltv.com');
```

---

## 🎉 COMPLETION SUMMARY

**Total Changes**:

- ✅ 2 Backend files modified (PatronDTO, PatronService)
- ✅ 2 Frontend files modified (user-management.html, user-management.ts)
- ✅ 3 Documentation files created
- ✅ 0 Compilation errors
- ⏳ 8 Core test scenarios
- ⏳ 3 Edge case scenarios
- ⏳ 5 Regression tests

**Estimated Testing Time**: 30-45 minutes

**Status**: 🟢 **READY FOR TESTING**

---

## 🚀 QUICK START

**Run everything**:

Terminal 1 (Backend):

```cmd
cd d:\java\QLTV\be-qltv
mvn spring-boot:run
```

Terminal 2 (Frontend):

```cmd
cd d:\java\QLTV\fe-qltv
npm start
```

Browser:

```
http://localhost:4200/admin/users
```

**First Test**:

1. Login: `admin@qltv.com` / `password`
2. Create user WITHOUT password
3. Logout
4. Login with new user / `password`
5. ✅ Success!

---

**Ngày tạo**: 08/10/2025  
**Developer**: GitHub Copilot  
**Status**: 🟢 Ready for QA
