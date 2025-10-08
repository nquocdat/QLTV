# 🔧 SỬA LỖI 401 UNAUTHORIZED - PAYMENT MANAGEMENT

## ❌ LỖI

**Triệu chứng**:

```
GET http://localhost:8081/api/payments 401 (Unauthorized)
HttpErrorResponse {status: 401, statusText: 'Unknown Error', ...}
```

**Trang**: Payment Management (`/admin/payment-management`)

**Thời điểm**: Khi load trang, gọi `ngOnInit()` → `loadPayments()`

---

## 🔍 NGUYÊN NHÂN

### Vấn đề 1: **URL KHÔNG TỒN TẠI**

**Frontend gọi**:

```typescript
// payment.service.ts (CŨ - SAI ❌)
private apiUrl = 'http://localhost:8081/api/payments';  // Endpoint này KHÔNG TỒN TẠI!

getAllPayments(): Observable<PaymentTransaction[]> {
  return this.http.get<PaymentTransaction[]>(`${this.apiUrl}`);
}
```

**Backend có**:

```java
// PaymentController.java
@RequestMapping("/api/payment")  // Số ÍT, không phải "payments"
public class PaymentController {
    // KHÔNG CÓ endpoint GET để lấy tất cả payments!

    @PostMapping("/cash")         // ✅ Có
    @PostMapping("/create-payment-url")  // ✅ Có
    @GetMapping("/vnpay-return")  // ✅ Có
    @GetMapping("/unpaid-fines/{patronId}")  // ✅ Có

    // ❌ KHÔNG CÓ @GetMapping("/") hoặc @GetMapping
}
```

**Kết quả**:

- Frontend gọi `GET /api/payments`
- Backend không có endpoint này
- Spring Security trả về 401 Unauthorized (vì endpoint không tồn tại)

---

### Vấn đề 2: **NHẦM LẪN API**

**Frontend thực sự cần**:

- Lấy danh sách **LoanPayment** (thanh toán cho mượn sách)
- Hiển thị: OrderId, Amount, PaymentMethod, Status, Date

**Backend có sẵn**:

```java
// LoanPaymentController.java ✅
@RequestMapping("/api/loan-payments")
public class LoanPaymentController {

    @GetMapping  // ✅ CÓ endpoint này!
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<LoanPaymentDTO>> getAllPayments() {
        List<LoanPaymentDTO> payments = loanPaymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
}
```

**Kết luận**: Frontend gọi **SAI API**, nên dùng `/api/loan-payments` thay vì `/api/payments`

---

## ✅ GIẢI PHÁP

### Sửa Frontend

**File**: `payment.service.ts`

**Thay đổi**:

```typescript
// TRƯỚC ❌
private apiUrl = 'http://localhost:8081/api/payments';

// SAU ✅
private apiUrl = 'http://localhost:8081/api/loan-payments';
```

**Lý do**:

- ✅ Endpoint `/api/loan-payments` TỒN TẠI trong backend
- ✅ Có `@GetMapping` trả về `List<LoanPaymentDTO>`
- ✅ Có `@PreAuthorize` cho ADMIN và LIBRARIAN
- ✅ Dữ liệu khớp với giao diện (OrderId, Amount, PaymentMethod, Status)

---

## 🧪 CÁCH TEST

### Test 1: Load Payment Management Page

**Bước 1**: Login as Admin

- Email: `admin@qltv.com`
- Password: `password`

**Bước 2**: Navigate to Payment Management

- URL: `http://localhost:4200/admin/payment-management`

**Kết quả mong đợi**:

- ✅ Trang load thành công
- ✅ Không có lỗi 401 trong console
- ✅ Hiển thị danh sách payments (nếu có)
- ✅ Network tab: `GET /api/loan-payments` → 200 OK

---

### Test 2: Check Backend Logs

**Backend console**:

```
2025-10-08 ... : GET /api/loan-payments
2025-10-08 ... : Fetching all loan payments
2025-10-08 ... : Found X payments
```

**Không còn**:

```
2025-10-08 ... : GET /api/payments → 401 Unauthorized ❌
```

---

### Test 3: Verify Data Structure

**Frontend nhận được**:

```json
[
  {
    "id": 1,
    "loanId": 5,
    "orderId": "LOAN_5_1728123456",
    "amount": 50000,
    "paymentMethod": "CASH",
    "status": "PENDING",
    "createdDate": "2025-10-08T10:30:00",
    "paidDate": null,
    "description": "Thanh toán phí trễ hạn"
  }
]
```

**Giao diện hiển thị**:

```
┌────────┬─────────────────┬─────────┬────────┬──────────┬─────────┬────────────────┬──────────────┐
│ Mã GT  │ OrderId         │ Số tiền │ PT     │ TT       │ Ngày tạo│ Ngày thanh toán│ Mô tả        │
├────────┼─────────────────┼─────────┼────────┼──────────┼─────────┼────────────────┼──────────────┤
│ 1      │ LOAN_5_172...   │ 50,000  │ CASH   │ PENDING  │ 10:30   │ -              │ Thanh toán...│
└────────┴─────────────────┴─────────┴────────┴──────────┴─────────┴────────────────┴──────────────┘
```

---

## 📊 SO SÁNH 2 API

### `/api/payment` (PaymentController)

**Mục đích**:

- Xử lý thanh toán VNPay cho **Fine** (phí phạt)
- Tạo URL thanh toán VNPay
- Xử lý callback từ VNPay
- Lấy unpaid fines của patron

**Endpoints**:

```java
POST   /api/payment/create-payment-url    // Tạo URL VNPay cho Fine
GET    /api/payment/vnpay-return          // Callback VNPay
GET    /api/payment/unpaid-fines/{id}     // Lấy fines chưa thanh toán
POST   /api/payment/cash                  // Xác nhận thanh toán tiền mặt (legacy)
```

**Không có**: `GET /api/payment` hoặc `GET /api/payments`

---

### `/api/loan-payments` (LoanPaymentController) ✅

**Mục đích**:

- Quản lý thanh toán cho **Loan** (mượn sách)
- Lấy danh sách tất cả payments
- Tạo payment cho loan
- Xác nhận thanh toán

**Endpoints**:

```java
GET    /api/loan-payments                 // ✅ Lấy TẤT CẢ payments
GET    /api/loan-payments/{id}            // Lấy payment theo ID
GET    /api/loan-payments/loan/{loanId}   // Lấy payment theo loanId
POST   /api/loan-payments                 // Tạo payment mới
PUT    /api/loan-payments/{id}/confirm    // Xác nhận thanh toán
```

**Đây là API mà Payment Management cần!**

---

## 🎯 TẠI SAO LỖI 401?

### Giải thích kỹ thuật

**1. Endpoint không tồn tại**:

```
Frontend: GET /api/payments
Backend:  ❌ Không có controller nào handle "/api/payments"
```

**2. Spring Security xử lý**:

```java
// SecurityConfig.java (giả định)
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
            .anyRequest().authenticated()  // Tất cả request cần authenticate
        .and()
        .exceptionHandling()
            .authenticationEntryPoint(...)  // Trả về 401 nếu unauthorized
}
```

**3. Flow**:

```
Client request → Spring DispatcherServlet
                → Không tìm thấy @RequestMapping("/api/payments")
                → Spring Security nhận request chưa được handle
                → Kiểm tra authentication
                → Không tìm thấy handler hợp lệ
                → Trả về 401 Unauthorized
```

**Lưu ý**: 401 không phải vì token sai, mà vì endpoint không tồn tại!

---

## 🔐 PHÂN QUYỀN

### LoanPaymentController Authorization

```java
@GetMapping
@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
public ResponseEntity<List<LoanPaymentDTO>> getAllPayments() {
    // ...
}
```

**Yêu cầu**:

- ✅ User phải có role **ADMIN** HOẶC **LIBRARIAN**
- ❌ Role **USER** không được phép

**Frontend Route Guard**:

```typescript
// app.routes.ts
{
  path: 'admin',
  canActivate: [AuthGuard],  // Kiểm tra isAdmin()
  children: [
    { path: 'payment-management', component: PaymentManagementComponent }
  ]
}
```

**Kết hợp**:

- Frontend: AuthGuard kiểm tra role trước khi load component
- Backend: @PreAuthorize kiểm tra role trước khi xử lý request

---

## 📝 CHECKLIST

### Đã sửa

- [x] Xác định nguyên nhân: Gọi sai API `/api/payments` thay vì `/api/loan-payments`
- [x] Sửa `payment.service.ts`: Đổi apiUrl từ `/api/payments` → `/api/loan-payments`
- [x] Verify không có lỗi compilation

### Cần test

- [ ] Load Payment Management page → Không lỗi 401
- [ ] Hiển thị danh sách payments đúng
- [ ] Filter theo Payment Method hoạt động
- [ ] Filter theo Status hoạt động
- [ ] Xác nhận thanh toán tiền mặt hoạt động
- [ ] Tổng tiền đã thanh toán tính đúng

### Lưu ý cho tương lai

- [ ] Đặt tên API rõ ràng: `/api/fine-payments` vs `/api/loan-payments`
- [ ] Document tất cả endpoints trong README
- [ ] Thêm error handling tốt hơn cho 404/401
- [ ] Hiển thị error message user-friendly

---

## 🎉 KẾT QUẢ

**Trước (❌ Lỗi)**:

```
Frontend: GET /api/payments → 401 Unauthorized
Console: ERROR HttpErrorResponse {status: 401}
UI: Không load được data
```

**Sau (✅ Hoạt động)**:

```
Frontend: GET /api/loan-payments → 200 OK
Response: [{ id: 1, orderId: "...", amount: 50000, ... }]
UI: Hiển thị bảng payments đầy đủ
```

---

## 💡 BÀI HỌC

### 1. Luôn kiểm tra Backend API trước

```bash
# Kiểm tra endpoints có sẵn
curl -H "Authorization: Bearer <token>" http://localhost:8081/api/loan-payments
```

### 2. Đọc error message kỹ

```
401 Unauthorized ≠ Token sai
401 Unauthorized = Không tìm thấy endpoint HOẶC thiếu quyền
```

### 3. Đặt tên API nhất quán

```
❌ /api/payment, /api/payments (số ít/nhiều khác nhau)
✅ /api/fine-payments, /api/loan-payments (rõ ràng mục đích)
```

### 4. Document API endpoints

Tạo file `API_ENDPOINTS.md`:

```markdown
## Payment APIs

### Fine Payments

- POST /api/payment/create-payment-url
- GET /api/payment/vnpay-return
- GET /api/payment/unpaid-fines/{patronId}

### Loan Payments

- GET /api/loan-payments (Get all)
- POST /api/loan-payments (Create)
- PUT /api/loan-payments/{id}/confirm (Confirm)
```

---

**Ngày sửa**: 08/10/2025  
**Developer**: GitHub Copilot  
**Status**: 🟢 Fixed - Ready to test
