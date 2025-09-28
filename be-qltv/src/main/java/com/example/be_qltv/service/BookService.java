package com.example.be_qltv.service;

import com.example.be_qltv.dto.AuthorDTO;
import com.example.be_qltv.dto.BookDTO;
import com.example.be_qltv.dto.DashboardStats;
import com.example.be_qltv.entity.Author;
import com.example.be_qltv.entity.Book;
import com.example.be_qltv.entity.Category;
import com.example.be_qltv.entity.Publisher;
import com.example.be_qltv.enums.BookStatus;
import com.example.be_qltv.repository.AuthorRepository;
import com.example.be_qltv.repository.BookRepository;
import com.example.be_qltv.repository.CategoryRepository;
import com.example.be_qltv.repository.PublisherRepository;
import com.example.be_qltv.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.example.be_qltv.dto.BookStatisticsDTO;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class BookService {
    public Page<BookDTO> getFavoriteBooks(Pageable pageable) {
        Page<Object[]> page = bookRepository.findBooksOrderByBorrowCountDesc(pageable);
        List<BookDTO> dtos = page.getContent().stream().map(row -> {
            Book book = (Book) row[0];
            Long borrowCount = (row[1] instanceof Long) ? (Long) row[1] : ((Number) row[1]).longValue();
            BookDTO dto = convertToDTO(book);
            dto.setBorrowCount(borrowCount);
            return dto;
        }).collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private LoanRepository loanRepository;

    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<BookDTO> getBookById(Long id) {
        return bookRepository.findById(id)
                .map(this::convertToDTO);
    }

    public BookDTO createBook(BookDTO bookDTO) {
        Book book = convertToEntity(bookDTO);
        
        // Set references
        if (bookDTO.getAuthorIds() != null && !bookDTO.getAuthorIds().isEmpty()) {
            Set<Author> authors = authorRepository.findAllById(bookDTO.getAuthorIds())
                .stream().collect(Collectors.toSet());
            book.setAuthors(authors);
        }
        
        if (bookDTO.getCategoryId() != null) {
            categoryRepository.findById(bookDTO.getCategoryId())
                .ifPresent(book::setCategory);
        }
        
        if (bookDTO.getPublisherId() != null) {
            publisherRepository.findById(bookDTO.getPublisherId())
                .ifPresent(book::setPublisher);
        }
        
        Book savedBook = bookRepository.save(book);
        return convertToDTO(savedBook);
    }

    public Optional<BookDTO> updateBook(Long id, BookDTO bookDTO) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    updateBookFromDTO(existingBook, bookDTO);
                    
                    // Update references
                    if (bookDTO.getAuthorIds() != null && !bookDTO.getAuthorIds().isEmpty()) {
                        Set<Author> authors = authorRepository.findAllById(bookDTO.getAuthorIds())
                            .stream().collect(Collectors.toSet());
                        existingBook.setAuthors(authors);
                    }
                    
                    if (bookDTO.getCategoryId() != null) {
                        categoryRepository.findById(bookDTO.getCategoryId())
                            .ifPresent(existingBook::setCategory);
                    }
                    
                    if (bookDTO.getPublisherId() != null) {
                        publisherRepository.findById(bookDTO.getPublisherId())
                            .ifPresent(existingBook::setPublisher);
                    }
                    
                    Book savedBook = bookRepository.save(existingBook);
                    return convertToDTO(savedBook);
                });
    }

    public boolean deleteBook(Long id) {
        return bookRepository.findById(id)
            .map(book -> {
                // Check if book can be deleted
                if (book.getStatus() == BookStatus.BORROWED) {
                    throw new IllegalStateException("Cannot delete book that is currently borrowed");
                }
                bookRepository.delete(book);
                return true;
            })
            .orElse(false);
    }

    public BookStatisticsDTO getBookStatistics() {
        BookStatisticsDTO stats = new BookStatisticsDTO();
        stats.setTotalBooks(bookRepository.count());
        stats.setAvailableBooks(Long.valueOf(bookRepository.findByStatus(BookStatus.AVAILABLE).size()));
        stats.setBorrowedBooks(Long.valueOf(bookRepository.findByStatus(BookStatus.BORROWED).size()));
        stats.setLostBooks(Long.valueOf(bookRepository.findByStatus(BookStatus.LOST).size()));
        stats.setDamagedBooks(Long.valueOf(bookRepository.findByStatus(BookStatus.DAMAGED).size()));
        return stats;
    }

    /**
     * Lấy danh sách sách nổi bật: ưu tiên sách được mượn nhiều nhất và còn available.
     * Nếu chưa đủ 10 sách available, bổ sung thêm sách mới nhất.
     */
    public List<BookDTO> getFeaturedBooks() {
        try {
            // Lấy tất cả sách AVAILABLE
            List<Book> availableBooks = bookRepository.findAvailableBooksForFeatured();
            // Sắp xếp theo số lần mượn (loans.size) giảm dần
            List<Book> sorted = availableBooks.stream()
                .sorted((b1, b2) -> Integer.compare(
                    b2.getLoans() != null ? b2.getLoans().size() : 0,
                    b1.getLoans() != null ? b1.getLoans().size() : 0))
                .collect(Collectors.toList());
            // Lấy tối đa 10 sách nổi bật
            List<Book> featured = sorted.stream().limit(10).collect(Collectors.toList());
            // Nếu chưa đủ 10, bổ sung sách mới nhất
            if (featured.size() < 10) {
                List<Book> recentAvailable = bookRepository.findRecentBooks(PageRequest.of(0, 10));
                for (Book b : recentAvailable) {
                    if (featured.stream().noneMatch(f -> f.getId().equals(b.getId()))) {
                        featured.add(b);
                        if (featured.size() >= 10) break;
                    }
                }
            }
            return featured.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("[ERROR] getFeaturedBooks: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<BookDTO> getRecentBooks(Pageable pageable) {
        return bookRepository.findRecentBooks(pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);
        
        stats.setTotalBooks(bookRepository.count());
        stats.setTotalAuthors(authorRepository.count());
        stats.setTotalCategories(categoryRepository.count());
        stats.setTotalPublishers(publisherRepository.count());
        stats.setTotalLoans(loanRepository.count());
        stats.setActiveLoans(Long.valueOf(loanRepository.countByReturnDateIsNull()));
        stats.setOverdueBooks(Long.valueOf(loanRepository.countByDueDateBeforeAndReturnDateIsNull(now)));
        
        Map<String, Long> monthlyBorrowings = loanRepository.findMonthlyBorrowingStats(6);
        stats.setMonthlyBorrowings(monthlyBorrowings);
        
        List<BookDTO> mostBorrowedBooks = bookRepository.findMostBorrowedBooks(
                sixMonthsAgo, 
                now, 
                PageRequest.of(0, 10))
                .getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        stats.setMostBorrowedBooks(mostBorrowedBooks);
        
        List<AuthorDTO> popularAuthors = authorRepository.findMostPopularAuthorsWithBorrowCount(PageRequest.of(0, 10))
                .getContent().stream()
                .map(author -> {
                    AuthorDTO dto = new AuthorDTO();
                    dto.setId(author.getId());
                    dto.setName(author.getName());
                    dto.setBiography(author.getBiography());
                    dto.setBirthDate(author.getBirthDate());
                    dto.setNationality(author.getNationality());
                    return dto;
                })
                .collect(Collectors.toList());
        stats.setMostPopularAuthors(popularAuthors);
        
        return stats;
    }

    public Page<BookDTO> getAllBooksWithFilters(
            String searchTerm,
            Long categoryId,
            BookStatus status,
            Long authorId,
            Long publisherId,
            Integer yearFrom,
            Integer yearTo,
            Pageable pageable) {
        java.time.LocalDate dateFrom = (yearFrom != null) ? java.time.LocalDate.of(yearFrom, 1, 1) : null;
        java.time.LocalDate dateTo = (yearTo != null) ? java.time.LocalDate.of(yearTo, 12, 31) : null;
        return bookRepository.findAllWithFilters(
                searchTerm, categoryId, status, authorId, publisherId, dateFrom, dateTo, pageable)
                .map(this::convertToDTO);
    }

    public Map<String, Long> getStatusSummary() {
        return bookRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    book -> book.getStatus().name(),
                    Collectors.counting()
                ));
    }

    public Page<BookDTO> getBooksByStatus(BookStatus status, Pageable pageable) {
        return bookRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    public Page<BookDTO> getBooksByCategory(Long categoryId, Pageable pageable) {
    // Lấy tất cả sách thuộc category, không lọc status
    return bookRepository.findByCategoryId(categoryId, pageable)
        .map(this::convertToDTO);
    }

    public Page<BookDTO> getBooksByAuthor(Long authorId, Pageable pageable) {
        return bookRepository.findByAuthorsId(authorId, pageable)
                .map(this::convertToDTO);
    }

    public Page<BookDTO> getBooksByPublisher(Long publisherId, Pageable pageable) {
        return bookRepository.findByPublisherId(publisherId, pageable)
                .map(this::convertToDTO);
    }

    public Page<BookDTO> searchBooksWithPagination(String query, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(query, pageable)
                .map(this::convertToDTO);
    }

    public List<String> getAuthorSuggestions(String query) {
        return authorRepository.findByNameContainingIgnoreCase(query).stream()
                .map(Author::getName)
                .collect(Collectors.toList());
    }

    public List<String> getPublisherSuggestions(String query) {
        return publisherRepository.findByNameContainingIgnoreCase(query).stream()
                .map(Publisher::getName)
                .collect(Collectors.toList());
    }

    public List<String> getCategorySuggestions(String query) {
        return categoryRepository.findByNameContainingIgnoreCase(query).stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }

    public List<String> getGenreSuggestions(String query) {
        return bookRepository.findDistinctGenres().stream()
                .filter(genre -> genre.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    // ============ EXISTING METHODS ============

    public boolean borrowBook(Long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.getAvailableCopies() > 0) {
                book.setAvailableCopies(book.getAvailableCopies() - 1);
                if (book.getAvailableCopies() == 0) {
                    book.setStatus(BookStatus.BORROWED);
                }
                bookRepository.save(book);
                return true;
            }
        }
        return false;
    }

    public boolean returnBook(Long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            if (book.getAvailableCopies() > 0) {
                book.setStatus(BookStatus.AVAILABLE);
            }
            bookRepository.save(book);
            return true;
        }
        return false;
    }

    public List<AuthorDTO> searchAuthorsByQuery(String query) {
        return authorRepository.findByNameContainingIgnoreCase(query.trim())
                .stream()
                .map(author -> {
                    AuthorDTO dto = new AuthorDTO();
                    dto.setId(author.getId());
                    dto.setName(author.getName());
                    dto.setBiography(author.getBiography());
                    dto.setBirthDate(author.getBirthDate());
                    dto.setNationality(author.getNationality());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<String> searchPublishersByQuery(String query) {
        return publisherRepository.findByNameContainingIgnoreCase(query.trim())
                .stream()
                .map(Publisher::getName)
                .collect(Collectors.toList());
    }

    public List<String> searchCategoriesByQuery(String query) {
        return categoryRepository.findByNameContainingIgnoreCase(query.trim())
                .stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }

    public List<String> searchGenresByQuery(String query) {
        return bookRepository.findDistinctGenres()
                .stream()
                .filter(genre -> genre.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Page<BookDTO> searchBooksByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::convertToDTO);
    }

    public List<BookDTO> searchBooksByAuthor(String authorName) {
        return bookRepository.findByAuthorsNameContainingIgnoreCase(authorName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookDTO> searchBooksByPublisher(String publisherName) {
        return bookRepository.findByPublisherNameContainingIgnoreCase(publisherName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookDTO> searchBooksByCategory(String categoryName) {
        return bookRepository.findByCategoryNameContainingIgnoreCase(categoryName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookDTO> searchBooksByGenre(String genre) {
        return bookRepository.findByGenreContainingIgnoreCase(genre)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookDTO> getAvailableBooks() {
        return bookRepository.findAvailableBooks().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<BookDTO> getAvailableBooksWithPagination(Pageable pageable) {
        return bookRepository.findByStatus(BookStatus.AVAILABLE, pageable)
                .map(this::convertToDTO);
    }

    // Helper methods
    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
    dto.setId(book.getId());
    dto.setTitle(book.getTitle());
    dto.setIsbn(book.getIsbn());
    dto.setAuthor(book.getAuthor());
        
        // Set publisher info
        if (book.getPublisher() != null) {
            dto.setPublisherId(book.getPublisher().getId());
            dto.setPublisherName(book.getPublisher().getName());
        }
        
        // Set category info
        if (book.getCategory() != null) {
            dto.setCategoryId(book.getCategory().getId());
            dto.setCategoryName(book.getCategory().getName());
        }
        
        // Convert publishedDate to publicationYear
        if (book.getPublishedDate() != null) {
            dto.setPublicationYear(book.getPublishedDate().getYear());
        }
        
        // Set author info
        if (book.getAuthors() != null) {
            Set<Long> authorIds = book.getAuthors().stream()
                .map(Author::getId)
                .collect(Collectors.toSet());
            dto.setAuthorIds(authorIds);
            
            Set<AuthorDTO> authorDTOs = book.getAuthors().stream()
                .map(author -> {
                    AuthorDTO authorDTO = new AuthorDTO();
                    authorDTO.setId(author.getId());
                    authorDTO.setName(author.getName());
                    authorDTO.setBiography(author.getBiography());
                    authorDTO.setBirthDate(author.getBirthDate());
                    authorDTO.setNationality(author.getNationality());
                    return authorDTO;
                })
                .collect(Collectors.toSet());
            dto.setAuthors(authorDTOs);
        }
        
        dto.setGenre(book.getGenre());
        dto.setStatus(book.getStatus());
        dto.setTotalCopies(book.getTotalCopies());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setDescription(book.getDescription());
        dto.setCoverImage(book.getImageUrl());
        
        return dto;
    }

    private Book convertToEntity(BookDTO dto) {
        Book book = new Book();
    book.setTitle(dto.getTitle());
    book.setIsbn(dto.getIsbn());
    book.setAuthor(dto.getAuthor());
        
        // Convert publicationYear to publishedDate
        if (dto.getPublicationYear() != null) {
            book.setPublishedDate(java.time.LocalDate.of(dto.getPublicationYear(), 1, 1));
        }
        
        book.setGenre(dto.getGenre());
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getTotalCopies());
        book.setDescription(dto.getDescription());
        book.setImageUrl(dto.getCoverImage());
        book.setStatus(dto.getStatus());
        
        return book;
    }

    private void updateBookFromDTO(Book book, BookDTO dto) {
    book.setTitle(dto.getTitle());
    book.setIsbn(dto.getIsbn());
    book.setAuthor(dto.getAuthor());
    book.setDescription(dto.getDescription());
    book.setImageUrl(dto.getCoverImage());
    book.setStatus(dto.getStatus());
    book.setTotalCopies(dto.getTotalCopies());
    book.setAvailableCopies(dto.getAvailableCopies());
        
        // Convert publicationYear to publishedDate
        if (dto.getPublicationYear() != null) {
            book.setPublishedDate(LocalDate.of(dto.getPublicationYear(), 1, 1));
        }

        // Update relationships
        if (dto.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                    .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
            book.setPublisher(publisher);
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            book.setCategory(category);
        }

        if (dto.getAuthorIds() != null && !dto.getAuthorIds().isEmpty()) {
            Set<Author> authors = dto.getAuthorIds().stream()
                    .map(id -> authorRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Author not found: " + id)))
                    .collect(Collectors.toSet());
            book.setAuthors(authors);
        }
        if (dto.getPublicationYear() != null) {
            book.setPublishedDate(java.time.LocalDate.of(dto.getPublicationYear(), 1, 1));
        }
        book.setGenre(dto.getGenre());
        book.setTotalCopies(dto.getTotalCopies());
        // Note: availableCopies should be updated carefully to maintain loan integrity
        if (dto.getAvailableCopies() != null) {
            book.setAvailableCopies(dto.getAvailableCopies());
        }
    }
}
