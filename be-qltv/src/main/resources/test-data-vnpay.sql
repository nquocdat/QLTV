-- =====================================================
-- VNPay Payment Test Data for QLTV
-- =====================================================

-- Xóa dữ liệu test cũ (nếu có)
DELETE FROM fines WHERE id IN (1, 2, 3);
DELETE FROM loans WHERE id IN (1, 2, 3);
DELETE FROM books WHERE id IN (1, 2, 3);
DELETE FROM patrons WHERE id IN (1, 2, 3);

-- =====================================================
-- 1. Tạo Test Patrons
-- =====================================================
INSERT INTO patrons (id, name, email, phone, address, status, membership_date, created_date, updated_date) 
VALUES 
(1, 'Nguyễn Văn A', 'nguyenvana@example.com', '0901234567', '123 Nguyễn Huệ, Q1, TP.HCM', 'ACTIVE', '2024-01-01', NOW(), NOW()),
(2, 'Trần Thị B', 'tranthib@example.com', '0902345678', '456 Lê Lợi, Q1, TP.HCM', 'ACTIVE', '2024-01-15', NOW(), NOW()),
(3, 'Phạm Văn C', 'phamvanc@example.com', '0903456789', '789 Hai Bà Trưng, Q3, TP.HCM', 'ACTIVE', '2024-02-01', NOW(), NOW());

-- =====================================================
-- 2. Tạo Test Books
-- =====================================================
INSERT INTO books (id, title, isbn, author, publisher, publication_year, category, status, total_copies, available_copies, created_date, updated_date) 
VALUES 
(1, 'Clean Code', '978-0132350884', 'Robert C. Martin', 'Prentice Hall', 2008, 'Programming', 'AVAILABLE', 5, 4, NOW(), NOW()),
(2, 'Design Patterns', '978-0201633612', 'Gang of Four', 'Addison-Wesley', 1994, 'Programming', 'AVAILABLE', 3, 2, NOW(), NOW()),
(3, 'Refactoring', '978-0134757599', 'Martin Fowler', 'Addison-Wesley', 2018, 'Programming', 'AVAILABLE', 4, 3, NOW(), NOW());

-- =====================================================
-- 3. Tạo Test Loans
-- =====================================================
INSERT INTO loans (id, patron_id, book_id, loan_date, due_date, return_date, status, created_date, updated_date) 
VALUES 
-- Loan 1: Đã trả nhưng quá hạn (có phí phạt)
(1, 1, 1, '2024-12-01', '2024-12-15', '2024-12-20', 'RETURNED', NOW(), NOW()),
-- Loan 2: Đã trả nhưng quá hạn (có phí phạt)
(2, 2, 2, '2024-12-10', '2024-12-24', '2024-12-28', 'RETURNED', NOW(), NOW()),
-- Loan 3: Đã trả đúng hạn (không phí phạt)
(3, 3, 3, '2025-01-01', '2025-01-15', '2025-01-14', 'RETURNED', NOW(), NOW());

-- =====================================================
-- 4. Tạo Test Fines (UNPAID)
-- =====================================================
INSERT INTO fines (id, loan_id, patron_id, amount, reason, status, created_date, updated_date) 
VALUES 
-- Fine 1: Trả sách quá hạn 5 ngày (5000 đ/ngày)
(1, 1, 1, 25000, 'Trả sách quá hạn 5 ngày', 'UNPAID', NOW(), NOW()),
-- Fine 2: Trả sách quá hạn 4 ngày
(2, 2, 2, 20000, 'Trả sách quá hạn 4 ngày', 'UNPAID', NOW(), NOW()),
-- Fine 3: Làm hỏng sách
(3, 1, 1, 50000, 'Làm hỏng bìa sách', 'UNPAID', NOW(), NOW());

-- =====================================================
-- 5. Verify Test Data
-- =====================================================
SELECT 'Patrons:' as table_name, COUNT(*) as count FROM patrons WHERE id IN (1,2,3)
UNION ALL
SELECT 'Books:', COUNT(*) FROM books WHERE id IN (1,2,3)
UNION ALL
SELECT 'Loans:', COUNT(*) FROM loans WHERE id IN (1,2,3)
UNION ALL
SELECT 'Fines:', COUNT(*) FROM fines WHERE id IN (1,2,3);

-- =====================================================
-- 6. Query Test Data
-- =====================================================
-- Xem tất cả fines chưa thanh toán
SELECT 
    f.id,
    f.amount,
    f.reason,
    f.status,
    p.name as patron_name,
    p.email as patron_email,
    l.loan_date,
    l.due_date,
    l.return_date
FROM fines f
JOIN patrons p ON f.patron_id = p.id
JOIN loans l ON f.loan_id = l.id
WHERE f.status = 'UNPAID'
ORDER BY f.id;

-- =====================================================
-- 7. Test Queries
-- =====================================================
-- Query để test API endpoint: /api/payment/unpaid-fines/{patronId}
SELECT * FROM fines WHERE patron_id = 1 AND status = 'UNPAID';

-- Tổng tiền phí phạt chưa thanh toán của patron
SELECT patron_id, SUM(amount) as total_unpaid
FROM fines 
WHERE status = 'UNPAID'
GROUP BY patron_id;

-- =====================================================
-- NOTES FOR TESTING
-- =====================================================
-- Test Case 1: Patron ID = 1
--   - Expected: 2 unpaid fines (25000 + 50000 = 75000 VND)
--   
-- Test Case 2: Patron ID = 2
--   - Expected: 1 unpaid fine (20000 VND)
--   
-- Test Case 3: Patron ID = 3
--   - Expected: 0 unpaid fines
--
-- VNPay Test Card:
--   - Bank: NCB
--   - Card: 9704198526191432198
--   - Name: NGUYEN VAN A
--   - Expiry: 07/15
--   - OTP: 123456
-- =====================================================
