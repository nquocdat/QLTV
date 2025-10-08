# 📚 BOOK COPIES SYSTEM - TÓM TẮT

## ✨ ĐÃ HOÀN THÀNH

### 🗄️ Database (1 file)

- ✅ `book-copies-migration.sql` (550 dòng)
  - Tạo bảng `book_copies`
  - Auto-migrate từ `books.total_copies`
  - Triggers tự động sync status
  - Helper functions

### 🔧 Backend (7 files)

**Mới tạo** (5 files):

- ✅ `BookCopy.java` - Entity với enums (ConditionStatus, CopyStatus)
- ✅ `BookCopyDTO.java` - Data transfer object
- ✅ `BookCopyRepository.java` - JPA queries
- ✅ `BookCopyService.java` - Business logic
- ✅ `BookCopyController.java` - REST APIs (12 endpoints)

**Đã cập nhật** (3 files):

- ✅ `Loan.java` - Thêm field `bookCopy`
- ✅ `LoanService.java` - Dùng BookCopy khi borrowing
- ✅ `LoanPaymentService.java` - Update copy status khi payment confirmed

---

## 🎯 CHỨC NĂNG CHÍNH

### Trước (Old):

- 1 book chỉ cho 1 người mượn tại 1 thời điểm
- Không track được copy cụ thể nào đang ở đâu

### Sau (New):

- **Nhiều người mượn cùng lúc** - Mỗi book có nhiều physical copies
- **Track từng copy** - Barcode, location, condition, status
- **Tự động sync** - Database triggers cập nhật status

---

## 🚀 TRIỂN KHAI

### BƯỚC 1: Database

```bash
mysql -u root -p qltv_db < book-copies-migration.sql
```

### BƯỚC 2: Backend

```bash
cd be-qltv
mvnw clean install
mvnw spring-boot:run
```

### BƯỚC 3: Test API

```http
# Lấy copies của book
GET http://localhost:8080/api/book-copies/book/1

# Đếm available copies
GET http://localhost:8080/api/book-copies/book/1/available/count

# Tạo 5 copies mới (Admin)
POST http://localhost:8080/api/book-copies/book/1/bulk
{
  "quantity": 5,
  "location": "Kệ A-12",
  "price": 150000
}
```

---

## 📊 API ENDPOINTS (12 Total)

| Endpoint                                         | Method | Auth      | Description             |
| ------------------------------------------------ | ------ | --------- | ----------------------- |
| `/api/book-copies/book/{bookId}`                 | GET    | Public    | Lấy tất cả copies       |
| `/api/book-copies/{id}`                          | GET    | Public    | Chi tiết 1 copy         |
| `/api/book-copies/book/{bookId}/available`       | GET    | Public    | Copy available đầu tiên |
| `/api/book-copies/book/{bookId}/available/count` | GET    | Public    | Đếm available           |
| `/api/book-copies/barcode/{barcode}`             | GET    | Public    | Tìm theo barcode        |
| `/api/book-copies/book/{bookId}`                 | POST   | Admin/Lib | Tạo copy mới            |
| `/api/book-copies/book/{bookId}/bulk`            | POST   | Admin/Lib | Tạo nhiều copies        |
| `/api/book-copies/{id}`                          | PUT    | Admin/Lib | Cập nhật copy           |
| `/api/book-copies/{id}`                          | DELETE | Admin     | Xóa copy                |
| `/api/book-copies`                               | GET    | Admin/Lib | Tất cả copies           |
| `/api/book-copies/status/{status}`               | GET    | Admin/Lib | Theo status             |
| `/api/book-copies/maintenance`                   | GET    | Admin/Lib | Cần sửa chữa            |

---

## 🔄 WORKFLOW MỚI

### Mượn sách:

```
1. User chọn book
2. System tìm copy available đầu tiên
3. Tạo loan với book_copy_id cụ thể
4. User chọn payment (CASH/VNPAY)
5. Khi payment confirmed:
   → Copy status = BORROWED
   → Loan status = BORROWED
   → Book.available_copies tự động giảm (trigger)
```

### Trả sách:

```
1. Librarian xác nhận trả
2. Loan status = RETURNED
3. Trigger tự động:
   → Copy status = AVAILABLE
   → Book.available_copies tự động tăng
```

---

## 📁 FILE STRUCTURE

```
be-qltv/src/main/java/com/example/be_qltv/
├── entity/
│   ├── BookCopy.java ⭐ NEW
│   └── Loan.java (updated)
├── dto/
│   └── BookCopyDTO.java ⭐ NEW
├── repository/
│   └── BookCopyRepository.java ⭐ NEW
├── service/
│   ├── BookCopyService.java ⭐ NEW
│   ├── LoanService.java (updated)
│   └── LoanPaymentService.java (updated)
└── controller/
    └── BookCopyController.java ⭐ NEW
```

---

## ✅ TESTING CHECKLIST

### Database

- [ ] Migration thành công
- [ ] Copies được tạo từ total_copies
- [ ] Loans có book_copy_id
- [ ] Triggers hoạt động

### APIs

- [ ] GET copies by book ID
- [ ] GET available count
- [ ] POST create copy (Admin)
- [ ] POST bulk create
- [ ] GET by barcode

### Workflow

- [ ] Borrow → Gán copy tự động
- [ ] Payment → Copy BORROWED
- [ ] Return → Copy AVAILABLE
- [ ] 2 users mượn cùng book (khác copy) ✅

---

## 📝 VÍ DỤ DATA

### Book #1 "Harry Potter" với 5 copies:

```json
[
  {
    "id": 1,
    "copyNumber": 1,
    "barcode": "HP001-C001",
    "status": "BORROWED",
    "location": "Đang mượn - User A"
  },
  {
    "id": 2,
    "copyNumber": 2,
    "barcode": "HP001-C002",
    "status": "BORROWED",
    "location": "Đang mượn - User B"
  },
  {
    "id": 3,
    "copyNumber": 3,
    "barcode": "HP001-C003",
    "status": "AVAILABLE",
    "location": "Kệ A-12"
  },
  {
    "id": 4,
    "copyNumber": 4,
    "barcode": "HP001-C004",
    "status": "RESERVED",
    "location": "Đặt trước - User C"
  },
  {
    "id": 5,
    "copyNumber": 5,
    "barcode": "HP001-C005",
    "status": "REPAIRING",
    "location": "Phòng bảo trì"
  }
]
```

**Result**: 2 người đang mượn, 1 copy còn trống, 1 đã đặt, 1 đang sửa! 🎯

---

## 🎨 TIẾP THEO (Frontend)

### Cần tạo:

1. `BookCopyService` (Angular)
2. `BookCopiesListComponent` - Danh sách copies
3. `BookCopyCardComponent` - Card hiển thị copy
4. `AdminCopiesComponent` - Quản lý (Admin)
5. Update `BookDetailModal` - Show copies với barcode

### UI Preview:

```
┌─────────────────────────────────────────┐
│ Harry Potter - Chi tiết sách            │
├─────────────────────────────────────────┤
│ Tác giả: J.K. Rowling                   │
│ Tổng số: 5 bản | Còn lại: 2 bản        │
│                                         │
│ DANH SÁCH BẢN SAO:                      │
│ ┌───────────────────────────────────┐   │
│ │ Copy #1 | HP001-C001 | 🔴 BORROWED│   │
│ │ Location: Đang mượn - Nguyễn A     │   │
│ └───────────────────────────────────┘   │
│ ┌───────────────────────────────────┐   │
│ │ Copy #2 | HP001-C002 | 🔴 BORROWED│   │
│ │ Location: Đang mượn - Trần B       │   │
│ └───────────────────────────────────┘   │
│ ┌───────────────────────────────────┐   │
│ │ Copy #3 | HP001-C003 | 🟢 AVAILABLE│   │
│ │ Location: Kệ A-12                 │   │
│ │ [📚 Mượn copy này]                │   │
│ └───────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

---

## 🎉 STATUS

**Backend**: ✅ 100% HOÀN THÀNH  
**Frontend**: 🔜 Chưa bắt đầu  
**Testing**: ⏳ Cần test APIs

**Sẵn sàng triển khai backend ngay bây giờ! 🚀**

---

## 📖 TÀI LIỆU CHI TIẾT

Xem file: `BOOK_COPIES_IMPLEMENTATION_GUIDE.md` (Hướng dẫn đầy đủ 400+ dòng)
