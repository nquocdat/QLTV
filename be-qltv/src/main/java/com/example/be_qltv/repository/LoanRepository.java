package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Loan;
import com.example.be_qltv.entity.Book;
import com.example.be_qltv.entity.Patron;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    // Find loans by patron
    List<Loan> findByPatron(Patron patron);
    
    // Find loans by patron ID
    List<Loan> findByPatronId(Long patronId);
    
    // Find loans by book
    List<Loan> findByBook(Book book);
    
    // Find loans by book ID
    List<Loan> findByBookId(Long bookId);
    
    // Find loans by status
    List<Loan> findByStatus(Loan.LoanStatus status);
    
    // Find active loans (borrowed status)
    @Query("SELECT l FROM Loan l WHERE l.status = 'BORROWED'")
    List<Loan> findActiveLoan();
    
    // Find overdue loans
    @Query("SELECT l FROM Loan l WHERE l.status = 'BORROWED' AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();
    
    // Find loans due today
    @Query("SELECT l FROM Loan l WHERE l.status = 'BORROWED' AND l.dueDate = CURRENT_DATE")
    List<Loan> findLoansDueToday();
    
    // Find loans by patron and status
    List<Loan> findByPatronAndStatus(Patron patron, Loan.LoanStatus status);
    
    // Find current loan for a book and patron
    @Query("SELECT l FROM Loan l WHERE l.book = :book AND l.patron = :patron AND l.status = 'BORROWED'")
    Optional<Loan> findCurrentLoan(@Param("book") Book book, @Param("patron") Patron patron);
    
    // Count active loans by patron
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.patron = :patron AND l.status = 'BORROWED'")
    Long countActiveLoansByPatron(@Param("patron") Patron patron);
    
    // Find loans with fines
    @Query("SELECT l FROM Loan l WHERE l.fineAmount > 0")
    List<Loan> findLoansWithFines();
    
    // Check if patron has overdue loans
    @Query("SELECT COUNT(l) > 0 FROM Loan l WHERE l.patron = :patron AND l.status = 'BORROWED' AND l.dueDate < CURRENT_DATE")
    boolean hasOverdueLoans(@Param("patron") Patron patron);
    
    // ============ PAGINATION METHODS ============
    
    // Find loans by patron with pagination
    Page<Loan> findByPatron(Patron patron, Pageable pageable);
    
    // Find loans by patron ID with pagination
    Page<Loan> findByPatronId(Long patronId, Pageable pageable);
    
    // Find loans by book with pagination
    Page<Loan> findByBook(Book book, Pageable pageable);
    
    // Find loans by status with pagination
    Page<Loan> findByStatus(Loan.LoanStatus status, Pageable pageable);
    
    // Find active loans with pagination
    @Query("SELECT l FROM Loan l WHERE l.status = 'BORROWED'")
    Page<Loan> findActiveLoans(Pageable pageable);
    
    // Find overdue loans with pagination
    @Query("SELECT l FROM Loan l WHERE l.status = 'BORROWED' AND l.dueDate < CURRENT_DATE")
    Page<Loan> findOverdueLoans(Pageable pageable);
    
    // Find loans with fines with pagination
    @Query("SELECT l FROM Loan l WHERE l.fineAmount > 0")
    Page<Loan> findLoansWithFines(Pageable pageable);
    
    // ============ REPORT METHODS ============
    
    // Find most borrowed books for reports
    @Query("SELECT l.book, COUNT(l) as borrowCount FROM Loan l GROUP BY l.book ORDER BY borrowCount DESC")
    List<Object[]> findMostBorrowedBooks();
    
    // Find returns by specific date
    @Query("SELECT l FROM Loan l WHERE l.returnDate = :date")
    List<Loan> findReturnsByDate(@Param("date") LocalDate date);
    
    // Find loans by date range for reports
    @Query("SELECT l FROM Loan l WHERE l.loanDate BETWEEN :startDate AND :endDate")
    List<Loan> findLoansByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Patron loan history sorted by date
    @Query("SELECT l FROM Loan l WHERE l.patron = :patron ORDER BY l.loanDate DESC")
    List<Loan> findPatronLoanHistory(@Param("patron") Patron patron);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnDate IS NULL")
    long countByReturnDateIsNull();
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.dueDate < :date AND l.returnDate IS NULL")
    long countByDueDateBeforeAndReturnDateIsNull(@Param("date") LocalDate date);

    @Query(value = """
        SELECT SUBSTRING(DATE_FORMAT(l.borrow_date, '%b'), 1, 3) as month, COUNT(*) as count 
        FROM loans l 
        WHERE l.borrow_date >= DATE_SUB(CURDATE(), INTERVAL :months MONTH) 
        GROUP BY month 
        ORDER BY l.borrow_date DESC
    """, nativeQuery = true)
    Map<String, Long> findMonthlyBorrowingStats(@Param("months") int months);
}
