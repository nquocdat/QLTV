package com.example.be_qltv.service;

import com.example.be_qltv.dto.LoanDTO;
import com.example.be_qltv.entity.Book;
import com.example.be_qltv.entity.Loan;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.repository.BookRepository;
import com.example.be_qltv.repository.LoanRepository;
import com.example.be_qltv.repository.PatronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PatronRepository patronRepository;

    @Autowired
    private BookService bookService;

    public List<LoanDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<LoanDTO> getLoanById(Long id) {
        return loanRepository.findById(id)
                .map(this::convertToDTO);
    }

    public List<LoanDTO> getLoansByPatronId(Long patronId) {
        return loanRepository.findByPatronId(patronId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LoanDTO> getLoansByBookId(Long bookId) {
        return loanRepository.findByBookId(bookId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LoanDTO> getActiveLoans() {
        return loanRepository.findActiveLoan().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LoanDTO> getOverdueLoans() {
        List<Loan> overdueLoans = loanRepository.findOverdueLoans();
        // Update status and calculate fines for overdue loans
        for (Loan loan : overdueLoans) {
            if (loan.getStatus() == Loan.LoanStatus.BORROWED) {
                loan.setStatus(Loan.LoanStatus.OVERDUE);
                calculateFine(loan);
                loanRepository.save(loan);
            }
        }
        
        return overdueLoans.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public LoanDTO borrowBook(Long bookId, Long patronId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        Optional<Patron> patronOpt = patronRepository.findById(patronId);

        if (bookOpt.isEmpty() || patronOpt.isEmpty()) {
            throw new RuntimeException("Book or Patron not found");
        }

        Book book = bookOpt.get();
        Patron patron = patronOpt.get();

        // Check if patron has overdue loans
        if (loanRepository.hasOverdueLoans(patron)) {
            throw new RuntimeException("Patron has overdue loans. Cannot borrow new books.");
        }

        // Check if book is available
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book is not available for loan");
        }

        // Check if patron already has this book on loan
        Optional<Loan> existingLoan = loanRepository.findCurrentLoan(book, patron);
        if (existingLoan.isPresent()) {
            throw new RuntimeException("Patron already has this book on loan");
        }

        // Create new loan
        Loan loan = new Loan(book, patron);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusWeeks(2)); // 2 weeks loan period
        loan.setStatus(Loan.LoanStatus.BORROWED);

        // Update book availability
        bookService.borrowBook(bookId);

        Loan savedLoan = loanRepository.save(loan);
        return convertToDTO(savedLoan);
    }

    public LoanDTO returnBook(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new RuntimeException("Loan not found");
        }

        Loan loan = loanOpt.get();
        if (loan.getStatus() != Loan.LoanStatus.BORROWED && loan.getStatus() != Loan.LoanStatus.OVERDUE) {
            throw new RuntimeException("Book is already returned or not eligible for return");
        }

        // Khi người dùng nhấn trả, chuyển sang trạng thái chờ xác nhận
        loan.setStatus(Loan.LoanStatus.PENDING_RETURN);
        Loan savedLoan = loanRepository.save(loan);
        return convertToDTO(savedLoan);
    }

    public LoanDTO renewLoan(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new RuntimeException("Loan not found");
        }

        Loan loan = loanOpt.get();
        if (loan.getStatus() != Loan.LoanStatus.BORROWED) {
            throw new RuntimeException("Only borrowed books can be renewed");
        }

        if (loan.getRenewalCount() >= 2) { // Maximum 2 renewals
            throw new RuntimeException("Maximum renewal limit reached");
        }

        // Check if patron has overdue loans
        if (loanRepository.hasOverdueLoans(loan.getPatron())) {
            throw new RuntimeException("Cannot renew loan. Patron has overdue loans.");
        }

        // Renew loan
        loan.setDueDate(loan.getDueDate().plusWeeks(2)); // Extend by 2 weeks
        loan.setIsRenewed(true);
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        loan.setStatus(Loan.LoanStatus.RENEWED);

        Loan savedLoan = loanRepository.save(loan);
        return convertToDTO(savedLoan);
    }

    public List<LoanDTO> getPatronLoanHistory(Long patronId) {
        Optional<Patron> patronOpt = patronRepository.findById(patronId);
        if (patronOpt.isEmpty()) {
            throw new RuntimeException("Patron not found");
        }

        return loanRepository.findPatronLoanHistory(patronOpt.get()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LoanDTO> getLoansWithFines() {
        return loanRepository.findLoansWithFines().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public LoanDTO confirmReturnBook(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new RuntimeException("Loan not found");
        }
        Loan loan = loanOpt.get();
        if (loan.getStatus() != Loan.LoanStatus.PENDING_RETURN) {
            throw new RuntimeException("Loan is not pending return");
        }
        loan.setReturnDate(LocalDate.now());
        loan.setStatus(Loan.LoanStatus.RETURNED);
        if (loan.getReturnDate().isAfter(loan.getDueDate())) {
            calculateFine(loan);
        }
        bookService.returnBook(loan.getBook().getId());
        Loan savedLoan = loanRepository.save(loan);
        return convertToDTO(savedLoan);
    }
    private void calculateFine(Loan loan) {
        LocalDate dueDate = loan.getDueDate();
        LocalDate returnDate = loan.getReturnDate() != null ? loan.getReturnDate() : LocalDate.now();
        
        if (returnDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
            BigDecimal finePerDay = new BigDecimal("1.00"); // $1 per day fine
            BigDecimal totalFine = finePerDay.multiply(new BigDecimal(daysOverdue));
            loan.setFineAmount(totalFine);
        }
    }

    // Helper methods
    private LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setBookId(loan.getBook().getId());
        dto.setBookTitle(loan.getBook().getTitle());
        // Get first author's name or empty string if no authors
        dto.setBookAuthor(loan.getBook().getAuthors().stream()
            .map(author -> author.getName())
            .findFirst()
            .orElse(""));
        dto.setPatronId(loan.getPatron().getId());
        dto.setPatronName(loan.getPatron().getName());
        dto.setPatronEmail(loan.getPatron().getEmail());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnDate(loan.getReturnDate());
        dto.setStatus(loan.getStatus().name());
        dto.setFineAmount(loan.getFineAmount());
        dto.setIsRenewed(loan.getIsRenewed());
        dto.setRenewalCount(loan.getRenewalCount());
        return dto;
    }
}
