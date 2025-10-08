package com.example.be_qltv.repository;

import com.example.be_qltv.entity.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    /**
     * Tìm tất cả copies của một book
     */
    List<BookCopy> findByBookId(Long bookId);

    /**
     * Tìm tất cả copies có sẵn của một book
     */
    List<BookCopy> findByBookIdAndStatus(Long bookId, BookCopy.CopyStatus status);

    /**
     * Tìm copy available đầu tiên của một book
     * Sử dụng Spring Data JPA method naming để tự động thêm LIMIT 1
     */
    Optional<BookCopy> findFirstByBookIdAndStatusOrderByCopyNumberAsc(Long bookId, BookCopy.CopyStatus status);

    /**
     * Đếm số lượng copies available của một book
     */
    @Query("SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book.id = :bookId AND bc.status = 'AVAILABLE'")
    Long countAvailableCopiesByBookId(@Param("bookId") Long bookId);

    /**
     * Tìm theo barcode
     */
    Optional<BookCopy> findByBarcode(String barcode);

    /**
     * Check barcode đã tồn tại chưa
     */
    boolean existsByBarcode(String barcode);

    /**
     * Tìm copies theo status
     */
    List<BookCopy> findByStatus(BookCopy.CopyStatus status);

    /**
     * Tìm copies theo condition
     */
    List<BookCopy> findByConditionStatus(BookCopy.ConditionStatus conditionStatus);

    /**
     * Tìm copies theo location
     */
    List<BookCopy> findByLocation(String location);

    /**
     * Lấy copy number cao nhất của một book
     */
    @Query("SELECT COALESCE(MAX(bc.copyNumber), 0) FROM BookCopy bc WHERE bc.book.id = :bookId")
    Integer findMaxCopyNumberByBookId(@Param("bookId") Long bookId);

    /**
     * Tìm tất cả copies của book với thông tin đầy đủ
     */
    @Query("SELECT bc FROM BookCopy bc " +
           "LEFT JOIN FETCH bc.book b " +
           "LEFT JOIN FETCH b.publisher " +
           "LEFT JOIN FETCH b.category " +
           "WHERE bc.book.id = :bookId " +
           "ORDER BY bc.copyNumber ASC")
    List<BookCopy> findByBookIdWithDetails(@Param("bookId") Long bookId);

    /**
     * Tìm copies theo nhiều criteria
     */
    @Query("SELECT bc FROM BookCopy bc WHERE " +
           "(:bookId IS NULL OR bc.book.id = :bookId) AND " +
           "(:status IS NULL OR bc.status = :status) AND " +
           "(:conditionStatus IS NULL OR bc.conditionStatus = :conditionStatus) AND " +
           "(:location IS NULL OR bc.location LIKE %:location%)")
    List<BookCopy> findByCriteria(
        @Param("bookId") Long bookId,
        @Param("status") BookCopy.CopyStatus status,
        @Param("conditionStatus") BookCopy.ConditionStatus conditionStatus,
        @Param("location") String location
    );

    /**
     * Thống kê số lượng copies theo status cho một book
     */
    @Query("SELECT bc.status, COUNT(bc) FROM BookCopy bc WHERE bc.book.id = :bookId GROUP BY bc.status")
    List<Object[]> countCopiesByStatusForBook(@Param("bookId") Long bookId);

    /**
     * Tìm copies cần sửa chữa hoặc thay thế
     */
    @Query("SELECT bc FROM BookCopy bc WHERE bc.conditionStatus IN ('POOR', 'DAMAGED') OR bc.status = 'REPAIRING'")
    List<BookCopy> findCopiesNeedingMaintenance();
}
