package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByBookId(Long bookId);
    
    List<Review> findByPatronId(Long patronId);
    
    Page<Review> findByBookId(Long bookId, Pageable pageable);
    
    Page<Review> findByPatronId(Long patronId, Pageable pageable);
    
    Optional<Review> findByBookIdAndPatronId(Long bookId, Long patronId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingForBook(@Param("bookId") Long bookId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId")
    Long getReviewCountForBook(@Param("bookId") Long bookId);
    
    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating ORDER BY r.createdDate DESC")
    List<Review> findByRatingGreaterThanEqual(@Param("minRating") Integer minRating);
    
    @Query("SELECT r FROM Review r ORDER BY r.createdDate DESC")
    List<Review> findRecentReviews(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.book.id = :bookId ORDER BY r.rating DESC, r.createdDate DESC")
    List<Review> findByBookIdOrderByRatingDesc(@Param("bookId") Long bookId);
}
