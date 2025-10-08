# 📚 HỆ THỐNG ĐÁNH GIÁ SÁCH - TRIỂN KHAI ĐẦY ĐỦ

## ✅ ĐÃ HOÀN THÀNH - BACKEND

### 1. Database Schema

- ✅ **Entity Review.java** - Đã cập nhật:
  - Thêm trường `approved` (Boolean) - Admin phải duyệt
  - Thêm trường `loan_id` (FK) - Liên kết với lịch sử mượn

### 2. Repository Layer

- ✅ **ReviewRepository.java** - Đã cập nhật:

  - `findByApproved()` - Lọc review đã duyệt/chưa duyệt
  - `findApprovedByBookId()` - Lấy review đã duyệt của sách
  - `getApprovedAverageRatingForBook()` - Tính rating trung bình (chỉ review đã duyệt)
  - `findByLoanId()` - Kiểm tra xem loan đã có review chưa

- ✅ **LoanRepository.java** - Đã thêm:
  - `findByPatronIdAndBookId()` - Kiểm tra người dùng đã mượn sách chưa

### 3. Service Layer

- ✅ **ReviewService.java** - MỚI TẠO:
  - `canPatronReviewBook()` - Kiểm tra điều kiện đánh giá:
    - ✅ Phải đã mượn sách
    - ✅ Phải đã trả sách (status = RETURNED)
    - ✅ Chưa đánh giá sách này trước đó
  - `createReview()` - Tạo review (approved = false)
  - `approveReview()` - Admin duyệt review
  - `deleteReview()` - Xóa review
  - `getPendingReviews()` - Danh sách review chờ duyệt
  - `getApprovedReviewsForBook()` - Review đã duyệt của sách
  - `getBookRatingStats()` - Thống kê rating (số sao, số đánh giá)

### 4. Controller Layer

- ✅ **ReviewController.java** - MỚI TẠO:

  - **Public APIs:**

    - `GET /api/reviews/book/{bookId}` - Xem review sách (đã duyệt)
    - `GET /api/reviews/book/{bookId}/stats` - Thống kê rating sách

  - **User APIs:**

    - `GET /api/reviews/can-review/{bookId}` - Kiểm tra có thể đánh giá không
    - `POST /api/reviews` - Gửi đánh giá mới
    - `GET /api/reviews/my-reviews` - Xem đánh giá của mình
    - `GET /api/reviews/loan/{loanId}` - Xem review của loan cụ thể

  - **Admin APIs:**
    - `GET /api/reviews/admin/all` - Tất cả review
    - `GET /api/reviews/admin/pending` - Review chờ duyệt
    - `PUT /api/reviews/admin/{id}/approve` - Duyệt review
    - `DELETE /api/reviews/admin/{id}` - Xóa review

### 5. DTO

- ✅ **ReviewDTO.java** - MỚI TẠO:
  - Chứa thông tin đầy đủ: id, bookId, bookTitle, patronName, rating, comment, approved, etc.

### 6. SQL Migration

- ✅ **review-approval-migration.sql** - MỚI TẠO:
  - Thêm cột `approved` vào bảng `reviews`
  - Thêm cột `loan_id` và foreign key
  - Tạo indexes cho performance

---

## 🚀 CÁCH TRIỂN KHAI

### Bước 1: Chạy SQL Migration

```bash
# Vào MySQL và chạy file migration
mysql -u root -p qltv_db < be-qltv/src/main/resources/review-approval-migration.sql

# Hoặc qua MySQL Workbench/phpMyAdmin, copy paste SQL
```

### Bước 2: Restart Backend

```bash
cd be-qltv
mvn clean package
mvn spring-boot:run
```

### Bước 3: Test Backend APIs

#### 3.1. Kiểm tra có thể đánh giá sách không

```bash
GET http://localhost:8081/api/reviews/can-review/1
Authorization: Bearer <token>

# Response:
{
  "canReview": true  # hoặc false
}
```

#### 3.2. Tạo đánh giá mới

```bash
POST http://localhost:8081/api/reviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "bookId": 1,
  "rating": 5,
  "comment": "Sách rất hay, nội dung hấp dẫn!"
}

# Response:
{
  "success": true,
  "message": "Đánh giá của bạn đã được gửi và đang chờ duyệt",
  "review": {
    "id": 1,
    "approved": false,
    ...
  }
}
```

#### 3.3. Xem review chờ duyệt (Admin)

```bash
GET http://localhost:8081/api/reviews/admin/pending?page=0&size=10
Authorization: Bearer <admin_token>
```

#### 3.4. Duyệt review (Admin)

```bash
PUT http://localhost:8081/api/reviews/admin/1/approve
Authorization: Bearer <admin_token>

# Response:
{
  "success": true,
  "message": "Đánh giá đã được phê duyệt",
  "review": {
    "id": 1,
    "approved": true,
    ...
  }
}
```

#### 3.5. Lấy thống kê rating của sách

```bash
GET http://localhost:8081/api/reviews/book/1/stats

# Response:
{
  "averageRating": 4.5,
  "reviewCount": 10
}
```

---

## 📱 FRONTEND - NHỮNG GÌ CẦN LÀM

### 1. Sửa trang chủ (Home Page)

**YÊU CẦU:**

- ❌ Bỏ phần "Thống kê thư viện"
- ❌ Bỏ phần "Sách bạn đã mượn"
- ❌ Bỏ phần "Thông tin thành viên"
- ✅ Giữ lại: Banner tìm kiếm, danh sách sách

**FILE CẦN SỬA:**

- `fe-qltv/src/app/components/home/home.html`
- `fe-qltv/src/app/components/home/home.ts`

### 2. Hiển thị số sao trên thẻ sách

**YÊU CẦU:**

- ✅ Thêm component hiển thị rating stars (1-5 sao)
- ✅ Hiển thị trên: Book card, Book detail, Search results

**FILE CẦN TẠO:**

- `fe-qltv/src/app/components/shared/star-rating/star-rating.ts`
- `fe-qltv/src/app/components/shared/star-rating/star-rating.html`

**FILE CẦN SỬA:**

- `fe-qltv/src/app/components/book-catalog/book-catalog.html` - Thêm stars vào card
- `fe-qltv/src/app/components/book-detail/book-detail.html` - Hiển thị rating & reviews

### 3. Trang đánh giá sách

**YÊU CẦU:**

- ✅ Hiển thị trong lịch sử mượn trả sách
- ✅ Button "Đánh giá" chỉ hiện khi:
  - Loan status = RETURNED
  - Chưa đánh giá lần nào
- ✅ Modal đánh giá: Chọn sao (1-5), nhập comment

**FILE CẦN SỬA:**

- `fe-qltv/src/app/components/user/loan-history/loan-history.html`
- `fe-qltv/src/app/components/user/loan-history/loan-history.ts`

**FILE CẦN TẠO:**

- `fe-qltv/src/app/components/modals/review-modal/review-modal.ts`
- `fe-qltv/src/app/components/modals/review-modal/review-modal.html`

### 4. Trang quản lý đánh giá (Admin)

**YÊU CẦU:**

- ✅ Menu: "Quản lý đánh giá"
- ✅ Tabs: "Chờ duyệt" | "Đã duyệt" | "Tất cả"
- ✅ Actions: Duyệt, Xóa
- ✅ Hiển thị: Tên sách, người đánh giá, số sao, comment, ngày tạo

**FILE CẦN TẠO:**

- `fe-qltv/src/app/components/admin/review-management/review-management.ts`
- `fe-qltv/src/app/components/admin/review-management/review-management.html`
- `fe-qltv/src/app/components/admin/review-management/review-management.css`

**FILE CẦN SỬA:**

- `fe-qltv/src/app/app.routes.ts` - Thêm route
- `fe-qltv/src/app/components/layout/admin-layout/admin-layout.html` - Thêm menu item

### 5. Service Layer (Frontend)

**FILE CẦN TẠO:**

- `fe-qltv/src/app/services/review.service.ts`

**Nội dung:**

```typescript
@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private apiUrl = 'http://localhost:8081/api/reviews';

  canReview(bookId: number): Observable<{ canReview: boolean }> {
    return this.http.get<{ canReview: boolean }>(
      `${this.apiUrl}/can-review/${bookId}`
    );
  }

  createReview(data: { bookId: number; rating: number; comment: string }) {
    return this.http.post(`${this.apiUrl}`, data);
  }

  getBookReviews(bookId: number): Observable<ReviewDTO[]> {
    return this.http.get<ReviewDTO[]>(`${this.apiUrl}/book/${bookId}`);
  }

  getBookRatingStats(
    bookId: number
  ): Observable<{ averageRating: number; reviewCount: number }> {
    return this.http.get<any>(`${this.apiUrl}/book/${bookId}/stats`);
  }

  // Admin methods
  getPendingReviews(page: number, size: number): Observable<Page<ReviewDTO>> {
    return this.http.get<Page<ReviewDTO>>(
      `${this.apiUrl}/admin/pending?page=${page}&size=${size}`
    );
  }

  approveReview(reviewId: number) {
    return this.http.put(`${this.apiUrl}/admin/${reviewId}/approve`, {});
  }

  deleteReview(reviewId: number) {
    return this.http.delete(`${this.apiUrl}/admin/${reviewId}`);
  }
}
```

---

## 📝 CHI TIẾT TRIỂN KHAI FRONTEND

### A. Star Rating Component

**star-rating.ts:**

```typescript
@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex items-center gap-1">
      <svg
        *ngFor="let star of stars"
        [class]="star ? 'text-yellow-400' : 'text-gray-300'"
        class="w-5 h-5 fill-current"
        viewBox="0 0 24 24"
      >
        <path
          d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
        />
      </svg>
      <span class="text-sm text-gray-600 ml-1" *ngIf="showCount">
        ({{ reviewCount }})
      </span>
    </div>
  `,
})
export class StarRating {
  @Input() rating: number = 0;
  @Input() reviewCount: number = 0;
  @Input() showCount: boolean = true;

  get stars(): boolean[] {
    const fullStars = Math.floor(this.rating);
    return Array(5)
      .fill(false)
      .map((_, i) => i < fullStars);
  }
}
```

### B. Review Modal Component

**review-modal.html:**

```html
<div
  class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
>
  <div class="bg-white rounded-lg p-6 w-full max-w-md">
    <h3 class="text-xl font-bold mb-4">Đánh giá sách</h3>

    <div class="mb-4">
      <label class="block text-sm font-medium mb-2">Đánh giá của bạn</label>
      <div class="flex gap-2">
        <button
          *ngFor="let star of [1,2,3,4,5]"
          (click)="selectedRating = star"
          class="text-3xl transition-colors"
          [class.text-yellow-400]="star <= selectedRating"
          [class.text-gray-300]="star > selectedRating"
        >
          ★
        </button>
      </div>
    </div>

    <div class="mb-4">
      <label class="block text-sm font-medium mb-2">Nhận xét</label>
      <textarea
        [(ngModel)]="comment"
        rows="4"
        class="w-full border rounded-lg p-2"
        placeholder="Chia sẻ cảm nhận của bạn về cuốn sách..."
      >
      </textarea>
    </div>

    <div class="flex gap-2 justify-end">
      <button
        (click)="onCancel()"
        class="px-4 py-2 border rounded-lg hover:bg-gray-50"
      >
        Hủy
      </button>
      <button
        (click)="onSubmit()"
        [disabled]="selectedRating === 0"
        class="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50"
      >
        Gửi đánh giá
      </button>
    </div>
  </div>
</div>
```

### C. Loan History với Review Button

**Thêm vào loan-history.html:**

```html
<tr *ngFor="let loan of loans">
  <td>{{loan.book.title}}</td>
  <td>{{loan.loanDate | date}}</td>
  <td>{{loan.returnDate | date}}</td>
  <td>
    <span [class]="getStatusClass(loan.status)">
      {{getStatusText(loan.status)}}
    </span>
  </td>
  <td>
    <!-- Nút đánh giá -->
    <button
      *ngIf="loan.status === 'RETURNED' && !loan.hasReview"
      (click)="openReviewModal(loan)"
      class="px-3 py-1 bg-yellow-500 text-white rounded hover:bg-yellow-600"
    >
      ⭐ Đánh giá
    </button>

    <span *ngIf="loan.hasReview" class="text-green-600"> ✓ Đã đánh giá </span>
  </td>
</tr>
```

### D. Admin Review Management

**review-management.html:**

```html
<div class="p-6">
  <h2 class="text-2xl font-bold mb-6">Quản lý đánh giá</h2>

  <!-- Tabs -->
  <div class="flex gap-4 mb-6 border-b">
    <button
      (click)="activeTab = 'pending'"
      [class.border-b-2]="activeTab === 'pending'"
      [class.border-indigo-600]="activeTab === 'pending'"
      class="px-4 py-2"
    >
      Chờ duyệt ({{pendingCount}})
    </button>
    <button
      (click)="activeTab = 'approved'"
      [class.border-b-2]="activeTab === 'approved'"
      [class.border-indigo-600]="activeTab === 'approved'"
      class="px-4 py-2"
    >
      Đã duyệt
    </button>
    <button
      (click)="activeTab = 'all'"
      [class.border-b-2]="activeTab === 'all'"
      [class.border-indigo-600]="activeTab === 'all'"
      class="px-4 py-2"
    >
      Tất cả
    </button>
  </div>

  <!-- Review List -->
  <div class="space-y-4">
    <div
      *ngFor="let review of reviews"
      class="border rounded-lg p-4 hover:shadow-md transition"
    >
      <div class="flex justify-between items-start mb-2">
        <div>
          <h3 class="font-bold">{{review.bookTitle}}</h3>
          <p class="text-sm text-gray-600">
            Bởi {{review.patronName}} • {{review.createdDate | date:'dd/MM/yyyy
            HH:mm'}}
          </p>
        </div>
        <div class="flex items-center gap-1">
          <span
            *ngFor="let star of [1,2,3,4,5]"
            [class.text-yellow-400]="star <= review.rating"
            [class.text-gray-300]="star > review.rating"
          >
            ★
          </span>
        </div>
      </div>

      <p class="text-gray-700 mb-3">{{review.comment}}</p>

      <div class="flex gap-2">
        <button
          *ngIf="!review.approved"
          (click)="approveReview(review.id)"
          class="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700"
        >
          ✓ Duyệt
        </button>
        <button
          (click)="deleteReview(review.id)"
          class="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700"
        >
          ✗ Xóa
        </button>
      </div>
    </div>
  </div>
</div>
```

---

## 🎯 PRIORITY CHECKLIST

### Backend ✅ (ĐÃ XONG)

- [x] Review entity với approved & loan_id
- [x] ReviewRepository với approval methods
- [x] ReviewService với business logic
- [x] ReviewController với REST APIs
- [x] SQL migration script

### Frontend 📝 (CẦN LÀM)

**Priority 1 - CRITICAL:**

- [ ] Tạo ReviewService
- [ ] Tạo StarRating component
- [ ] Sửa Home page (bỏ phần không cần)
- [ ] Thêm stars vào Book cards

**Priority 2 - HIGH:**

- [ ] Tạo Review Modal
- [ ] Thêm Review button vào Loan History
- [ ] Hiển thị reviews trên Book Detail page

**Priority 3 - MEDIUM:**

- [ ] Tạo Admin Review Management page
- [ ] Thêm menu "Quản lý đánh giá"
- [ ] Add route cho review management

---

## 🚀 BƯỚC TIẾP THEO

1. **Chạy SQL migration** để update database
2. **Restart backend** để load các class mới
3. **Test các API** bằng Postman/curl
4. Tôi sẽ giúp bạn tạo từng component frontend theo priority

**Bạn muốn tôi bắt đầu với phần nào của Frontend?**

- A. ReviewService + StarRating component
- B. Sửa Home page
- C. Review Modal + Loan History
- D. Admin Review Management
