-- Schema cho hệ thống quản lý thư viện

-- Tạo bảng patrons (người dùng)
CREATE TABLE IF NOT EXISTS patrons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    password VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tạo bảng books (sách)
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    publisher VARCHAR(255),
    published_date DATE,
    category VARCHAR(100),
    genre VARCHAR(100),
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    description TEXT,
    image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tạo bảng loans (mượn sách)
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patron_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    loan_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patron_id) REFERENCES patrons(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- Tạo bảng fines (phạt)
CREATE TABLE IF NOT EXISTS fines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    patron_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    reason VARCHAR(255),
    status VARCHAR(20) DEFAULT 'UNPAID',
    paid_date DATE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    FOREIGN KEY (patron_id) REFERENCES patrons(id) ON DELETE CASCADE
);

-- Tạo bảng reservations (đặt trước)
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patron_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    reservation_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patron_id) REFERENCES patrons(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- Tạo bảng notifications (thông báo)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patron_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patron_id) REFERENCES patrons(id) ON DELETE CASCADE
);

-- Tạo bảng reviews (đánh giá sách)
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    patron_id BIGINT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (patron_id) REFERENCES patrons(id) ON DELETE CASCADE,
    UNIQUE KEY unique_book_patron_review (book_id, patron_id)
);

-- Tạo bảng categories (danh mục sách)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng authors (tác giả)
CREATE TABLE IF NOT EXISTS authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    biography TEXT,
    birth_date DATE,
    nationality VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng book_authors (quan hệ nhiều-nhiều giữa sách và tác giả)
CREATE TABLE IF NOT EXISTS book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- Bảng membership_tiers (hạng thành viên)
CREATE TABLE IF NOT EXISTS membership_tiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    level ENUM('BASIC', 'VIP', 'PREMIUM') NOT NULL,
    max_books INT NOT NULL DEFAULT 3,
    loan_duration_days INT NOT NULL DEFAULT 14,
    late_fee_discount DECIMAL(5,2) DEFAULT 0.00,
    reservation_priority BOOLEAN DEFAULT FALSE,
    early_access BOOLEAN DEFAULT FALSE,
    min_loans_required INT DEFAULT 0,
    min_points_required INT DEFAULT 0,
    max_violations_allowed INT DEFAULT 5,
    color VARCHAR(50) DEFAULT '#6B7280',
    icon VARCHAR(50) DEFAULT 'user',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng user_memberships (thành viên của người dùng)
CREATE TABLE IF NOT EXISTS user_memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patron_id BIGINT NOT NULL,
    tier_id BIGINT NOT NULL,
    current_points INT DEFAULT 0,
    total_loans INT DEFAULT 0,
    violation_count INT DEFAULT 0,
    join_date DATE NOT NULL,
    upgrade_date DATE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patron_id) REFERENCES patrons(id) ON DELETE CASCADE,
    FOREIGN KEY (tier_id) REFERENCES membership_tiers(id) ON DELETE CASCADE,
    UNIQUE KEY unique_patron_membership (patron_id)
);

-- Bảng book_status_history (lịch sử trạng thái sách)
CREATE TABLE IF NOT EXISTS book_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    status ENUM('AVAILABLE', 'BORROWED', 'RESERVED', 'DAMAGED', 'LOST', 'MAINTENANCE') NOT NULL,
    previous_status ENUM('AVAILABLE', 'BORROWED', 'RESERVED', 'DAMAGED', 'LOST', 'MAINTENANCE'),
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by BIGINT,
    notes TEXT,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES patrons(id) ON DELETE SET NULL
);

-- Bảng user_violations (vi phạm của người dùng)
CREATE TABLE IF NOT EXISTS user_violations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patron_id BIGINT NOT NULL,
    type ENUM('LATE_RETURN', 'DAMAGE', 'LOST', 'OVERDUE_FINE', 'BEHAVIOR') NOT NULL,
    description TEXT NOT NULL,
    violation_date DATE NOT NULL,
    penalty_amount DECIMAL(10,2) DEFAULT 0.00,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_date DATE,
    resolved_by BIGINT,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patron_id) REFERENCES patrons(id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by) REFERENCES patrons(id) ON DELETE SET NULL
);

-- Bảng user_ratings (xếp loại độc giả)
CREATE TABLE IF NOT EXISTS user_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patron_id BIGINT NOT NULL,
    rating ENUM('EXCELLENT', 'GOOD', 'AVERAGE', 'POOR') NOT NULL,
    score INT NOT NULL CHECK (score >= 0 AND score <= 100),
    total_loans INT DEFAULT 0,
    on_time_returns INT DEFAULT 0,
    late_returns INT DEFAULT 0,
    violation_points INT DEFAULT 0,
    last_calculated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patron_id) REFERENCES patrons(id) ON DELETE CASCADE,
    UNIQUE KEY unique_patron_rating (patron_id)
);

-- Bảng email_notifications (thông báo email)
CREATE TABLE IF NOT EXISTS email_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patron_id BIGINT NOT NULL,
    type ENUM('REMINDER', 'DUE_TODAY', 'OVERDUE', 'BOOK_AVAILABLE', 'NEW_BOOK', 'MEMBERSHIP_UPDATE') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    email_sent BOOLEAN DEFAULT FALSE,
    sent_date TIMESTAMP NULL,
    scheduled_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    status ENUM('PENDING', 'SENT', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    error_message TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patron_id) REFERENCES patrons(id) ON DELETE CASCADE
);

-- Bảng book_statistics (thống kê sách)
CREATE TABLE IF NOT EXISTS book_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    total_loans INT DEFAULT 0,
    current_loans INT DEFAULT 0,
    total_reservations INT DEFAULT 0,
    damage_reports INT DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    popularity_score INT DEFAULT 0,
    last_loan_date DATE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    UNIQUE KEY unique_book_stats (book_id)
);

-- Bảng publishers (nhà xuất bản)
CREATE TABLE IF NOT EXISTS publishers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(500),
    phone VARCHAR(20),
    email VARCHAR(100),
    website VARCHAR(255),
    country VARCHAR(100),
    established_year INT,
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Cập nhật bảng books để liên kết với publishers
ALTER TABLE books ADD COLUMN IF NOT EXISTS publisher_id BIGINT;
ALTER TABLE books ADD CONSTRAINT fk_book_publisher 
    FOREIGN KEY (publisher_id) REFERENCES publishers(id) ON DELETE SET NULL;

-- Bảng book_categories (cải thiện quan hệ category)
ALTER TABLE books ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE books ADD CONSTRAINT fk_book_category 
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;

-- Tạo thêm các index cho performance
CREATE INDEX IF NOT EXISTS idx_user_membership_patron ON user_memberships(patron_id);
CREATE INDEX IF NOT EXISTS idx_user_membership_tier ON user_memberships(tier_id);
CREATE INDEX IF NOT EXISTS idx_book_status_history_book ON book_status_history(book_id);
CREATE INDEX IF NOT EXISTS idx_book_status_history_date ON book_status_history(change_date);
CREATE INDEX IF NOT EXISTS idx_user_violations_patron ON user_violations(patron_id);
CREATE INDEX IF NOT EXISTS idx_user_violations_type ON user_violations(type);
CREATE INDEX IF NOT EXISTS idx_user_violations_resolved ON user_violations(resolved);
CREATE INDEX IF NOT EXISTS idx_user_ratings_patron ON user_ratings(patron_id);
CREATE INDEX IF NOT EXISTS idx_user_ratings_rating ON user_ratings(rating);
CREATE INDEX IF NOT EXISTS idx_email_notifications_patron ON email_notifications(patron_id);
CREATE INDEX IF NOT EXISTS idx_email_notifications_status ON email_notifications(status);
CREATE INDEX IF NOT EXISTS idx_email_notifications_scheduled ON email_notifications(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_book_statistics_book ON book_statistics(book_id);
CREATE INDEX IF NOT EXISTS idx_publishers_name ON publishers(name);
CREATE INDEX IF NOT EXISTS idx_books_publisher ON books(publisher_id);
CREATE INDEX IF NOT EXISTS idx_books_category_id ON books(category_id);
