package com.example.be_qltv.controller;

import com.example.be_qltv.dto.ReviewDTO;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.repository.PatronRepository;
import com.example.be_qltv.service.ReviewService;
import com.example.be_qltv.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:4200")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PatronRepository patronRepository;

    /**
     * Check if current user can review a book
     */
    @GetMapping("/can-review/{bookId}")
    @PreAuthorize("hasAnyRole('PATRON', 'ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Boolean>> canReviewBook(
            @PathVariable Long bookId,
            @RequestHeader("Authorization") String token) {
        
        String username = jwtUtil.getUserNameFromJwtToken(token.substring(7));
        Patron patron = patronRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean canReview = reviewService.canPatronReviewBook(patron.getId(), bookId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("canReview", canReview);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new review
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PATRON', 'ADMIN', 'LIBRARIAN')")
    public ResponseEntity<?> createReview(
            @RequestBody Map<String, Object> reviewData,
            @RequestHeader("Authorization") String token) {
        
        try {
            String username = jwtUtil.getUserNameFromJwtToken(token.substring(7));
            Patron patron = patronRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Long bookId = Long.valueOf(reviewData.get("bookId").toString());
            Integer rating = Integer.valueOf(reviewData.get("rating").toString());
            String comment = reviewData.get("comment") != null ? reviewData.get("comment").toString() : "";

            ReviewDTO review = reviewService.createReview(patron.getId(), bookId, rating, comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đánh giá của bạn đã được gửi và đang chờ duyệt");
            response.put("review", review);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi tạo đánh giá: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get approved reviews for a book (public)
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getBookReviews(@PathVariable Long bookId) {
        List<ReviewDTO> reviews = reviewService.getApprovedReviewsForBook(bookId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get book rating statistics (public)
     */
    @GetMapping("/book/{bookId}/stats")
    public ResponseEntity<Map<String, Object>> getBookRatingStats(@PathVariable Long bookId) {
        Map<String, Object> stats = reviewService.getBookRatingStats(bookId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all reviews (admin only)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewDTO>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ReviewDTO> reviews = reviewService.getAllReviews(page, size);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get pending reviews (admin only)
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewDTO>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ReviewDTO> reviews = reviewService.getPendingReviews(page, size);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get approved reviews (admin only)
     */
    @GetMapping("/admin/approved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewDTO>> getApprovedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ReviewDTO> reviews = reviewService.getApprovedReviews(page, size);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Approve a review (admin only)
     */
    @PutMapping("/admin/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveReview(@PathVariable Long reviewId) {
        try {
            ReviewDTO review = reviewService.approveReview(reviewId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đánh giá đã được phê duyệt");
            response.put("review", review);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi phê duyệt: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete a review (admin only)
     */
    @DeleteMapping("/admin/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đánh giá đã được xóa");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi xóa: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get current user's reviews
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("hasAnyRole('PATRON', 'ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<ReviewDTO>> getMyReviews(
            @RequestHeader("Authorization") String token) {
        
        String username = jwtUtil.getUserNameFromJwtToken(token.substring(7));
        Patron patron = patronRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<ReviewDTO> reviews = reviewService.getPatronReviews(patron.getId());
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get review for a specific loan
     */
    @GetMapping("/loan/{loanId}")
    @PreAuthorize("hasAnyRole('PATRON', 'ADMIN', 'LIBRARIAN')")
    public ResponseEntity<?> getReviewByLoan(@PathVariable Long loanId) {
        return reviewService.getReviewByLoanId(loanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
