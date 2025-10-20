package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Book;
import com.example.be_qltv.enums.BookStatus;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
       @Query("SELECT b as book, COUNT(l.id) as borrowCount FROM Book b LEFT JOIN b.loans l GROUP BY b ORDER BY COUNT(l.id) DESC")
       Page<Object[]> findBooksOrderByBorrowCountDesc(Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.status = :status")
    Long countByStatus(@Param("status") com.example.be_qltv.enums.BookStatus status);

    @Query("SELECT b, COUNT(l) as borrowCount " +
           "FROM Book b " +
           "LEFT JOIN b.loans l " +
           "WHERE (:startDate IS NULL OR l.loanDate >= :startDate) " +
           "AND (:endDate IS NULL OR l.loanDate <= :endDate) " +
           "GROUP BY b " +
           "ORDER BY COUNT(l) DESC")
    Page<Book> findMostBorrowedBooks(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT b FROM Book b " +
           "WHERE b.status = 'AVAILABLE' " +
           "ORDER BY b.createdDate DESC")
    List<Book> findRecentBooks(Pageable pageable);

       @Query("SELECT b FROM Book b " +
                 "WHERE (:searchTerm IS NULL OR " +
                 "       LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                 "       b.isbn LIKE CONCAT('%', :searchTerm, '%') OR " +
                 "       EXISTS (SELECT a FROM b.authors a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))) " +
                 "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
                 "AND (:status IS NULL OR b.status = :status) " +
                 "AND (:authorId IS NULL OR EXISTS (SELECT a FROM b.authors a WHERE a.id = :authorId)) " +
                 "AND (:publisherId IS NULL OR b.publisher.id = :publisherId) " +
                 "AND (:dateFrom IS NULL OR b.publishedDate >= :dateFrom) " +
                 "AND (:dateTo IS NULL OR b.publishedDate <= :dateTo) " +
                 "ORDER BY b.createdDate DESC")
       Page<Book> findAllWithFilters(
              @Param("searchTerm") String searchTerm,
              @Param("categoryId") Long categoryId,
              @Param("status") BookStatus status,
              @Param("authorId") Long authorId,
              @Param("publisherId") Long publisherId,
              @Param("dateFrom") java.time.LocalDate dateFrom,
              @Param("dateTo") java.time.LocalDate dateTo,
              Pageable pageable
       );

    // Trả về tất cả sách AVAILABLE, sẽ sort ở tầng service
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE'")
    List<Book> findAvailableBooksForFeatured();

    @Query("SELECT b FROM Book b WHERE b.status = :status")
    Page<Book> findByStatus(@Param("status") BookStatus status, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.category.id = :categoryId")
    Page<Book> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    Page<Book> findByAuthor(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.publisher.id = :publisherId")
    Page<Book> findByPublisher(@Param("publisherId") Long publisherId, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> searchByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE'")
    List<Book> findAvailableBooks();

    @Query("SELECT b FROM Book b WHERE b.availableCopies <= :threshold")
    List<Book> findBooksWithLowStock(@Param("threshold") int threshold);

    @Query("SELECT DISTINCT b.genre FROM Book b")
    List<String> findDistinctGenres();

    @Query("SELECT b FROM Book b WHERE b.category.id = :categoryId")
    Page<Book> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    Page<Book> findByAuthorsId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.publisher.id = :publisherId")
    Page<Book> findByPublisherId(@Param("publisherId") Long publisherId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))")
    List<Book> findByGenreContainingIgnoreCase(@Param("genre") String genre);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorsNameContainingIgnoreCase(@Param("authorName") String authorName);

    @Query("SELECT b FROM Book b WHERE LOWER(b.publisher.name) LIKE LOWER(CONCAT('%', :publisherName, '%'))")
    List<Book> findByPublisherNameContainingIgnoreCase(@Param("publisherName") String publisherName);

    @Query("SELECT b FROM Book b WHERE LOWER(b.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))")
    List<Book> findByCategoryNameContainingIgnoreCase(@Param("categoryName") String categoryName);

    @Query("SELECT b FROM Book b WHERE b.status = :status")
    List<Book> findByStatus(@Param("status") BookStatus status);

}
