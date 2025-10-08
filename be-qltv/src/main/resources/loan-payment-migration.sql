-- ================================
-- LOAN PAYMENT SYSTEM - DATABASE MIGRATION
-- Thêm chức năng thanh toán khi mượn sách
-- ================================

-- 1. Tạo bảng loan_payments
CREATE TABLE IF NOT EXISTS `loan_payments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `loan_id` BIGINT NOT NULL,
  `patron_id` BIGINT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL COMMENT 'Số tiền thanh toán',
  `payment_method` ENUM('CASH', 'VNPAY') NOT NULL COMMENT 'Phương thức thanh toán',
  `payment_status` ENUM('PENDING', 'CONFIRMED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái thanh toán',
  `transaction_no` VARCHAR(255) NULL COMMENT 'Mã giao dịch VNPay',
  `bank_code` VARCHAR(50) NULL COMMENT 'Mã ngân hàng',
  `vnpay_response_code` VARCHAR(10) NULL COMMENT 'Mã phản hồi VNPay',
  `description` VARCHAR(500) NULL COMMENT 'Mô tả giao dịch',
  `created_date` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Ngày tạo',
  `confirmed_date` DATETIME(6) NULL COMMENT 'Ngày xác nhận',
  `confirmed_by` BIGINT NULL COMMENT 'ID Admin/Librarian xác nhận (chỉ cho Cash)',
  `updated_date` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Ngày cập nhật',
  
  PRIMARY KEY (`id`),
  FOREIGN KEY (`loan_id`) REFERENCES `loans`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`patron_id`) REFERENCES `patrons`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`confirmed_by`) REFERENCES `patrons`(`id`) ON DELETE SET NULL,
  
  INDEX `idx_loan_id` (`loan_id`),
  INDEX `idx_patron_id` (`patron_id`),
  INDEX `idx_payment_status` (`payment_status`),
  INDEX `idx_payment_method` (`payment_method`),
  INDEX `idx_created_date` (`created_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Bảng lưu thông tin thanh toán khi mượn sách';

-- 2. Cập nhật bảng loans - thêm status mới
-- Backup old status values first
-- ALTER TABLE `loans` 
-- ADD COLUMN `old_status` VARCHAR(50) NULL AFTER `status`;

-- Update column definition
ALTER TABLE `loans` 
MODIFY COLUMN `status` ENUM(
  'PENDING_PAYMENT',    -- Chờ thanh toán
  'BORROWED',           -- Đã mượn (sau khi thanh toán thành công)
  'OVERDUE',            -- Quá hạn
  'RENEWED',            -- Đã gia hạn
  'RETURNED',           -- Đã trả
  'PENDING_RETURN'      -- Chờ xác nhận trả
) NOT NULL DEFAULT 'PENDING_PAYMENT';

-- 3. Thêm cấu hình phí đặt cọc vào bảng mới (tùy chọn)
CREATE TABLE IF NOT EXISTS `system_settings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `setting_key` VARCHAR(100) NOT NULL UNIQUE,
  `setting_value` VARCHAR(500) NOT NULL,
  `description` VARCHAR(500) NULL,
  `created_date` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_date` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Insert default settings
INSERT INTO `system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('loan_deposit_amount', '50000', 'Phí đặt cọc khi mượn sách (VND)'),
('loan_duration_days', '14', 'Số ngày mượn sách mặc định'),
('fine_per_day', '5000', 'Phí phạt mỗi ngày quá hạn (VND)')
ON DUPLICATE KEY UPDATE `setting_value` = VALUES(`setting_value`);

-- 4. Verify tables created
SELECT 
  TABLE_NAME, 
  ENGINE, 
  TABLE_ROWS, 
  CREATE_TIME
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'qltv_db' 
  AND TABLE_NAME IN ('loan_payments', 'system_settings');

-- 5. Show loan_payments structure
DESCRIBE loan_payments;

-- 6. Show loans status enum values
SHOW COLUMNS FROM loans LIKE 'status';

-- ================================
-- SAMPLE DATA (for testing)
-- ================================

-- Test data sẽ được tạo khi có loan thật qua API
-- Không insert trực tiếp vào đây

-- ================================
-- ROLLBACK SCRIPT (if needed)
-- ================================

/*
-- To rollback these changes:

-- Drop new tables
DROP TABLE IF EXISTS `loan_payments`;
DROP TABLE IF EXISTS `system_settings`;

-- Restore old loans status enum
ALTER TABLE `loans` 
MODIFY COLUMN `status` ENUM(
  'BORROWED',
  'OVERDUE',
  'RENEWED',
  'RETURNED',
  'PENDING_RETURN'
) NOT NULL;

*/

-- ================================
-- VERIFICATION QUERIES
-- ================================

-- Check loan_payments table
SELECT COUNT(*) as total_payments FROM loan_payments;

-- Check pending cash payments
SELECT 
  lp.*,
  p.name as patron_name,
  l.loan_date
FROM loan_payments lp
JOIN patrons p ON lp.patron_id = p.id
JOIN loans l ON lp.loan_id = l.id
WHERE lp.payment_method = 'CASH' 
  AND lp.payment_status = 'PENDING'
ORDER BY lp.created_date DESC;

-- Check VNPay payments
SELECT 
  lp.*,
  p.name as patron_name
FROM loan_payments lp
JOIN patrons p ON lp.patron_id = p.id
WHERE lp.payment_method = 'VNPAY'
ORDER BY lp.created_date DESC;

-- Check loans waiting for payment
SELECT 
  l.*,
  p.name as patron_name,
  b.title as book_title
FROM loans l
JOIN patrons p ON l.patron_id = p.id
JOIN books b ON l.book_id = b.id
WHERE l.status = 'PENDING_PAYMENT'
ORDER BY l.created_date DESC;

COMMIT;
