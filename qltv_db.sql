-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: localhost:3307
-- Thời gian đã tạo: Th9 29, 2025 lúc 11:14 AM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `qltv_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `authors`
--

CREATE TABLE `authors` (
  `id` bigint(20) NOT NULL,
  `biography` text DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `nationality` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `authors`
--

INSERT INTO `authors` (`id`, `biography`, `birth_date`, `created_date`, `name`, `nationality`) VALUES
(1, 'American author of books on programming languages', NULL, '2025-09-24 13:48:28.000000', 'Herbert Schildt', 'American'),
(2, 'Spring Framework expert and author', NULL, '2025-09-24 13:48:28.000000', 'Craig Walls', 'American'),
(3, 'Software engineer and author known as Uncle Bob', NULL, '2025-09-24 13:48:28.000000', 'Robert C. Martin', 'American'),
(4, 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', NULL, '2025-09-24 13:48:28.000000', 'Gang of Four', 'International'),
(5, 'Computer science professor and author', NULL, '2025-09-24 13:48:28.000000', 'Abraham Silberschatz', 'American'),
(6, 'Computer science professor at Princeton', NULL, '2025-09-24 13:48:28.000000', 'Robert Sedgewick', 'American'),
(7, 'Pragmatic programmer and author', NULL, '2025-09-24 13:48:28.000000', 'David Thomas', 'British'),
(8, 'Software architect and author', NULL, '2025-09-24 13:48:28.000000', 'Eric Freeman', 'American'),
(9, 'Software engineer at Google, formerly at Sun', NULL, '2025-09-24 13:48:28.000000', 'Joshua Bloch', 'American'),
(10, 'Computer science professor at Dartmouth', NULL, '2025-09-24 13:48:28.000000', 'Thomas H. Cormen', 'American'),
(11, 'sfhjkjhgbfd', '1979-01-19', '2025-09-25 17:55:38.000000', 'Nam Cao', 'Việt Nam'),
(12, 'ágdhfjgkh', '2020-02-08', '2025-09-25 18:43:57.000000', 'sadsfgdhmj', 'adsfghj'),
(13, 'Vũ Trọng Phụng (1912-1939) là một nhà văn, nhà báo nổi tiếng của Việt Nam vào đầu thế kỷ 20. Tuy thời gian cầm bút rất ngắn ngủi, với tác phẩm đầu tay là truyện ngắn Chống nạng lên đường đăng trên Ngọ báo vào năm 1930, ông đã để lại một kho tác phẩm đáng kinh ngạc: hơn 30 truyện ngắn, 9 tập tiểu thuyết, 9 tập phóng sự, 7 vở kịch, cùng một bản dịch vở kịch từ tiếng Pháp, một số bài viết phê bình, tranh luận văn học và hàng trăm bài báo viết về các vấn đề chính trị, xã hội, văn hóa[1]. Một số trích đoạn tác phẩm của ông trong các tác phẩm Số đỏ và Giông Tố đã được đưa vào sách giáo khoa môn Ngữ văn của Việt Nam.[2][3]', '1919-10-20', '2025-09-26 18:08:20.000000', 'Vũ Trọng Phụng', 'Việt Nam');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `books`
--

CREATE TABLE `books` (
  `id` bigint(20) NOT NULL,
  `author` varchar(255) NOT NULL,
  `available_copies` int(11) NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `genre` varchar(100) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `isbn` varchar(20) DEFAULT NULL,
  `published_date` date DEFAULT NULL,
  `publisher` varchar(255) DEFAULT NULL,
  `status` enum('AVAILABLE','ON_LOAN','UNAVAILABLE') NOT NULL,
  `title` varchar(255) NOT NULL,
  `total_copies` int(11) NOT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `publisher_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `books`
--

INSERT INTO `books` (`id`, `author`, `available_copies`, `category`, `created_date`, `description`, `genre`, `image_url`, `isbn`, `published_date`, `publisher`, `status`, `title`, `total_copies`, `updated_date`, `category_id`, `publisher_id`) VALUES
(1, 'Herbert Schildt', 5, NULL, '2025-09-24 13:48:28.000000', '', 'Programming', 'https://laptrinhjavaweb.com/repository/ckfinder/images/core_java_volumn.jpg', '23456789987654', '2020-01-01', NULL, 'AVAILABLE', 'Java:', 5, '2025-09-27 23:28:32.000000', 6, 1),
(2, 'Craig Walls', 3, NULL, '2025-09-24 13:48:28.000000', '', 'Programming', 'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1440839028i/26186049.jpg', '234789384756567', '2019-01-01', NULL, 'AVAILABLE', 'Spring Boot in Action', 3, '2025-09-27 23:27:30.000000', 6, 1),
(3, 'Robert C. Martin', 4, NULL, '2025-09-24 13:48:28.000000', '', 'Software Development', 'https://salt.tikicdn.com/cache/750x750/ts/product/ea/e7/41/498ad677d05798241b67bf97b86af986.jpg.webp', '1234567890345', '2008-01-01', NULL, 'AVAILABLE', 'Clean Code', 4, '2025-09-27 23:42:43.000000', 3, 4),
(4, 'Gang of Four', 1, NULL, '2025-09-24 13:48:28.000000', NULL, 'Software Architecture', NULL, NULL, '1994-01-01', NULL, 'AVAILABLE', 'Design Patterns', 2, '2025-09-24 13:48:28.000000', NULL, NULL),
(5, 'Abraham Silberschatz', 3, NULL, '2025-09-24 13:48:28.000000', '', 'Database', '', '987654323456', '2019-01-01', NULL, 'AVAILABLE', 'Database System Concepts', 3, '2025-09-27 21:20:17.000000', 2, 4),
(6, 'Robert Sedgewick', 2, NULL, '2025-09-24 13:48:28.000000', NULL, 'Computer Science', NULL, NULL, '2011-01-01', NULL, 'AVAILABLE', 'Algorithms', 4, '2025-09-24 13:48:28.000000', NULL, NULL),
(7, 'David Thomas', 3, NULL, '2025-09-24 13:48:28.000000', NULL, 'Software Development', NULL, NULL, '2019-01-01', NULL, 'AVAILABLE', 'The Pragmatic Programmer', 3, '2025-09-24 13:48:28.000000', NULL, NULL),
(8, 'Eric Freeman', 2, NULL, '2025-09-24 13:48:28.000000', NULL, 'Software Architecture', NULL, NULL, '2004-01-01', NULL, 'AVAILABLE', 'Head First Design Patterns', 2, '2025-09-24 13:48:28.000000', NULL, NULL),
(9, 'Joshua Bloch', 1, NULL, '2025-09-24 13:48:28.000000', NULL, 'Programming', NULL, NULL, '2017-01-01', NULL, 'AVAILABLE', 'Effective Java', 3, '2025-09-24 13:48:28.000000', NULL, NULL),
(10, 'Thomas H. Cormen', 4, NULL, '2025-09-24 13:48:28.000000', NULL, 'Computer Science', NULL, NULL, '2009-01-01', NULL, 'AVAILABLE', 'Introduction to Algorithms', 5, '2025-09-24 13:48:28.000000', NULL, NULL),
(11, 'Vũ Trọng Phụng', 1, NULL, '2025-09-27 20:56:24.000000', '', '', '', '12345678', '2025-01-01', NULL, 'AVAILABLE', 'ádfg', 1, '2025-09-27 20:56:24.000000', 2, NULL),
(12, 'Ngô Tất Tố', 1, NULL, '2025-09-27 21:12:57.000000', '', '', 'https://product.hstatic.net/1000237375/product/bia_truoc_08_b4497226ce524fd79a1d81b9086b99a9.jpg', '1234567890', '2025-01-01', NULL, 'AVAILABLE', 'Tắt đèn', 1, '2025-09-27 21:12:57.000000', 2, 11);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `book_authors`
--

CREATE TABLE `book_authors` (
  `book_id` bigint(20) NOT NULL,
  `author_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `book_authors`
--

INSERT INTO `book_authors` (`book_id`, `author_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(5, 5);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `book_reservations`
--

CREATE TABLE `book_reservations` (
  `id` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `expiry_date` datetime(6) DEFAULT NULL,
  `notification_sent` bit(1) DEFAULT NULL,
  `reservation_date` datetime(6) NOT NULL,
  `status` enum('ACTIVE','CANCELLED','EXPIRED','FULFILLED') NOT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `book_id` bigint(20) NOT NULL,
  `patron_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `categories`
--

CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `categories`
--

INSERT INTO `categories` (`id`, `created_date`, `description`, `name`) VALUES
(2, NULL, 'khoa học', 'khoa học'),
(3, '2025-09-25 19:41:17.000000', 'kinh dị', 'kinh dị'),
(4, '2025-09-25 19:42:03.000000', 'trinh thám', 'trinh thám'),
(5, '2025-09-26 18:13:27.000000', 'Kinh tế', 'Kinh tế'),
(6, '2025-09-27 21:02:34.000000', 'Công nghệ', 'Công nghệ'),
(7, '2025-09-27 23:29:36.000000', 'Tâm lí', 'Tâm li xã hội');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `fines`
--

CREATE TABLE `fines` (
  `id` bigint(20) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `paid_date` date DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `loan_id` bigint(20) NOT NULL,
  `patron_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `loans`
--

CREATE TABLE `loans` (
  `id` bigint(20) NOT NULL,
  `created_date` date DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `fine_amount` decimal(10,2) DEFAULT NULL,
  `is_renewed` bit(1) DEFAULT NULL,
  `loan_date` date NOT NULL,
  `renewal_count` int(11) DEFAULT NULL,
  `return_date` date DEFAULT NULL,
  `status` enum('BORROWED','OVERDUE','RENEWED','RETURNED') NOT NULL,
  `updated_date` date DEFAULT NULL,
  `book_id` bigint(20) NOT NULL,
  `patron_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `loans`
--

INSERT INTO `loans` (`id`, `created_date`, `due_date`, `fine_amount`, `is_renewed`, `loan_date`, `renewal_count`, `return_date`, `status`, `updated_date`, `book_id`, `patron_id`) VALUES
(1, '2025-09-24', '2024-02-15', 0.00, b'0', '2024-01-15', 0, NULL, 'BORROWED', '2025-09-24', 1, 3),
(2, '2025-09-24', '2024-02-20', 0.00, b'0', '2024-01-20', 0, NULL, 'BORROWED', '2025-09-24', 6, 3),
(3, '2025-09-24', '2024-02-10', 0.00, b'0', '2024-01-10', 0, '2024-02-08', 'RETURNED', '2025-09-24', 2, 4),
(4, '2025-09-24', '2024-02-25', 0.00, b'0', '2024-01-25', 0, NULL, 'BORROWED', '2025-09-24', 9, 4),
(5, '2025-09-24', '2024-02-18', 0.00, b'0', '2024-01-18', 0, NULL, 'BORROWED', '2025-09-24', 4, 5),
(7, '2025-09-27', '2025-10-11', 0.00, b'0', '2025-09-27', 0, '2025-09-27', 'RETURNED', '2025-09-27', 1, 6),
(8, '2025-09-27', '2025-10-11', 0.00, b'0', '2025-09-27', 0, NULL, 'BORROWED', '2025-09-27', 2, 6),
(11, '2025-09-27', '2025-10-11', 0.00, b'0', '2025-09-27', 0, NULL, 'BORROWED', '2025-09-27', 1, 6),
(12, '2025-09-27', '2025-10-11', 0.00, b'0', '2025-09-27', 0, NULL, 'BORROWED', '2025-09-27', 3, 6);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `membership_tiers`
--

CREATE TABLE `membership_tiers` (
  `id` bigint(20) NOT NULL,
  `color` varchar(50) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `early_access` bit(1) DEFAULT NULL,
  `icon` varchar(50) DEFAULT NULL,
  `late_fee_discount` decimal(5,2) DEFAULT NULL,
  `level` enum('BASIC','PREMIUM','VIP') NOT NULL,
  `loan_duration_days` int(11) NOT NULL,
  `max_books` int(11) NOT NULL,
  `max_violations_allowed` int(11) DEFAULT NULL,
  `min_loans_required` int(11) DEFAULT NULL,
  `min_points_required` int(11) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `reservation_priority` bit(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL,
  `book_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `is_read` bit(1) DEFAULT NULL,
  `loan_id` bigint(20) DEFAULT NULL,
  `message` text NOT NULL,
  `read_date` datetime(6) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `type` enum('ERROR','INFO','REMINDER','SUCCESS','WARNING') NOT NULL,
  `patron_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `patrons`
--

CREATE TABLE `patrons` (
  `id` bigint(20) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','LIBRARIAN','USER') NOT NULL,
  `updated_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `patrons`
--

INSERT INTO `patrons` (`id`, `address`, `created_date`, `email`, `is_active`, `name`, `password`, `phone_number`, `role`, `updated_date`) VALUES
(1, '123 Admin Street', '2025-09-24', 'admin@library.com', b'1', 'Admin User', '$2a$10$W86l/jnanHmXA3PU5dxQr.mgAoPSH9VP8zI42/uIldtO6XBynXtk6', '0123456789', 'ADMIN', '2025-09-24'),
(2, '456 Library Ave', '2025-09-24', 'librarian@library.com', b'1', 'Librarian One', '$2a$10$IDKKd88I/R9vVgGxoIdwm.oe.9JnROhD0MrG7aL/VQ9OdioL8NSAe', '0123456788', 'LIBRARIAN', '2025-09-24'),
(3, '789 User Road', '2025-09-24', 'john@example.com', b'1', 'John Doe', '$2a$10$6Qox0LY0m3fZOEIzS7bM1uGFcg/McVE7DetRz434qpPFIzPs/7.2q', '0123456787', 'USER', '2025-09-24'),
(4, '321 Reader Lane', '2025-09-24', 'jane@example.com', b'1', 'Jane Smith', '$2a$10$dHZtF6JL1RzD5nd34U/kVOFbStbegk73Fcyb30buOe1n07xSKx3g6', '0123456786', 'USER', '2025-09-24'),
(5, '654 Student Street', '2025-09-24', 'bob@example.com', b'1', 'Bob Wilson', '$2a$10$sWXCROm//8cjPS7DYI6MWe3rFmQ0ypGfTPMb2qqApBNLdFWiT4Ru2', '0123456785', 'USER', '2025-09-24'),
(6, 'tp ha noi', '2025-09-24', 'the@gmail.com', b'1', 'thepham', '$2a$10$1kgWnrcfD4Nl/OEeH/JGOeF6ahinmzzuNu7HEyD2mFdvVMsr31HGq', '0923128976', 'ADMIN', '2025-09-24'),
(7, 'gregretg', '2025-09-25', 'test123@gmail.com', b'1', 'test', '$2a$10$uxi9nbLZCiQZbJx49nR1jusaJAh6oN5ZzSsrckbiLeXrWqJPFt8TW', '09387534765', 'USER', '2025-09-25'),
(8, 'hsgregew', '2025-09-25', 'the123245@gmail.com', b'1', 'ỉhgeuo rvg', '$2a$10$VUJKDPHRu3iQ4oZkwA1he.xa7zWt6csOEN67c8YJJdW16BRVoucd.', '56523354379', 'USER', '2025-09-25'),
(9, 'sfhbykhf', '2025-09-26', 'the123@gmail.com', b'1', 'phạm thế', '$2a$10$sM0NMfVhgoLAPNGwOoThM.miUn.1facYk0iOtyyrFSVOB1xbT9Fpm', '08256374123', 'USER', '2025-09-26'),
(10, 'hà nội', '2025-09-27', 'phamthe@gmail.com', b'1', 'pham the', '$2a$10$jMDeh7IdNRigqvic23KD9uoGWN4d6mAVBSx2oe8dBKTx.osSQDdmy', '0923857734', 'USER', '2025-09-27');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `publishers`
--

CREATE TABLE `publishers` (
  `id` bigint(20) NOT NULL,
  `address` varchar(500) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `established_year` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `publishers`
--

INSERT INTO `publishers` (`id`, `address`, `country`, `created_date`, `description`, `email`, `established_year`, `name`, `phone`, `updated_date`, `website`) VALUES
(1, 'New York, USA', NULL, '2025-09-24 13:48:28.000000', 'Technology and Education', NULL, NULL, 'McGraw-Hill Education', NULL, '2025-09-24 13:48:28.000000', NULL),
(2, 'Greenwich, USA', NULL, '2025-09-24 13:48:28.000000', 'Computer Science and Programming', NULL, NULL, 'Manning Publications', NULL, '2025-09-24 13:48:28.000000', NULL),
(3, 'New Jersey, USA', NULL, '2025-09-24 13:48:28.000000', 'Academic and Professional', NULL, NULL, 'Prentice Hall', NULL, '2025-09-24 13:48:28.000000', NULL),
(4, 'Massachusetts, USA', NULL, '2025-09-24 13:48:28.000000', 'Technology and Programming', NULL, NULL, 'Addison-Wesley', NULL, '2025-09-24 13:48:28.000000', NULL),
(5, 'California, USA', NULL, '2025-09-24 13:48:28.000000', 'Technology and Programming', NULL, NULL, 'O\'Reilly Media', NULL, '2025-09-24 13:48:28.000000', NULL),
(6, 'Massachusetts, USA', NULL, '2025-09-24 13:48:28.000000', 'Academic and Technical', NULL, NULL, 'MIT Press', NULL, '2025-09-24 13:48:28.000000', NULL),
(7, 'ghmtigfd', NULL, '2025-09-25 17:56:30.000000', NULL, 'kimdong@vn.com', NULL, 'Nhà xuât bản Kim Đồng', NULL, '2025-09-25 17:56:30.000000', NULL),
(8, 'ưe gregth', NULL, '2025-09-25 18:01:24.000000', NULL, 'ssdbghr@jfrwhfo.com', NULL, 'sdgvre ye', NULL, '2025-09-25 18:01:24.000000', NULL),
(9, 'r gtrh vjy', NULL, '2025-09-25 19:28:02.000000', NULL, 'kin=mdong@gmail.com', NULL, 'eh ytujk', NULL, '2025-09-25 19:28:02.000000', NULL),
(10, 'akdbfjwhfew', NULL, '2025-09-26 12:58:07.000000', NULL, 'kimdong@gmail.com', NULL, ',kim dong', NULL, '2025-09-27 23:31:05.000000', 'https://nxbkimdong.com.vn/'),
(11, 'Hà nội', NULL, '2025-09-26 19:02:30.000000', NULL, 'kimdong@vn.com', NULL, 'Kim đồng', NULL, '2025-09-26 19:02:30.000000', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `reservations`
--

CREATE TABLE `reservations` (
  `id` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `expiry_date` date NOT NULL,
  `notes` text DEFAULT NULL,
  `reservation_date` date NOT NULL,
  `status` varchar(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `book_id` bigint(20) NOT NULL,
  `patron_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `reviews`
--

CREATE TABLE `reviews` (
  `id` bigint(20) NOT NULL,
  `comment` text DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `rating` int(11) NOT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `book_id` bigint(20) NOT NULL,
  `patron_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `authors`
--
ALTER TABLE `authors`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `books`
--
ALTER TABLE `books`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKkibbepcitr0a3cpk3rfr7nihn` (`isbn`),
  ADD KEY `FKleqa3hhc0uhfvurq6mil47xk0` (`category_id`),
  ADD KEY `FKayy5edfrqnegqj3882nce6qo8` (`publisher_id`);

--
-- Chỉ mục cho bảng `book_authors`
--
ALTER TABLE `book_authors`
  ADD PRIMARY KEY (`book_id`,`author_id`),
  ADD KEY `FKo86065vktj3hy1m7syr9cn7va` (`author_id`);

--
-- Chỉ mục cho bảng `book_reservations`
--
ALTER TABLE `book_reservations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKg5kli21h3rfndgsed1pkgnucc` (`book_id`),
  ADD KEY `FK9liewhwe17frkktxumiui3aa0` (`patron_id`);

--
-- Chỉ mục cho bảng `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKt8o6pivur7nn124jehx7cygw5` (`name`);

--
-- Chỉ mục cho bảng `fines`
--
ALTER TABLE `fines`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKpxdidln7gte3bgknqqhknl99h` (`loan_id`),
  ADD KEY `FKosxp03pv9govwgitwmrjcnrt8` (`patron_id`);

--
-- Chỉ mục cho bảng `loans`
--
ALTER TABLE `loans`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKokwvlrv6o4i4h3le3bwhe6kie` (`book_id`),
  ADD KEY `FKm7e8wdupps8pkcth4otj8d7pk` (`patron_id`);

--
-- Chỉ mục cho bảng `membership_tiers`
--
ALTER TABLE `membership_tiers`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK62qkmvgd9n3ho73qle5dtcjco` (`patron_id`);

--
-- Chỉ mục cho bảng `patrons`
--
ALTER TABLE `patrons`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK6c7ccr5q6fghxmuplkunme4c8` (`email`);

--
-- Chỉ mục cho bảng `publishers`
--
ALTER TABLE `publishers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKan1ucpx8sw2qm194mlok8e5us` (`name`);

--
-- Chỉ mục cho bảng `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKrsdd3ib3landfpmgoolccjakt` (`book_id`),
  ADD KEY `FK8kcwu81qxp0fjg1ttrgpf57yf` (`patron_id`);

--
-- Chỉ mục cho bảng `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK6a9k6xvev80se5rreqvuqr7f9` (`book_id`),
  ADD KEY `FKc02ygoff0palxkllhukiri9w9` (`patron_id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `authors`
--
ALTER TABLE `authors`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT cho bảng `books`
--
ALTER TABLE `books`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT cho bảng `book_reservations`
--
ALTER TABLE `book_reservations`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `categories`
--
ALTER TABLE `categories`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT cho bảng `fines`
--
ALTER TABLE `fines`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `loans`
--
ALTER TABLE `loans`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT cho bảng `membership_tiers`
--
ALTER TABLE `membership_tiers`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `patrons`
--
ALTER TABLE `patrons`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT cho bảng `publishers`
--
ALTER TABLE `publishers`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT cho bảng `reservations`
--
ALTER TABLE `reservations`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `books`
--
ALTER TABLE `books`
  ADD CONSTRAINT `FKayy5edfrqnegqj3882nce6qo8` FOREIGN KEY (`publisher_id`) REFERENCES `publishers` (`id`),
  ADD CONSTRAINT `FKleqa3hhc0uhfvurq6mil47xk0` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);

--
-- Các ràng buộc cho bảng `book_authors`
--
ALTER TABLE `book_authors`
  ADD CONSTRAINT `FKbhqtkv2cndf10uhtknaqbyo0a` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`),
  ADD CONSTRAINT `FKo86065vktj3hy1m7syr9cn7va` FOREIGN KEY (`author_id`) REFERENCES `authors` (`id`);

--
-- Các ràng buộc cho bảng `book_reservations`
--
ALTER TABLE `book_reservations`
  ADD CONSTRAINT `FK9liewhwe17frkktxumiui3aa0` FOREIGN KEY (`patron_id`) REFERENCES `patrons` (`id`),
  ADD CONSTRAINT `FKg5kli21h3rfndgsed1pkgnucc` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`);

--
-- Các ràng buộc cho bảng `fines`
--
ALTER TABLE `fines`
  ADD CONSTRAINT `FKosxp03pv9govwgitwmrjcnrt8` FOREIGN KEY (`patron_id`) REFERENCES `patrons` (`id`),
  ADD CONSTRAINT `FKpxdidln7gte3bgknqqhknl99h` FOREIGN KEY (`loan_id`) REFERENCES `loans` (`id`);

--
-- Các ràng buộc cho bảng `loans`
--
ALTER TABLE `loans`
  ADD CONSTRAINT `FKm7e8wdupps8pkcth4otj8d7pk` FOREIGN KEY (`patron_id`) REFERENCES `patrons` (`id`),
  ADD CONSTRAINT `FKokwvlrv6o4i4h3le3bwhe6kie` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`);

--
-- Các ràng buộc cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `FK62qkmvgd9n3ho73qle5dtcjco` FOREIGN KEY (`patron_id`) REFERENCES `patrons` (`id`);

--
-- Các ràng buộc cho bảng `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `FK8kcwu81qxp0fjg1ttrgpf57yf` FOREIGN KEY (`patron_id`) REFERENCES `patrons` (`id`),
  ADD CONSTRAINT `FKrsdd3ib3landfpmgoolccjakt` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`);

--
-- Các ràng buộc cho bảng `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `FK6a9k6xvev80se5rreqvuqr7f9` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`),
  ADD CONSTRAINT `FKc02ygoff0palxkllhukiri9w9` FOREIGN KEY (`patron_id`) REFERENCES `patrons` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
