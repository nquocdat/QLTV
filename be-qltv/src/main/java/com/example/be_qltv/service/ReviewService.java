package com.example.be_qltv.service;

import com.example.be_qltv.dto.ReviewDTO;
import com.example.be_qltv.entity.Book;
import com.example.be_qltv.entity.Loan;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.entity.Review;
import com.example.be_qltv.repository.BookRepository;
import com.example.be_qltv.repository.LoanRepository;
import com.example.be_qltv.repository.PatronRepository;
import com.example.be_qltv.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PatronRepository patronRepository;

    @Autowired
    private LoanRepository loanRepository;

    /**
     * Check if a patron is eligible to review a book
     * (must have returned the book from a loan)
     */
    public boolean canPatronReviewBook(Long patronId, Long bookId) {
        // Check if patron has a returned loan for this book
        List<Loan> returnedLoans = loanRepository.findByPatronIdAndBookId(patronId, bookId);
        
        boolean hasReturnedLoan = returnedLoans.stream()
                .anyMatch(loan -> loan.getStatus() == Loan.LoanStatus.RETURNED);
        
        if (!hasReturnedLoan) {
            return false;
        }
        
        // Check if patron already reviewed this book
        Optional<Review> existingReview = reviewRepository.findByBookIdAndPatronId(bookId, patronId);
        return existingReview.isEmpty();
    }

    /**
     * Create a new review (requires approval)
     */
    public ReviewDTO createReview(Long patronId, Long bookId, Integer rating, String comment) {
        if (!canPatronReviewBook(patronId, bookId)) {
            throw new IllegalStateException("You are not eligible to review this book. You must have returned it first.");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new IllegalArgumentException("Patron not found"));

        // Find the most recent returned loan for this book and patron
        List<Loan> returnedLoans = loanRepository.findByPatronIdAndBookId(patronId, bookId);
        Loan loan = returnedLoans.stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.RETURNED)
                .findFirst()
                .orElse(null);

        Review review = new Review();
        review.setBook(book);
        review.setPatron(patron);
        review.setRating(rating);
        review.setComment(comment);
        review.setApproved(false); // Requires admin approval
        review.setLoan(loan);

        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }

    /**
     * Get all reviews (for admin)
     */
    public Page<ReviewDTO> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Review> reviews = reviewRepository.findAll(pageable);
        return reviews.map(this::convertToDTO);
    }

    /**
     * Get pending reviews (not approved)
     */
    public Page<ReviewDTO> getPendingReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Review> reviews = reviewRepository.findByApproved(false, pageable);
        return reviews.map(this::convertToDTO);
    }

    /**
     * Get approved reviews
     */
    public Page<ReviewDTO> getApprovedReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Review> reviews = reviewRepository.findByApproved(true, pageable);
        return reviews.map(this::convertToDTO);
    }

    /**
     * Get approved reviews for a book
     */
    public List<ReviewDTO> getApprovedReviewsForBook(Long bookId) {
        List<Review> reviews = reviewRepository.findApprovedByBookId(bookId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get book rating statistics
     */
    public Map<String, Object> getBookRatingStats(Long bookId) {
        Map<String, Object> stats = new HashMap<>();
        
        Double avgRating = reviewRepository.getApprovedAverageRatingForBook(bookId);
        Long reviewCount = reviewRepository.getApprovedReviewCountForBook(bookId);
        
        stats.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        stats.put("reviewCount", reviewCount != null ? reviewCount : 0);
        
        return stats;
    }

    /**
     * Approve a review
     */
    public ReviewDTO approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        review.setApproved(true);
        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }

    /**
     * Reject/Delete a review
     */
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    /**
     * Get patron's reviews
     */
    public List<ReviewDTO> getPatronReviews(Long patronId) {
        List<Review> reviews = reviewRepository.findByPatronId(patronId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get review for a specific loan
     */
    public Optional<ReviewDTO> getReviewByLoanId(Long loanId) {
        Optional<Review> review = reviewRepository.findByLoanId(loanId);
        return review.map(this::convertToDTO);
    }

    /**
     * Convert Review entity to DTO
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setBookId(review.getBook().getId());
        dto.setBookTitle(review.getBook().getTitle());
        dto.setPatronId(review.getPatron().getId());
        dto.setPatronName(review.getPatron().getName());
        dto.setLoanId(review.getLoan() != null ? review.getLoan().getId() : null);
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setApproved(review.getApproved());
        dto.setCreatedDate(review.getCreatedDate());
        dto.setUpdatedDate(review.getUpdatedDate());
        return dto;
    }
}
