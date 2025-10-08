# CHECKLIST KIỂM TRA NHANH

## ✅ Đã Hoàn Thành

### Backend

- [x] `LoanService.returnBook()` - Tính phí quá hạn tự động (5,000 VND/ngày)
- [x] `LoanService.returnBookWithDamageFine()` - Xử lý sách hư hỏng
- [x] `LoanController` endpoint mới: `PUT /{loanId}/return-with-damage`
- [x] `LoanPaymentController.confirmCashPayment()` - Đã có sẵn

### Frontend

- [x] `StatusTranslatorService` - Dịch tất cả trạng thái sang tiếng Việt
- [x] `LoanService.returnBookWithDamage()` method
- [x] `LoanManagement` component:
  - [x] Properties: showReturnModal, returningLoan, isDamaged, damageFine, damageNotes
  - [x] Methods: openReturnModal, closeReturnModal
  - [x] Methods: calculateOverdueDays, calculateOverdueFine, calculateTotalFine
  - [x] Methods: confirmReturnBook, confirmCashPayment
- [x] `loan-management.html`:
  - [x] Nút "Xác nhận thanh toán tiền mặt" (PENDING_PAYMENT + CASH)
  - [x] Nút "Trả sách" (BORROWED/OVERDUE)
  - [x] Modal trả sách với form đầy đủ
  - [x] Dịch trạng thái sang tiếng Việt

## 🧪 CẦN KIỂM TRA

### 1. Khởi động ứng dụng

```bash
# Terminal 1 - Backend
cd be-qltv
mvnw spring-boot:run

# Terminal 2 - Frontend
cd fe-qltv
npm start
```

### 2. Test trả sách không hư hỏng

- [ ] Vào "Quản lý mượn/trả"
- [ ] Tìm sách có status "Đang mượn" hoặc "Quá hạn"
- [ ] Click nút "Trả sách" (icon mũi tên quay về, màu xanh dương)
- [ ] Modal hiển thị đầy đủ thông tin
- [ ] Nếu quá hạn → Hiển thị cảnh báo đỏ với số ngày và phí
- [ ] Không check "Sách bị hỏng"
- [ ] Click "Xác nhận trả sách"
- [ ] Kiểm tra:
  - [ ] Loan status → "Chờ thanh toán" (nếu có phí) hoặc "Đã trả"
  - [ ] Copy status → "Có sẵn"
  - [ ] Payment được tạo với đúng số tiền

### 3. Test trả sách bị hư hỏng

- [ ] Mở modal trả sách cho loan khác
- [ ] Check "Sách bị hỏng"
- [ ] Form mở rộng với 2 trường:
  - [ ] Input "Phí phạt hư hỏng (VND)" - bắt buộc
  - [ ] Textarea "Ghi chú về hư hỏng" - tùy chọn
- [ ] Nhập phí hư hỏng (VD: 50000)
- [ ] Nhập ghi chú (VD: "Bìa sách bị rách")
- [ ] Xem phần "Tổng phí phạt":
  - [ ] Hiển thị phí quá hạn (nếu có)
  - [ ] Hiển thị phí hư hỏng
  - [ ] Tổng cộng chính xác
- [ ] Click "Xác nhận trả sách"
- [ ] Kiểm tra:
  - [ ] Copy status → "Đang sửa chữa"
  - [ ] Payment có tổng phí chính xác
  - [ ] Ghi chú được lưu

### 4. Test xác nhận thanh toán tiền mặt

- [ ] Sau khi trả sách có phí → Status = "Chờ thanh toán"
- [ ] Phương thức thanh toán = "Tiền mặt"
- [ ] Nút "Xác nhận thanh toán tiền mặt" hiển thị (icon tiền, màu xanh lá)
- [ ] Click nút
- [ ] Kiểm tra:
  - [ ] Payment status → "Đã xác nhận"
  - [ ] Loan status → "Đã trả"
  - [ ] Thông báo thành công

### 5. Test tiếng Việt

Tất cả trạng thái phải hiển thị tiếng Việt:

- [ ] "Đang mượn" (BORROWED)
- [ ] "Quá hạn" (OVERDUE)
- [ ] "Chờ thanh toán" (PENDING_PAYMENT)
- [ ] "Đã trả" (RETURNED)
- [ ] "Tiền mặt" (CASH)
- [ ] "VNPay" (VNPAY)
- [ ] "Chờ xác nhận" (PENDING)
- [ ] "Đã xác nhận" (CONFIRMED)

### 6. Test ISBN tự động điền

- [ ] Vào "Quản lý sách"
- [ ] Click nút "Sửa" trên bất kỳ sách nào
- [ ] Kiểm tra ISBN đã tự động điền trong form
- [ ] ✅ (Tính năng này đã có sẵn từ trước)

## 📊 Kịch Bản Test Chi Tiết

### Kịch bản 1: Trả sách đúng hạn

**Input**: Loan đang mượn, chưa quá hạn
**Actions**:

1. Click "Trả sách"
2. Modal hiển thị, không có cảnh báo
3. Xác nhận

**Expected**:

- Copy status = "Có sẵn"
- Loan status = "Đã trả"
- Không có payment (vì không có phí)

### Kịch bản 2: Trả sách quá hạn 3 ngày

**Input**: Loan quá hạn 3 ngày
**Actions**:

1. Click "Trả sách"
2. Modal hiển thị cảnh báo đỏ: "Số ngày quá hạn: 3 ngày, Phí phạt: 15,000 VND"
3. Xác nhận

**Expected**:

- Copy status = "Có sẵn"
- Loan status = "Chờ thanh toán"
- Payment: 15,000 VND, method = CASH, status = PENDING

### Kịch bản 3: Trả sách hư hỏng + quá hạn

**Input**: Loan quá hạn 2 ngày
**Actions**:

1. Click "Trả sách"
2. Cảnh báo: "Số ngày quá hạn: 2 ngày, Phí phạt: 10,000 VND"
3. Check "Sách bị hỏng"
4. Nhập phí hư hỏng: 50,000
5. Nhập ghi chú: "Trang 45 bị rách, bìa bị cong"
6. Tổng phí hiển thị: 60,000 VND
7. Xác nhận

**Expected**:

- Copy status = "Đang sửa chữa"
- Loan status = "Chờ thanh toán"
- Payment: 60,000 VND (10K quá hạn + 50K hư hỏng)
- Ghi chú được lưu

### Kịch bản 4: Xác nhận tiền mặt

**Input**: Loan với status = "Chờ thanh toán", payment method = "Tiền mặt"
**Actions**:

1. Nút "Xác nhận thanh toán tiền mặt" hiển thị
2. Click nút

**Expected**:

- Payment status = "Đã xác nhận"
- Loan status = "Đã trả"
- Thông báo: "Xác nhận thanh toán thành công"

### Kịch bản 5: Validation

**Input**: Mở modal trả sách
**Actions**:

1. Check "Sách bị hỏng"
2. Không nhập phí hư hỏng (để trống hoặc 0)
3. Cố click "Xác nhận trả sách"

**Expected**:

- Nút "Xác nhận trả sách" bị disable
- Không thể submit form

## 🐛 Lỗi Thường Gặp

### Lỗi 1: Modal không mở

**Nguyên nhân**: `showReturnModal` không được set đúng
**Kiểm tra**: Console log trong `openReturnModal()`

### Lỗi 2: Phí tính sai

**Nguyên nhân**: Múi giờ hoặc logic tính ngày
**Kiểm tra**: Console log `calculateOverdueDays()` và `calculateOverdueFine()`

### Lỗi 3: Nút không hiển thị

**Nguyên nhân**: Điều kiện `*ngIf` không đúng
**Kiểm tra**:

- Loan status có đúng "BORROWED" hoặc "OVERDUE"?
- Với cash payment: có đúng "PENDING_PAYMENT" + "CASH"?

### Lỗi 4: Không thể submit

**Nguyên nhân**: FormsModule chưa import hoặc validation
**Kiểm tra**:

- FormsModule đã import trong component?
- Nếu isDamaged = true, damageFine phải > 0

## ✨ Các Cải Tiến Tương Lai

1. **Báo cáo phí phạt**:

   - Tổng phí phạt theo tháng/năm
   - Top độc giả có phí phạt cao nhất
   - Biểu đồ xu hướng

2. **Thông báo tự động**:

   - Email/SMS nhắc trả sách trước hạn
   - Thông báo khi sắp quá hạn
   - Thông báo phí phạt

3. **Quản lý sách hư hỏng**:

   - Dashboard riêng cho sách đang sửa chữa
   - Lịch sử sửa chữa
   - Chi phí sửa chữa

4. **Gia hạn tự động**:

   - Cho phép gia hạn online
   - Giới hạn số lần gia hạn
   - Không cho gia hạn nếu có người đặt trước

5. **Tích hợp thanh toán online**:
   - VNPay cho phí phạt (đã có infrastructure)
   - Momo, ZaloPay
   - Thanh toán qua thẻ

---

**Ghi chú**: Checklist này giúp đảm bảo tất cả tính năng hoạt động đúng. Hãy test từng kịch bản để phát hiện lỗi sớm!
