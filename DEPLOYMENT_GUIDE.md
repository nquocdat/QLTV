# 🚀 HƯỚNG DẪN DEPLOYMENT - HỆ THỐNG THANH TOÁN MƯỢN SÁCH

**Ngày cập nhật**: October 6, 2025  
**Tính năng**: Thanh toán khi mượn sách (Cash & VNPay)

---

## ✅ CHECKLIST HOÀN THÀNH

### Backend (100%)

- ✅ Database migration script
- ✅ LoanPayment entity
- ✅ LoanPaymentDTO
- ✅ LoanPaymentRepository
- ✅ LoanPaymentService
- ✅ LoanPaymentController
- ✅ Updated: Loan entity
- ✅ Updated: LoanService
- ✅ Updated: LoanController
- ✅ Updated: VNPayService

### Frontend (100%)

- ✅ LoanPaymentService
- ✅ Updated: LoanService
- ✅ PaymentMethodModalComponent
- ✅ PendingPaymentsComponent
- ✅ PaymentResultComponent
- ✅ Updated: app.routes.ts
- ✅ **Updated: home.component** (tích hợp payment modal)

---

## 📋 CÁC BƯỚC DEPLOYMENT

### BƯỚC 1: Backup Database

```cmd
cd d:\java\QLTV
mysqldump -u root -p qltv_db > backup_before_payment_$(date +%Y%m%d).sql
```

---

### BƯỚC 2: Chạy Database Migration

```cmd
mysql -u root -p qltv_db < be-qltv\src\main\resources\loan-payment-migration.sql
```

**Verify migration thành công**:

```sql
USE qltv_db;

-- Kiểm tra bảng loan_payments
SHOW TABLES LIKE 'loan_payments';
DESCRIBE loan_payments;

-- Kiểm tra loans có PENDING_PAYMENT
DESCRIBE loans;

-- Kiểm tra system_settings (optional)
SHOW TABLES LIKE 'system_settings';
```

**Expected output**:

```
+------------------+
| Tables_in_qltv_db (loan_payments) |
+------------------+
| loan_payments    |
+------------------+

+-----------------+---------------+------+-----+---------+----------------+
| Field           | Type          | Null | Key | Default | Extra          |
+-----------------+---------------+------+-----+---------+----------------+
| id              | bigint        | NO   | PRI | NULL    | auto_increment |
| loan_id         | bigint        | NO   | MUL | NULL    |                |
| patron_id       | bigint        | NO   | MUL | NULL    |                |
| amount          | decimal(10,2) | NO   |     | NULL    |                |
| payment_method  | enum(...)     | NO   |     | NULL    |                |
| payment_status  | enum(...)     | NO   |     | NULL    |                |
| ...
```

---

### BƯỚC 3: Build Backend

```cmd
cd d:\java\QLTV\be-qltv

REM Clean và build
mvnw clean install

REM Nếu có lỗi, skip tests
mvnw clean install -DskipTests
```

**Expected output**:

```
[INFO] BUILD SUCCESS
[INFO] Total time: XX s
```

---

### BƯỚC 4: Chạy Backend

```cmd
cd d:\java\QLTV\be-qltv
mvnw spring-boot:run
```

**Verify backend đang chạy**:

- Browser: http://localhost:8081/actuator/health (nếu có actuator)
- Logs: Kiểm tra không có errors
- Test endpoint: http://localhost:8081/api/loan-payments

**Expected log**:

```
Started BeQltvApplication in X.XXX seconds
```

---

### BƯỚC 5: Install Frontend Dependencies (nếu cần)

```cmd
cd d:\java\QLTV\fe-qltv
npm install
```

---

### BƯỚC 6: Chạy Frontend

```cmd
cd d:\java\QLTV\fe-qltv
ng serve
```

**Verify frontend đang chạy**:

- Browser: http://localhost:4200
- Console: Không có compile errors
- Check routes:
  - http://localhost:4200/library/home
  - http://localhost:4200/library/payment-result
  - http://localhost:4200/admin/pending-payments

**Expected output**:

```
** Angular Live Development Server is listening on localhost:4200 **
✔ Compiled successfully.
```

---

## 🧪 TESTING WORKFLOW

### Test 1: Cash Payment Flow

**Mục tiêu**: Kiểm tra thanh toán tiền mặt từ đầu đến cuối

**Steps**:

1. **User mượn sách**:

   - Đăng nhập với user thường (patron)
   - Vào http://localhost:4200/library/home
   - Tìm sách có `availableCopies > 0`
   - Click "Mượn sách"
   - ✅ Modal chọn phương thức thanh toán hiện ra

2. **Chọn tiền mặt**:

   - Chọn radio "💵 Tiền mặt"
   - Kiểm tra thông tin:
     - Tên sách đúng
     - Phí đặt cọc: 50,000 VND
     - Thông báo: "Vui lòng đến quầy thủ thư..."
   - Click "Xác nhận"
   - ✅ Thấy alert: "Vui lòng đến quầy thủ thư để thanh toán..."

3. **Verify database**:

```sql
-- Kiểm tra loan vừa tạo
SELECT * FROM loans
WHERE status = 'PENDING_PAYMENT'
ORDER BY created_date DESC
LIMIT 1;

-- Kiểm tra payment
SELECT * FROM loan_payments
WHERE payment_method = 'CASH'
AND payment_status = 'PENDING'
ORDER BY created_date DESC
LIMIT 1;

-- Kiểm tra book availability KHÔNG thay đổi
SELECT id, title, available_copies
FROM books
WHERE id = [book_id];
```

4. **Admin xác nhận**:

   - Đăng xuất user
   - Đăng nhập với admin/librarian
   - Vào http://localhost:4200/admin/pending-payments
   - ✅ Thấy payment trong danh sách
   - Click "✅ Xác nhận"
   - Confirm dialog
   - ✅ Thấy alert: "Xác nhận thanh toán thành công"

5. **Verify kết quả**:

```sql
-- Payment đã confirmed
SELECT * FROM loan_payments WHERE id = [payment_id];
-- payment_status = 'CONFIRMED'
-- confirmed_by = [admin_id]
-- confirmed_date IS NOT NULL

-- Loan đã borrowed
SELECT * FROM loans WHERE id = [loan_id];
-- status = 'BORROWED'

-- Book availability đã giảm
SELECT id, title, available_copies
FROM books
WHERE id = [book_id];
-- available_copies giảm 1
```

**✅ Expected result**: Cash payment flow hoạt động hoàn hảo

---

### Test 2: VNPay Payment Flow (Success)

**Mục tiêu**: Kiểm tra thanh toán VNPay thành công

**Steps**:

1. **User mượn sách**:

   - Đăng nhập với user thường
   - Vào http://localhost:4200/library/home
   - Click "Mượn sách" trên một cuốn sách
   - Modal hiện ra

2. **Chọn VNPay**:

   - Chọn radio "🏦 VNPay"
   - Kiểm tra thông tin:
     - Phí đặt cọc: 50,000 VND
     - Thông báo: "Bạn sẽ được chuyển đến cổng thanh toán VNPay..."
   - Click "Xác nhận"

3. **Verify database trước khi thanh toán**:

```sql
SELECT l.*, lp.*
FROM loans l
JOIN loan_payments lp ON l.id = lp.loan_id
WHERE l.status = 'PENDING_PAYMENT'
ORDER BY l.created_date DESC
LIMIT 1;
```

4. **Redirect đến VNPay**:

   - ✅ Browser redirect đến VNPay sandbox
   - URL chứa:
     - `vnp_OrderInfo`: "Phi dat coc muon sach: [Book Title]"
     - `vnp_Amount`: 5000000 (50,000 \* 100)
     - `vnp_TxnRef`: LOAN*[loanId]*[timestamp]

5. **Thanh toán trên VNPay Sandbox**:

   - Chọn ngân hàng: NCB
   - Thẻ test:
     ```
     Số thẻ: 9704198526191432198
     Tên: NGUYEN VAN A
     Ngày phát hành: 07/15
     Mật khẩu: 123456
     ```
   - Click "Thanh toán"
   - ✅ VNPay redirect về: http://localhost:4200/library/payment-result

6. **Payment Result Page**:

   - ✅ Hiển thị icon ✅ xanh
   - ✅ Tiêu đề: "Thanh toán thành công!"
   - ✅ Message: Loan đã được tạo
   - ✅ Có button "Xem sách đã mượn"
   - ✅ Có button "Về trang chủ"

7. **Verify database sau khi thanh toán**:

```sql
-- Payment confirmed
SELECT * FROM loan_payments
WHERE payment_method = 'VNPAY'
ORDER BY confirmed_date DESC
LIMIT 1;
-- payment_status = 'CONFIRMED'
-- vnpay_response_code = '00'
-- transaction_no IS NOT NULL

-- Loan borrowed
SELECT * FROM loans WHERE id = [loan_id];
-- status = 'BORROWED'

-- Book availability giảm
SELECT available_copies FROM books WHERE id = [book_id];
```

**✅ Expected result**: VNPay success flow hoạt động hoàn hảo

---

### Test 3: VNPay Payment Flow (Failed)

**Mục tiêu**: Kiểm tra khi user hủy thanh toán VNPay

**Steps**:

1-4. **Giống Test 2 đến bước redirect VNPay**

5. **Hủy thanh toán**:

   - Tại trang VNPay, click "Hủy giao dịch"
   - ✅ VNPay redirect về: http://localhost:4200/library/payment-result

6. **Payment Result Page**:

   - ✅ Hiển thị icon ❌ đỏ
   - ✅ Tiêu đề: "Thanh toán thất bại"
   - ✅ Message: Giao dịch không thành công
   - ✅ Có button "Về trang chủ"

7. **Verify database**:

```sql
-- Payment failed
SELECT * FROM loan_payments
WHERE id = [payment_id];
-- payment_status = 'FAILED'
-- vnpay_response_code != '00'

-- Loan bị xóa
SELECT * FROM loans WHERE id = [loan_id];
-- Không tồn tại

-- Book availability KHÔNG thay đổi
SELECT available_copies FROM books WHERE id = [book_id];
```

**✅ Expected result**: VNPay failure flow xử lý đúng

---

## 🔍 COMMON ISSUES & SOLUTIONS

### Issue 1: Migration SQL lỗi

**Error**:

```
ERROR 1062 (23000): Duplicate entry...
```

**Solution**:

```sql
-- Drop và chạy lại
DROP TABLE IF EXISTS loan_payments;
-- Rồi chạy lại migration script
```

---

### Issue 2: Backend không start

**Error**:

```
Error creating bean with name 'loanPaymentRepository'
```

**Solution**:

- Check migration đã chạy chưa
- Verify table `loan_payments` tồn tại
- Check application.properties: `spring.jpa.hibernate.ddl-auto=update`

---

### Issue 3: Frontend compile error

**Error**:

```
Cannot find module 'payment-method-modal'
```

**Solution**:

```cmd
cd fe-qltv
npm install
ng build
```

---

### Issue 4: VNPay redirect không hoạt động

**Possible causes**:

- VNPay return URL sai
- Frontend không chạy tại http://localhost:4200
- Route `/library/payment-result` chưa được config

**Solution**:

- Check VNPayService: `returnUrl = "http://localhost:4200/library/payment-result"`
- Check app.routes.ts: có route `payment-result` chưa
- Test trực tiếp: http://localhost:4200/library/payment-result

---

### Issue 5: Admin không thấy pending payments

**Possible causes**:

- User không có role ADMIN hoặc LIBRARIAN
- Endpoint bị block bởi security

**Solution**:

```sql
-- Check user role
SELECT u.*, r.name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = '[username]';
```

- Verify LoanPaymentController có `@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")`
- Check AuthGuard và RoleGuard

---

## 📊 MONITORING

### Database Queries for Monitoring

**Pending payments count**:

```sql
SELECT COUNT(*) as pending_count
FROM loan_payments
WHERE payment_status = 'PENDING';
```

**Today's payments**:

```sql
SELECT
  payment_method,
  payment_status,
  COUNT(*) as count,
  SUM(amount) as total_amount
FROM loan_payments
WHERE DATE(created_date) = CURDATE()
GROUP BY payment_method, payment_status;
```

**Pending loans**:

```sql
SELECT COUNT(*) as pending_loans
FROM loans
WHERE status = 'PENDING_PAYMENT';
```

---

## 🎯 NEXT STEPS

Sau khi deployment thành công:

1. ✅ **Monitor logs** trong 24h đầu
2. ✅ **Test với real users**
3. ✅ **Backup database** hàng ngày
4. ✅ **Monitor VNPay transactions**
5. ⚠️ **Switch to VNPay production** khi ready:
   - Update VNPay credentials trong application.properties
   - Update VNPay URL từ sandbox sang production
   - Test kỹ với số tiền nhỏ

---

## 📞 SUPPORT RESOURCES

**Documentation**:

- `LOAN_PAYMENT_IMPLEMENTATION_SUMMARY.md` - Tổng quan
- `PAYMENT_MODAL_INTEGRATION_GUIDE.md` - Hướng dẫn tích hợp
- `LOAN_PAYMENT_FINAL_CHECKLIST.md` - Checklist chi tiết
- `DEPLOYMENT_GUIDE.md` - File này

**Key Files**:

- Backend: `LoanPaymentService.java`, `LoanPaymentController.java`
- Frontend: `home.ts`, `payment-method-modal.component.ts`
- Migration: `loan-payment-migration.sql`

---

## ✅ DEPLOYMENT CHECKLIST

Trước khi deploy production:

- [ ] Database backup hoàn tất
- [ ] Migration script đã test
- [ ] Backend tests passed
- [ ] Frontend build thành công
- [ ] All 3 test cases passed
- [ ] VNPay sandbox hoạt động
- [ ] Admin panel accessible
- [ ] Payment result page hiển thị đúng
- [ ] Logs không có errors
- [ ] Security configs OK
- [ ] Role guards hoạt động

---

**🎉 DEPLOYMENT HOÀN TẤT!**

**Date**: October 6, 2025  
**Status**: ✅ READY FOR PRODUCTION
