# 📚 BOOK COPIES SYSTEM - HƯỚNG DẪN TRIỂN KHAI HOÀN CHỈNH

**Ngày tạo**: October 6, 2025  
**Phiên bản**: 1.0  
**Tác giả**: GitHub Copilot

---

## 🎯 TỔNG QUAN

Hệ thống Book Copies cho phép:

- ✅ **Nhiều người mượn cùng lúc** - Mỗi Book có nhiều BookCopy vật lý
- ✅ **Quản lý chặt chẽ** - Track từng copy: barcode, location, condition, status
- ✅ **Giống thư viện thực tế** - Đúng với cách thư viện hoạt động

### TRƯỚC (Old System):

```
Book #1 "Harry Potter"
├─ total_copies: 5
├─ available_copies: 5
└─ ❌ Chỉ 1 người mượn được → available_copies giảm xuống 4
```

### SAU (New System):

```
Book #1 "Harry Potter"
├─ BookCopy #1 (Barcode: HP001) → BORROWED by User A
├─ BookCopy #2 (Barcode: HP002) → BORROWED by User B
├─ BookCopy #3 (Barcode: HP003) → AVAILABLE
├─ BookCopy #4 (Barcode: HP004) → RESERVED for User C
└─ BookCopy #5 (Barcode: HP005) → REPAIRING
✅ 2 người đang mượn đồng thời!
```

---

## 📦 FILES ĐÃ TẠO

### 1. Database Migration

- `book-copies-migration.sql` (550 dòng)
  - Tạo bảng `book_copies`
  - Migrate dữ liệu từ `books.total_copies`
  - Thêm `book_copy_id` vào `loans`
  - Triggers tự động cập nhật status
  - Views & Functions helper

### 2. Backend - Entity & DTO

- `BookCopy.java` (260 dòng)
- `BookCopyDTO.java` (210 dòng)

### 3. Backend - Repository & Service

- `BookCopyRepository.java` (90 dòng)
- `BookCopyService.java` (290 dòng)

### 4. Backend - Controller

- `BookCopyController.java` (210 dòng)

### 5. Backend - Updates

- `Loan.java` - Thêm field `bookCopy`
- `LoanService.java` - Dùng BookCopy khi tạo loan
- `LoanPaymentService.java` - Cập nhật copy status khi thanh toán

**TỔNG**: 7 files mới + 3 files updated

---

## 🗄️ DATABASE SCHEMA

### Bảng `book_copies`

```sql
CREATE TABLE book_copies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    copy_number INT NOT NULL,
    barcode VARCHAR(50) UNIQUE NOT NULL,
    condition_status ENUM('NEW', 'GOOD', 'FAIR', 'POOR', 'DAMAGED'),
    status ENUM('AVAILABLE', 'BORROWED', 'RESERVED', 'LOST', 'REPAIRING'),
    location VARCHAR(100) DEFAULT 'Kho chính',
    acquisition_date DATE,
    price DECIMAL(10, 2),
    notes TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id),
    UNIQUE (book_id, copy_number)
);
```

### Bảng `loans` (Updated)

```sql
ALTER TABLE loans
ADD COLUMN book_copy_id BIGINT,
ADD FOREIGN KEY (book_copy_id) REFERENCES book_copies(id);
```

---

## 🚀 TRIỂN KHAI

### BƯỚC 1: Chạy Migration

```bash
# Backup database trước
mysqldump -u root -p qltv_db > backup_before_book_copies.sql

# Chạy migration
mysql -u root -p qltv_db < book-copies-migration.sql
```

**Kết quả**:

- Tạo bảng `book_copies` ✅
- Tự động tạo copies từ `total_copies` của mỗi book ✅
- Migrate loans hiện tại sang `book_copy_id` ✅
- Tạo triggers để tự động sync status ✅

**Kiểm tra**:

```sql
-- Xem copies đã tạo
SELECT b.title, COUNT(bc.id) as copies_created
FROM books b
LEFT JOIN book_copies bc ON b.id = bc.book_id
GROUP BY b.id
LIMIT 10;

-- Xem loans đã migrate
SELECT l.id, bc.barcode, bc.copy_number, bc.status
FROM loans l
INNER JOIN book_copies bc ON l.book_copy_id = bc.id
LIMIT 10;
```

### BƯỚC 2: Compile Backend

```bash
cd be-qltv
mvnw clean install
```

**Files mới**:

- `entity/BookCopy.java`
- `dto/BookCopyDTO.java`
- `repository/BookCopyRepository.java`
- `service/BookCopyService.java`
- `controller/BookCopyController.java`

**Files updated**:

- `entity/Loan.java` - Field `bookCopy`
- `service/LoanService.java` - Dùng `BookCopyService`
- `service/LoanPaymentService.java` - Update copy status

### BƯỚC 3: Test Backend APIs

#### 3.1. Lấy copies của một book

```http
GET http://localhost:8080/api/book-copies/book/1
```

**Response**:

```json
[
  {
    "id": 1,
    "bookId": 1,
    "bookTitle": "Harry Potter",
    "copyNumber": 1,
    "barcode": "HP001-C001",
    "status": "AVAILABLE",
    "statusDisplay": "Có sẵn",
    "conditionStatus": "GOOD",
    "conditionStatusDisplay": "Tốt",
    "location": "Kho chính"
  },
  ...
]
```

#### 3.2. Đếm copies available

```http
GET http://localhost:8080/api/book-copies/book/1/available/count
```

**Response**:

```json
{
  "availableCount": 3
}
```

#### 3.3. Tạo copy mới (Admin/Librarian)

```http
POST http://localhost:8080/api/book-copies/book/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "location": "Kệ A-12",
  "price": 150000,
  "conditionStatus": "NEW"
}
```

#### 3.4. Tạo nhiều copies (Bulk)

```http
POST http://localhost:8080/api/book-copies/book/1/bulk
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantity": 5,
  "location": "Kệ B-03",
  "price": 150000
}
```

#### 3.5. Tìm copy theo barcode

```http
GET http://localhost:8080/api/book-copies/barcode/HP001-C001
```

#### 3.6. Lấy copies cần bảo trì (Admin)

```http
GET http://localhost:8080/api/book-copies/maintenance
Authorization: Bearer <token>
```

---

## 🔄 WORKFLOW MỚI

### Khi User Mượn Sách:

**Trước**:

```
1. User click "Mượn sách"
2. Check book.availableCopies > 0
3. Create Loan (book_id)
4. Giảm book.availableCopies--
```

**Sau** (với Book Copies):

```
1. User click "Mượn sách"
2. Tìm copy available đầu tiên
   → BookCopyService.getFirstAvailableCopy(bookId)
3. Create Loan với book_copy_id cụ thể
4. Chọn payment method (CASH/VNPAY)
5. Create LoanPayment (PENDING)
6. Khi payment confirmed:
   → Update copy.status = BORROWED
   → Update loan.status = BORROWED
   → Trigger tự động giảm book.available_copies
```

### Code Example (LoanService.java):

```java
public LoanDTO borrowBookWithPayment(Long bookId, Long patronId, String paymentMethod) {
    // 1. Check available copies
    Long availableCount = bookCopyService.countAvailableCopies(bookId);
    if (availableCount <= 0) {
        throw new RuntimeException("No available copy");
    }

    // 2. Lấy copy available đầu tiên
    BookCopy availableCopy = bookCopyService.getAvailableCopyEntity(bookId);

    // 3. Create loan với book_copy_id
    Loan loan = new Loan(book, patron);
    loan.setBookCopy(availableCopy); // ⭐ Gán copy cụ thể
    loan.setStatus(Loan.LoanStatus.PENDING_PAYMENT);

    // 4. Create payment...
}
```

### Code Example (LoanPaymentService.java):

```java
public LoanPaymentDTO confirmCashPayment(Long paymentId, Long confirmedById) {
    // ...

    // Cập nhật copy status
    if (loan.getBookCopy() != null) {
        bookCopyService.updateCopyStatus(
            loan.getBookCopy().getId(),
            BookCopy.CopyStatus.BORROWED
        ); // ⭐ Trigger sẽ tự động giảm book.available_copies
    }

    // ...
}
```

---

## 📊 DATABASE TRIGGERS (Tự động)

### Trigger 1: Insert Loan

```sql
CREATE TRIGGER update_copy_status_after_loan_insert
AFTER INSERT ON loans
FOR EACH ROW
BEGIN
    IF NEW.book_copy_id IS NOT NULL THEN
        UPDATE book_copies
        SET status = CASE
            WHEN NEW.status = 'BORROWED' THEN 'BORROWED'
            WHEN NEW.status = 'RESERVED' THEN 'RESERVED'
            ELSE status
        END
        WHERE id = NEW.book_copy_id;
    END IF;
END
```

### Trigger 2: Update Loan

```sql
CREATE TRIGGER update_copy_status_after_loan_update
AFTER UPDATE ON loans
FOR EACH ROW
BEGIN
    -- Khi trả sách: copy về AVAILABLE
    IF OLD.book_copy_id IS NOT NULL AND NEW.status = 'RETURNED' THEN
        UPDATE book_copies
        SET status = 'AVAILABLE'
        WHERE id = OLD.book_copy_id;
    END IF;

    -- Khi mượn: copy sang BORROWED
    IF NEW.book_copy_id IS NOT NULL THEN
        UPDATE book_copies
        SET status = CASE
            WHEN NEW.status = 'BORROWED' THEN 'BORROWED'
            WHEN NEW.status = 'RESERVED' THEN 'RESERVED'
            WHEN NEW.status = 'RETURNED' THEN 'AVAILABLE'
            ELSE status
        END
        WHERE id = NEW.book_copy_id;
    END IF;
END
```

**Lợi ích**: Không cần code Java để cập nhật status, database tự động sync! 🎯

---

## 🔧 HELPER FUNCTIONS

```sql
-- Đếm copies available
SELECT get_available_copies_count(1);  -- Book ID = 1

-- Lấy copy_id available đầu tiên
SELECT get_next_available_copy(1);
```

---

## 📋 API ENDPOINTS SUMMARY

| Method | Endpoint                                         | Description                 | Auth            |
| ------ | ------------------------------------------------ | --------------------------- | --------------- |
| GET    | `/api/book-copies/book/{bookId}`                 | Lấy tất cả copies của book  | Public          |
| GET    | `/api/book-copies/{id}`                          | Lấy 1 copy by ID            | Public          |
| GET    | `/api/book-copies/book/{bookId}/available`       | Lấy copy available đầu tiên | Public          |
| GET    | `/api/book-copies/book/{bookId}/available/count` | Đếm available copies        | Public          |
| GET    | `/api/book-copies/barcode/{barcode}`             | Tìm copy theo barcode       | Public          |
| POST   | `/api/book-copies/book/{bookId}`                 | Tạo copy mới                | Admin/Librarian |
| POST   | `/api/book-copies/book/{bookId}/bulk`            | Tạo nhiều copies            | Admin/Librarian |
| PUT    | `/api/book-copies/{id}`                          | Cập nhật copy               | Admin/Librarian |
| DELETE | `/api/book-copies/{id}`                          | Xóa copy                    | Admin only      |
| GET    | `/api/book-copies`                               | Lấy tất cả copies           | Admin/Librarian |
| GET    | `/api/book-copies/status/{status}`               | Lấy copies theo status      | Admin/Librarian |
| GET    | `/api/book-copies/maintenance`                   | Lấy copies cần sửa chữa     | Admin/Librarian |

---

## ✅ TESTING CHECKLIST

### Database

- [ ] Migration chạy thành công
- [ ] Book copies được tạo từ total_copies
- [ ] Loans có book_copy_id
- [ ] Triggers hoạt động

### Backend APIs

- [ ] GET copies by book ID
- [ ] GET available copy count
- [ ] POST create copy (Admin)
- [ ] POST bulk create copies (Admin)
- [ ] GET copy by barcode
- [ ] PUT update copy status
- [ ] GET maintenance copies

### Loan Flow

- [ ] Borrow book → Tự động gán copy
- [ ] Payment confirmed → Copy status = BORROWED
- [ ] Return book → Copy status = AVAILABLE
- [ ] Nhiều user mượn cùng book (khác copy)

---

## 🎨 FRONTEND (Tiếp theo)

Tôi sẽ tạo trong phần tiếp theo:

1. **BookCopyService** (Angular)
2. **BookCopiesListComponent** - Hiển thị danh sách copies
3. **BookCopyDetailComponent** - Chi tiết từng copy
4. **AdminBookCopiesComponent** - Quản lý copies (Admin)
5. **Update BookDetailModal** - Show available copies với barcode

---

## 🔄 ROLLBACK (Nếu Cần)

```sql
-- Xóa triggers
DROP TRIGGER IF EXISTS update_copy_status_after_loan_insert;
DROP TRIGGER IF EXISTS update_copy_status_after_loan_update;

-- Xóa functions
DROP FUNCTION IF EXISTS get_available_copies_count;
DROP FUNCTION IF EXISTS get_next_available_copy;

-- Xóa view
DROP VIEW IF EXISTS book_copies_full_info;

-- Xóa column book_copy_id
ALTER TABLE loans DROP FOREIGN KEY fk_loans_book_copy;
ALTER TABLE loans DROP COLUMN book_copy_id;

-- Xóa bảng
DROP TABLE IF EXISTS book_copies;

-- Restore backup
mysql -u root -p qltv_db < backup_before_book_copies.sql
```

---

## 📚 KẾT LUẬN

### Đã Hoàn Thành:

✅ Database schema & migration  
✅ Backend Entity, DTO, Repository  
✅ Backend Service layer  
✅ Backend Controller & APIs  
✅ Integration với LoanService  
✅ Payment flow với book copies  
✅ Triggers tự động sync status

### Tiếp Theo:

🔜 Frontend Angular services  
🔜 UI components  
🔜 Admin management pages

**Hệ thống sẵn sàng cho testing backend! 🚀**

Bạn có thể chạy migration và test APIs ngay bây giờ.
