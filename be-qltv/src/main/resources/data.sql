-- Dữ liệu mẫu cho hệ thống quản lý thư viện

-- Xóa dữ liệu cũ (nếu có)
DELETE FROM book_authors;
DELETE FROM reviews;
DELETE FROM notifications;
DELETE FROM reservations;
DELETE FROM fines;
DELETE FROM loans;
DELETE FROM books;
DELETE FROM authors;
DELETE FROM categories;
DELETE FROM patrons;

-- Reset AUTO_INCREMENT
ALTER TABLE patrons AUTO_INCREMENT = 1;
ALTER TABLE books AUTO_INCREMENT = 1;
ALTER TABLE loans AUTO_INCREMENT = 1;
ALTER TABLE categories AUTO_INCREMENT = 1;
ALTER TABLE authors AUTO_INCREMENT = 1;
ALTER TABLE reservations AUTO_INCREMENT = 1;
ALTER TABLE fines AUTO_INCREMENT = 1;
ALTER TABLE notifications AUTO_INCREMENT = 1;
ALTER TABLE reviews AUTO_INCREMENT = 1;

-- Thêm dữ liệu Admin (mật khẩu: secret)
INSERT INTO patrons (name, email, phone, address, role, password, active, created_date) VALUES
('Admin User', 'admin@library.com', '0123456789', '123 Admin Street', 'ROLE_ADMIN', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPggGA4u6', TRUE, NOW()),
('Test User', 'the@gmail.com', '0123456999', '123 Test Street', 'ROLE_ADMIN', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPggGA4u6', TRUE, NOW()),
('Librarian One', 'librarian@library.com', '0123456788', '456 Library Ave', 'ROLE_LIBRARIAN', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPggGA4u6', TRUE, NOW()),
('John Doe', 'john@example.com', '0123456787', '789 User Road', 'ROLE_USER', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPggGA4u6', TRUE, NOW()),
('Jane Smith', 'jane@example.com', '0123456786', '321 Reader Lane', 'ROLE_USER', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPggGA4u6', TRUE, NOW()),
('Bob Wilson', 'bob@example.com', '0123456785', '654 Student Street', 'ROLE_USER', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPggGA4u6', TRUE, NOW()),
('Alice Brown', 'alice@example.com', '0123456784', '987 Scholar Avenue', 'ROLE_USER', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPggGA4u6', TRUE, NOW());

-- Thêm categories
INSERT INTO categories (name, description) VALUES
('Programming', 'Books about programming languages and software development'),
('Database', 'Books about database design and management'),
('Software Development', 'Books about software engineering practices'),
('Software Architecture', 'Books about system design and architecture patterns'),
('Computer Science', 'General computer science and algorithms'),
('Web Development', 'Books about web technologies and frameworks'),
('Mobile Development', 'Books about mobile app development'),
('Data Science', 'Books about data analysis and machine learning'),
('Cybersecurity', 'Books about information security'),
('DevOps', 'Books about deployment and operations');

-- Thêm authors
INSERT INTO authors (name, biography, nationality) VALUES
('Herbert Schildt', 'American computer programmer and author of programming books', 'American'),
('Craig Walls', 'Software developer and author specializing in Java and Spring', 'American'),
('Robert C. Martin', 'Software engineer and author, known as Uncle Bob', 'American'),
('Gang of Four', 'Erich Gamma, Richard Helm, Ralph Johnson, and John Vlissides', 'International'),
('Abraham Silberschatz', 'Computer scientist and professor', 'American'),
('Robert Sedgewick', 'Computer scientist and professor at Princeton', 'American'),
('David Thomas', 'Programmer and author of The Pragmatic Programmer', 'American'),
('Eric Freeman', 'Computer scientist and author', 'American'),
('Joshua Bloch', 'Software engineer who led the design of Java Collections Framework', 'American'),
('Thomas H. Cormen', 'Computer scientist and professor at Dartmouth College', 'American');

-- Thêm dữ liệu sách
INSERT INTO books (title, author, isbn, publisher, published_date, category, genre, total_copies, available_copies, description, status) VALUES
('Java: The Complete Reference', 'Herbert Schildt', '9780071808552', 'McGraw-Hill Education', '2020-01-01', 'Programming', 'Java', 5, 3, 'Comprehensive guide to Java programming language covering all essential topics.', 'AVAILABLE'),
('Spring Boot in Action', 'Craig Walls', '9781617292545', 'Manning Publications', '2019-05-15', 'Programming', 'Java', 3, 2, 'Practical guide to building applications with Spring Boot framework.', 'AVAILABLE'),
('Clean Code', 'Robert C. Martin', '9780132350884', 'Prentice Hall', '2008-08-01', 'Software Development', 'Best Practices', 4, 4, 'A handbook of agile software craftsmanship with best practices for writing clean code.', 'AVAILABLE'),
('Design Patterns', 'Gang of Four', '9780201633610', 'Addison-Wesley', '1994-10-31', 'Software Architecture', 'Design Patterns', 2, 1, 'Elements of reusable object-oriented software design patterns.', 'AVAILABLE'),
('Database System Concepts', 'Abraham Silberschatz', '9780078022159', 'McGraw-Hill Education', '2019-02-01', 'Database', 'Database Design', 3, 3, 'Comprehensive introduction to database system concepts and design.', 'AVAILABLE'),
('Algorithms', 'Robert Sedgewick', '9780321573513', 'Addison-Wesley', '2011-03-19', 'Computer Science', 'Algorithms', 4, 2, 'Essential information about algorithms and data structures.', 'AVAILABLE'),
('The Pragmatic Programmer', 'David Thomas', '9780135957059', 'Addison-Wesley', '2019-09-13', 'Software Development', 'Best Practices', 3, 3, 'Your journey to mastery in software development.', 'AVAILABLE'),
('Head First Design Patterns', 'Eric Freeman', '9780596007126', 'O Reilly Media', '2004-10-25', 'Software Architecture', 'Design Patterns', 2, 2, 'A brain-friendly guide to design patterns.', 'AVAILABLE'),
('Effective Java', 'Joshua Bloch', '9780134685991', 'Addison-Wesley', '2017-12-27', 'Programming', 'Java', 3, 1, 'Best practices for the Java platform by the designer of the Java Collections Framework.', 'AVAILABLE'),
('Introduction to Algorithms', 'Thomas H. Cormen', '9780262033848', 'MIT Press', '2009-07-31', 'Computer Science', 'Algorithms', 5, 4, 'Comprehensive introduction to the modern study of computer algorithms.', 'AVAILABLE'),
('JavaScript: The Good Parts', 'Douglas Crockford', '9780596517748', 'O Reilly Media', '2008-05-01', 'Programming', 'JavaScript', 3, 3, 'A deep dive into the good parts of JavaScript.', 'AVAILABLE'),
('Python Crash Course', 'Eric Matthes', '9781593279288', 'No Starch Press', '2019-05-03', 'Programming', 'Python', 4, 4, 'A hands-on introduction to programming with Python.', 'AVAILABLE');

-- Thêm quan hệ book-authors
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5),
(6, 6), (7, 7), (8, 8), (9, 9), (10, 10);

-- Thêm dữ liệu mượn sách (một số sách đã được mượn)
INSERT INTO loans (patron_id, book_id, loan_date, due_date, status, fine_amount) VALUES
(3, 1, '2024-01-15', '2024-02-15', 'ACTIVE', 0.00),
(3, 6, '2024-01-20', '2024-02-20', 'ACTIVE', 0.00),
(4, 2, '2024-01-10', '2024-02-10', 'RETURNED', 0.00),
(4, 9, '2024-01-25', '2024-02-25', 'OVERDUE', 5.00),
(5, 4, '2024-01-18', '2024-02-18', 'ACTIVE', 0.00),
(5, 6, '2024-01-22', '2024-02-22', 'RETURNED', 0.00),
(6, 3, '2024-01-12', '2024-02-12', 'OVERDUE', 3.00);

-- Cập nhật return_date cho các sách đã trả
UPDATE loans SET return_date = '2024-02-08' WHERE patron_id = 4 AND book_id = 2;
UPDATE loans SET return_date = '2024-02-20' WHERE patron_id = 5 AND book_id = 6;

-- Thêm fines
INSERT INTO fines (loan_id, patron_id, amount, reason, status) VALUES
(4, 4, 5.00, 'Late return fee', 'UNPAID'),
(7, 6, 3.00, 'Overdue book fine', 'UNPAID');

-- Thêm reservations
INSERT INTO reservations (patron_id, book_id, reservation_date, expiry_date, status) VALUES
(3, 4, '2024-01-30', '2024-02-06', 'ACTIVE'),
(5, 9, '2024-01-28', '2024-02-04', 'EXPIRED');

-- Thêm notifications
INSERT INTO notifications (patron_id, title, message, type, is_read) VALUES
(3, 'Book Due Soon', 'Your book "Java: The Complete Reference" is due in 3 days.', 'REMINDER', FALSE),
(4, 'Overdue Book', 'Your book "Effective Java" is overdue. Please return it to avoid additional fines.', 'WARNING', FALSE),
(5, 'Book Available', 'The book you reserved "Design Patterns" is now available for pickup.', 'INFO', TRUE),
(6, 'Fine Notice', 'You have an outstanding fine of $3.00. Please pay at the circulation desk.', 'FINE', FALSE);

-- Thêm reviews
INSERT INTO reviews (book_id, patron_id, rating, comment) VALUES
(1, 3, 5, 'Excellent comprehensive guide to Java. Highly recommended!'),
(2, 4, 4, 'Great book for learning Spring Boot. Very practical examples.'),
(3, 5, 5, 'Must-read for any serious developer. Changed how I write code.'),
(5, 6, 4, 'Very thorough coverage of database concepts. Good for students.'),
(6, 3, 5, 'Essential algorithms book. Clear explanations and good examples.');

-- Thêm membership tiers
INSERT INTO membership_tiers (name, level, max_books, loan_duration_days, late_fee_discount, reservation_priority, early_access, min_loans_required, min_points_required, max_violations_allowed, color, icon) VALUES
('Thành viên Cơ bản', 'BASIC', 3, 14, 0.00, FALSE, FALSE, 0, 0, 5, '#6B7280', 'user'),
('Thành viên VIP', 'VIP', 5, 21, 20.00, TRUE, FALSE, 20, 100, 2, '#3B82F6', 'star'),
('Thành viên Premium', 'PREMIUM', 10, 30, 50.00, TRUE, TRUE, 50, 300, 1, '#F59E0B', 'crown');

-- Thêm publishers
INSERT INTO publishers (name, address, phone, email, website, country, established_year, description) VALUES
('Kim Đồng', '55 Quang Trung, Hai Bà Trưng, Hà Nội', '024 3942 0008', 'info@nxbkimdong.com.vn', 'https://nxbkimdong.com.vn', 'Vietnam', 1957, 'Nhà xuất bản chuyên về sách thiếu nhi và giáo dục'),
('Trẻ', '161B Lý Chính Thắng, Quận 3, TP.HCM', '028 3930 5742', 'info@nxbtre.com.vn', 'https://nxbtre.com.vn', 'Vietnam', 1981, 'Nhà xuất bản Trẻ'),
('Lao động', '175 Giảng Võ, Đống Đa, Hà Nội', '024 3851 3388', 'info@nxblaodong.com.vn', 'https://nxblaodong.com.vn', 'Vietnam', 1958, 'Nhà xuất bản Lao động'),
('Văn học', '18 Nguyễn Trường Tộ, Ba Đình, Hà Nội', '024 3733 5202', 'info@nxbvanhoc.com.vn', 'https://nxbvanhoc.com.vn', 'Vietnam', 1957, 'Nhà xuất bản Văn học'),
('Thông tin và Truyền thông', '115 Trần Duy Hưng, Cầu Giấy, Hà Nội', '024 3795 6666', 'info@nxbtttt.vn', 'https://nxbtttt.vn', 'Vietnam', 1956, 'Nhà xuất bản Thông tin và Truyền thông'),
("O'Reilly Media", '1005 Gravenstein Highway North, Sebastopol, CA', '+1-707-827-7000', 'info@oreilly.com', 'https://oreilly.com', 'USA', 1978, 'Technology and business publisher'),
('Pearson Education', '221 River Street, Hoboken, NJ', '+1-201-236-7000', 'info@pearson.com', 'https://pearson.com', 'USA', 1844, 'Educational publisher'),
('MIT Press', '1 Rogers Street, Cambridge, MA', '+1-617-253-5646', 'info@mitpress.mit.edu', 'https://mitpress.mit.edu', 'USA', 1962, 'Academic publisher'),
('Apress', '233 Spring Street, New York, NY', '+1-212-460-1500', 'info@apress.com', 'https://apress.com', 'USA', 1999, 'Technical publisher'),
('Manning Publications', '20 Baldwin Road, Shelter Island, NY', '+1-203-626-2002', 'support@manning.com', 'https://manning.com', 'USA', 1994, 'Computer book publisher');

-- Thêm user memberships cho TẤT CẢ users (bao gồm admin, librarian và users)
-- Patron IDs: 1=Admin, 2=Test User, 3=Librarian, 4-7=Regular Users
INSERT INTO user_memberships (patron_id, tier_id, current_points, total_loans, violation_count, join_date) VALUES
(1, 3, 500, 100, 0, '2024-01-01'), -- Admin User - Premium
(2, 3, 400, 80, 0, '2024-01-01'),  -- Test User - Premium  
(3, 2, 200, 30, 0, '2024-01-01'),  -- Librarian One - VIP
(4, 2, 150, 25, 1, '2024-01-15'),  -- John Doe - VIP
(5, 1, 50, 8, 0, '2024-03-10'),    -- Jane Smith - Basic
(6, 3, 350, 55, 0, '2023-12-01'),  -- Bob Wilson - Premium
(7, 1, 30, 5, 2, '2024-05-20');    -- Alice Brown - Basic

-- Thêm user ratings
INSERT INTO user_ratings (patron_id, rating, score, total_loans, on_time_returns, late_returns, violation_points) VALUES
(4, 'GOOD', 85, 25, 22, 3, 10),      -- John Doe
(5, 'EXCELLENT', 95, 8, 8, 0, 0),    -- Jane Smith
(6, 'EXCELLENT', 98, 55, 55, 0, 0),  -- Bob Wilson
(7, 'AVERAGE', 70, 5, 3, 2, 20);     -- Alice Brown

-- Thêm một số violations mẫu
INSERT INTO user_violations (patron_id, type, description, violation_date, penalty_amount, resolved, severity) VALUES
(4, 'LATE_RETURN', 'Trả sách muộn 3 ngày', '2024-08-15', 15000, TRUE, 'LOW'),
(7, 'LATE_RETURN', 'Trả sách muộn 7 ngày', '2024-07-10', 35000, FALSE, 'MEDIUM'),
(7, 'DAMAGE', 'Làm rách trang sách', '2024-06-20', 50000, FALSE, 'HIGH');

-- Thêm email notifications mẫu
INSERT INTO email_notifications (patron_id, type, title, message, email_sent, sent_date, status) VALUES
(4, 'REMINDER', 'Nhắc nhở trả sách', 'Sách "Clean Code" sẽ đến hạn trả vào ngày mai', TRUE, NOW(), 'SENT'),
(5, 'BOOK_AVAILABLE', 'Sách đã sẵn sàng', 'Sách "Design Patterns" mà bạn đặt trước đã có sẵn', TRUE, NOW(), 'SENT'),
(6, 'MEMBERSHIP_UPDATE', 'Nâng hạng thành viên', 'Chúc mừng! Bạn đã được nâng lên hạng Premium', TRUE, NOW(), 'SENT');
