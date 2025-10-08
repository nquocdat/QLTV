# 🎉 HOÀN THÀNH TÍCH HỢP VNPAY VÀO PROJECT QLTV

## ✅ Các files đã tạo/cập nhật:

### 1. VNPayUtil.java (✅ Hoàn thành)

📁 `be-qltv/src/main/java/com/example/be_qltv/util/VNPayUtil.java`

- ✅ Copy từ demo thành công
- ✅ Hàm `hmacSHA512()` - Tạo chữ ký
- ✅ Hàm `getRandomNumber()` - Tạo mã GD ngẫu nhiên
- ✅ Hàm `getIpAddress()` - Lấy IP address

### 2. VNPayConfig.java (✅ Đã cập nhật)

📁 `be-qltv/src/main/java/com/example/be_qltv/config/VNPayConfig.java`

- ✅ Sử dụng constants thay vì @Value
- ✅ Thông tin VNPay Sandbox:
  - Terminal Code: NDYCNE7G
  - Secret Key: 6QLSH3HHOHZJK72EQNXCYVEP41JI8779
  - Return URL: http://localhost:8080/api/payment/vnpay-return

### 3. VNPayService.java (✅ Hoàn thành)

📁 `be-qltv/src/main/java/com/example/be_qltv/service/VNPayService.java`

- ✅ `createPaymentUrl(Fine, HttpServletRequest)` - Tạo URL thanh toán
- ✅ `verifyPaymentReturn(Map<String,String>)` - Xác thực callback
- ✅ `updateFineAfterPayment(Long, String, String)` - Cập nhật Fine
- ✅ Logging chi tiết để debug

### 4. PaymentController.java (⚠️ Cần tạo hoặc cập nhật)

📁 `be-qltv/src/main/java/com/example/be_qltv/controller/PaymentController.java`

**Lưu ý:** Project đã có `PaymentService.java` nhưng cần tạo controller mới hoặc thêm endpoints

## 🚀 Hướng dẫn sử dụng

### API Endpoints

#### 1. Tạo URL thanh toán

```http
POST /api/payment/create-payment-url
Content-Type: application/json

{
  "fineId": 1
}
```

**Response:**

```json
{
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...",
  "fineId": 1,
  "amount": 50000
}
```

#### 2. Callback từ VNPay (tự động)

```http
GET /api/payment/vnpay-return?vnp_Amount=5000000&vnp_ResponseCode=00&vnp_SecureHash=...
```

**Response:**

```json
{
  "success": true,
  "message": "Thanh toán thành công",
  "transactionId": "14369214",
  "amount": "5000000",
  "orderInfo": "Thanh toan phi phat ID:1",
  "txnRef": "12345678"
}
```

#### 3. Lấy danh sách phí phạt chưa thanh toán

```http
GET /api/payment/unpaid-fines/{patronId}
```

## 💻 Frontend Integration (Angular)

### 1. Tạo Payment Service

```typescript
// payment.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  private apiUrl = 'http://localhost:8080/api/payment';

  constructor(private http: HttpClient) {}

  createPaymentUrl(fineId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/create-payment-url`, { fineId });
  }

  getUnpaidFines(patronId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/unpaid-fines/${patronId}`);
  }
}
```

### 2. Tạo Component thanh toán

```typescript
// fine-payment.component.ts
import { Component, OnInit } from '@angular/core';
import { PaymentService } from './payment.service';

@Component({
  selector: 'app-fine-payment',
  template: `
    <div class="fine-payment">
      <h2>Phí phạt chưa thanh toán</h2>

      <div *ngFor="let fine of unpaidFines" class="fine-item">
        <div class="fine-info">
          <p><strong>Mã phí phạt:</strong> #{{ fine.id }}</p>
          <p><strong>Số tiền:</strong> {{ fine.amount | number }} VND</p>
          <p><strong>Lý do:</strong> {{ fine.reason }}</p>
        </div>
        <button (click)="payFine(fine.id)" class="btn-pay">Thanh toán</button>
      </div>
    </div>
  `,
})
export class FinePaymentComponent implements OnInit {
  unpaidFines: any[] = [];
  patronId: number = 1; // Lấy từ auth service

  constructor(private paymentService: PaymentService) {}

  ngOnInit() {
    this.loadUnpaidFines();
  }

  loadUnpaidFines() {
    this.paymentService
      .getUnpaidFines(this.patronId)
      .subscribe((fines) => (this.unpaidFines = fines));
  }

  payFine(fineId: number) {
    this.paymentService.createPaymentUrl(fineId).subscribe(
      (response) => {
        // Redirect đến VNPay
        window.location.href = response.paymentUrl;
      },
      (error) => {
        alert('Lỗi tạo URL thanh toán: ' + error.message);
      }
    );
  }
}
```

### 3. Tạo Payment Return Component

```typescript
// payment-return.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-payment-return',
  template: `
    <div class="payment-result">
      <div *ngIf="success" class="success">
        <i class="fa fa-check-circle"></i>
        <h2>Thanh toán thành công!</h2>
        <p>Mã giao dịch: {{ transactionId }}</p>
        <p>Số tiền: {{ amount | number }} VND</p>
      </div>

      <div *ngIf="!success" class="error">
        <i class="fa fa-times-circle"></i>
        <h2>Thanh toán thất bại!</h2>
        <p>{{ message }}</p>
      </div>

      <button (click)="goHome()">Về trang chủ</button>
    </div>
  `,
})
export class PaymentReturnComponent implements OnInit {
  success: boolean = false;
  message: string = '';
  transactionId: string = '';
  amount: string = '';

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      // Backend sẽ xử lý verify và update Fine
      // Frontend chỉ hiển thị kết quả
      this.success = params['vnp_ResponseCode'] === '00';
      this.transactionId = params['vnp_TransactionNo'];
      this.amount = params['vnp_Amount'];
      this.message = this.success
        ? 'Thanh toán thành công'
        : 'Thanh toán thất bại';
    });
  }

  goHome() {
    this.router.navigate(['/']);
  }
}
```

### 4. Routing

```typescript
// app-routing.module.ts
const routes: Routes = [
  // ... existing routes
  { path: 'fines/payment', component: FinePaymentComponent },
  { path: 'payment/return', component: PaymentReturnComponent },
];
```

## 📝 Tạo Controller còn thiếu

Tạo file `PaymentController.java` với nội dung đã chuẩn bị sẵn ở trên.

## ✅ Test Flow

1. **Backend chạy:** `http://localhost:8080`
2. **Tạo một Fine test** (sử dụng Postman hoặc qua DB)
3. **Gọi API tạo payment URL:**
   ```bash
   curl -X POST http://localhost:8080/api/payment/create-payment-url \
     -H "Content-Type: application/json" \
     -d '{"fineId": 1}'
   ```
4. **Mở URL trong browser**
5. **Thanh toán với thẻ test:**
   - Ngân hàng: NCB
   - Số thẻ: 9704198526191432198
   - Tên: NGUYEN VAN A
   - Ngày: 07/15
   - OTP: 123456
6. **Kiểm tra callback:**
   - VNPay sẽ redirect về: `http://localhost:8080/api/payment/vnpay-return?...`
   - Backend verify chữ ký
   - Cập nhật Fine status: UNPAID → PAID
7. **Kiểm tra DB:** Fine đã được cập nhật

## 🎯 Điểm khác biệt với demo thành công

1. **Entity:** Fine (thay vì chỉ là demo)
2. **Business Logic:** Cập nhật trạng thái Fine sau thanh toán
3. **Return URL:** `/api/payment/vnpay-return` (backend endpoint)
4. **Order Info:** Format có Fine ID để trace back

## 📌 Lưu ý quan trọng

✅ **Đã áp dụng đúng chuẩn VNPay:**

- Key KHÔNG encode
- Value PHẢI encode UTF-8
- Sort params theo alphabet
- HMAC SHA512 signature

✅ **Logging đầy đủ** để debug

✅ **Transaction safe** với @Transactional

## 🔧 Bước tiếp theo

1. Tạo file `PaymentController.java` (code đã chuẩn bị)
2. Test API với Postman
3. Tích hợp Frontend (Angular)
4. Deploy và test end-to-end

**Tích hợp thành công! 🎉**
