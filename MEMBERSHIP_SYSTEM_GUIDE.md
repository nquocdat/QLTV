# 📚 HƯỚNG DẪN TRIỂN KHAI MEMBERSHIP SYSTEM

## ✅ ĐÃ HOÀN THÀNH

### Backend (Java Spring Boot)

1. **Entities**

   - ✅ `MembershipTier` - Định nghĩa các hạng thành viên
   - ✅ `UserMembership` - Lưu thông tin thành viên của user

2. **DTOs**

   - ✅ `MembershipTierDTO` - Transfer object cho tier
   - ✅ `UserMembershipDTO` - Transfer object cho user membership

3. **Repositories**

   - ✅ `MembershipTierRepository` - Query membership tiers
   - ✅ `UserMembershipRepository` - Query user memberships

4. **Service**

   - ✅ `MembershipService` - Logic nghiệp vụ đầy đủ
     - Quản lý membership tiers
     - Quản lý user memberships
     - Cộng điểm tự động
     - Nâng hạng tự động
     - Xuống hạng khi vi phạm
     - Tính toán progress

5. **Controller**

   - ✅ `MembershipController` - RESTful API endpoints
     - GET /api/membership/tiers - Lấy danh sách tiers
     - GET /api/membership/users/{userId} - Lấy membership của user
     - POST /api/membership/users/{userId}/upgrade - Nâng hạng
     - POST /api/membership/users/{userId}/points - Cộng điểm
     - POST /api/membership/users/{userId}/increment-loan - Tăng số lần mượn
     - POST /api/membership/users/{userId}/increment-violation - Tăng vi phạm

6. **Integration**
   - ✅ Tích hợp vào `LoanService`
     - Tự động cộng điểm khi mượn sách (+5 điểm)
     - Tự động cộng điểm khi trả đúng hạn (+10 điểm)
     - Tự động tăng vi phạm khi trả muộn
     - Tự động kiểm tra và nâng/xuống hạng

### Frontend (Angular)

1. **Models**

   - ✅ Cập nhật `UserMembership` interface với fields mới
   - ✅ Thêm support cho tier details

2. **Services**

   - ✅ `AdvancedFeaturesService` - Kết nối API thực
     - getMembershipTiers() - Gọi backend API
     - getUserMembership() - Gọi backend API
     - updateMembership() - Gọi backend API
     - upgradeMembership() - Gọi backend API
     - addPoints() - Gọi backend API

3. **Components**
   - ✅ `Home` component - Load membership data từ API
   - ✅ `MembershipManagement` component - Quản lý admin

## 🚀 CÁCH SỬ DỤNG

### 0. Setup Database (Bắt buộc lần đầu)

#### Cách 1: Chạy từ MySQL Command Line

```sql
-- 1. Kết nối MySQL
mysql -u root -p

-- 2. Tạo database nếu chưa có
CREATE DATABASE IF NOT EXISTS qltv_db;
USE qltv_db;

-- 3. Chạy schema (tạo tables)
SOURCE d:/java/QLTV/be-qltv/src/main/resources/schema.sql;

-- 4. Chạy data (insert dữ liệu mẫu)
SOURCE d:/java/QLTV/be-qltv/src/main/resources/data.sql;

-- 5. Kiểm tra dữ liệu
SELECT * FROM membership_tiers;
SELECT * FROM user_memberships;
```

#### Cách 2: Reset Membership Data (Nếu gặp lỗi)

```sql
mysql -u root -p
USE qltv_db;
SOURCE d:/java/QLTV/reset-membership-data.sql;
```

Script `reset-membership-data.sql` sẽ:

- Xóa tất cả user memberships cũ
- Tạo lại membership tiers nếu chưa có
- Tự động tạo BASIC membership cho tất cả users
- Nâng admin users lên PREMIUM
- Hiển thị thống kê membership

### 1. Khởi động Backend

```bash
cd be-qltv
mvn clean install
mvn spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8080`

### 2. Khởi động Frontend

```bash
cd fe-qltv
npm install
npm start
```

Frontend sẽ chạy tại: `http://localhost:4200`

### 3. Test API với Postman

#### Get All Membership Tiers

```
GET http://localhost:8080/api/membership/tiers
```

#### Get User Membership

```
GET http://localhost:8080/api/membership/users/1
```

#### Upgrade User Membership

```
POST http://localhost:8080/api/membership/users/1/upgrade
Content-Type: application/json

{
  "tierId": 2
}
```

#### Add Points

```
POST http://localhost:8080/api/membership/users/1/points
Content-Type: application/json

{
  "points": 50
}
```

## 📊 LOGIC TỰ ĐỘNG

### 1. Khi User Mượn Sách

- ✅ Tăng `totalLoans` +1
- ✅ Cộng điểm +5
- ✅ Kiểm tra điều kiện nâng hạng

### 2. Khi User Trả Sách Đúng Hạn

- ✅ Cộng điểm +10
- ✅ Kiểm tra điều kiện nâng hạng

### 3. Khi User Trả Sách Muộn

- ✅ Tăng `violationCount` +1
- ✅ Kiểm tra nếu vượt quá giới hạn → Xuống hạng về BASIC

### 4. Điều Kiện Nâng Hạng Tự Động

**BASIC → VIP:**

- Tổng số lần mượn ≥ 20
- Điểm tích lũy ≥ 100
- Vi phạm ≤ 2

**VIP → PREMIUM:**

- Tổng số lần mượn ≥ 50
- Điểm tích lũy ≥ 300
- Vi phạm ≤ 1

## 🎯 CÁC QUYỀN LỢI THEO HẠNG

### BASIC (Cơ bản)

- Mượn tối đa: **3 cuốn**
- Thời gian mượn: **14 ngày**
- Giảm phí phạt: **0%**

### VIP

- Mượn tối đa: **5 cuốn**
- Thời gian mượn: **21 ngày**
- Giảm phí phạt: **20%**
- Ưu tiên đặt trước sách

### PREMIUM

- Mượn tối đa: **10 cuốn**
- Thời gian mượn: **30 ngày**
- Giảm phí phạt: **50%**
- Ưu tiên đặt trước sách
- Truy cập sớm sách mới

## 🔧 KIỂM TRA HỆ THỐNG

### 1. Kiểm tra Database

```sql
-- Xem các membership tiers
SELECT * FROM membership_tiers;

-- Xem user memberships
SELECT * FROM user_memberships;

-- Xem membership của user cụ thể
SELECT um.*, mt.name as tier_name, p.name as patron_name
FROM user_memberships um
JOIN membership_tiers mt ON um.tier_id = mt.id
JOIN patrons p ON um.patron_id = p.id
WHERE um.patron_id = 1;
```

### 2. Kiểm tra Frontend

1. Đăng nhập vào hệ thống
2. Vào trang Home - Xem phần "Thông tin thành viên"
3. Kiểm tra:
   - Hạng thành viên hiện tại
   - Điểm tích lũy
   - Số sách đã mượn
   - Thanh tiến độ nâng hạng

### 3. Test Auto-Upgrade

1. Tạo user mới (sẽ có hạng BASIC mặc định)
2. Mượn sách 20 lần (tích lũy 20 \* 5 = 100 điểm)
3. Trả đúng hạn để được +10 điểm mỗi lần
4. Sau khi đủ điều kiện, hệ thống tự động nâng lên VIP

## 📝 LƯU Ý

1. **Database Schema**: Bảng `membership_tiers` và `user_memberships` phải được tạo (đã có trong `schema.sql`)

2. **Dữ liệu mẫu**: File `data.sql` đã có sẵn:

   - 3 membership tiers (BASIC, VIP, PREMIUM)
   - 7 user memberships cho tất cả users (admin, test user, librarian, regular users)
   - Hệ thống tự động tạo BASIC membership cho user mới nếu chưa có

3. **Auto-Create Membership**: Backend tự động tạo BASIC membership khi user chưa có membership

4. **API URL**: Frontend gọi API tại `http://localhost:8080/api/membership` (đổi thành 8081 nếu backend chạy port 8081)

5. **Error Handling**:

   - Frontend có fallback khi API lỗi (sử dụng default membership)
   - Backend có try-catch và logging chi tiết

6. **Performance**: Service đã tối ưu với EAGER/LAZY loading cho relationships

7. **No Mock Data**: Frontend đã xóa tất cả mock data, chỉ sử dụng data từ backend API

## 🐛 XỬ LÝ LỖI

### Nếu gặp lỗi "Table doesn't exist"

```sql
-- Chạy lại schema
SOURCE be-qltv/src/main/resources/schema.sql;
-- Sau đó chạy lại data
SOURCE be-qltv/src/main/resources/data.sql;
```

### Nếu gặp lỗi 500 khi call API membership

**Nguyên nhân**: User chưa có membership trong database

**Giải pháp**: Hệ thống sẽ tự động tạo membership BASIC cho user khi lần đầu truy cập. Hoặc bạn có thể:

```sql
-- Thêm membership thủ công cho user (thay USER_ID bằng ID thực tế)
INSERT INTO user_memberships (patron_id, tier_id, current_points, total_loans, violation_count, join_date)
VALUES (USER_ID, 1, 0, 0, 0, CURDATE());
```

### Nếu gặp lỗi CORS

- Kiểm tra `@CrossOrigin` trong Controller
- URL frontend phải là `http://localhost:4200`
- Backend phải chạy tại `http://localhost:8080` (hoặc 8081)

### Nếu frontend không load data

- Mở Console (F12) để xem lỗi API
- Kiểm tra backend có đang chạy không
- Kiểm tra URL API trong service (mặc định: http://localhost:8080)
- Kiểm tra data.sql đã chạy chưa (phải có membership_tiers và user_memberships)

## 🎉 KẾT LUẬN

Hệ thống membership đã được triển khai đầy đủ với:

- ✅ Backend API hoàn chỉnh
- ✅ Frontend integration
- ✅ Auto-upgrade logic
- ✅ Points system
- ✅ Violation tracking
- ✅ Progress calculation

Hệ thống sẵn sàng sử dụng và có thể mở rộng thêm tính năng!
