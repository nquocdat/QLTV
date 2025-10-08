package com.example.be_qltv.service;

import com.example.be_qltv.dto.LoanDTO;
import com.example.be_qltv.entity.Book;
import com.example.be_qltv.entity.BookCopy;
import com.example.be_qltv.entity.Loan;
import com.example.be_qltv.entity.LoanPayment;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.repository.BookRepository;
import com.example.be_qltv.repository.LoanRepository;
import com.example.be_qltv.repository.LoanPaymentRepository;
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
    
    @Autowired
    private LoanPaymentRepository loanPaymentRepository;
    
    @Autowired
    private BookCopyService bookCopyService;

    @Autowired
    private MembershipService membershipService;

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
    
    /**
     * Mượn sách với thanh toán (CASH hoặc VNPAY)
     * Tạo loan với status PENDING_PAYMENT và tạo payment record
     * SỬ DỤNG BOOK COPY SYSTEM
     */
    public LoanDTO borrowBookWithPayment(Long bookId, Long patronId, String paymentMethod) {
        System.out.println("LoanService.borrowBookWithPayment - START");
        System.out.println("BookId: " + bookId + ", PatronId: " + patronId + ", PaymentMethod: " + paymentMethod);
        
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        Optional<Patron> patronOpt = patronRepository.findById(patronId);

        if (bookOpt.isEmpty() || patronOpt.isEmpty()) {
            System.out.println("ERROR: Book or Patron not found");
            throw new RuntimeException("Book or Patron not found");
        }

        Book book = bookOpt.get();
        Patron patron = patronOpt.get();
        System.out.println("Found book: " + book.getTitle() + ", patron: " + patron.getName());

        // Check if patron has overdue loans
        if (loanRepository.hasOverdueLoans(patron)) {
            System.out.println("ERROR: Patron has overdue loans");
            throw new RuntimeException("Patron has overdue loans. Cannot borrow new books.");
        }

        // Lấy copy available đầu tiên
        Long availableCount = bookCopyService.countAvailableCopies(bookId);
        System.out.println("Available copies count: " + availableCount);
        if (availableCount <= 0) {
            System.out.println("ERROR: No available copy");
            throw new RuntimeException("No available copy for this book");
        }

        BookCopy availableCopy = bookCopyService.getAvailableCopyEntity(bookId);
        System.out.println("Got available copy: " + availableCopy.getBarcode());

        // Check if patron already has this book on loan
        Optional<Loan> existingLoan = loanRepository.findCurrentLoan(book, patron);
        if (existingLoan.isPresent()) {
            System.out.println("ERROR: Patron already has this book on loan");
            throw new RuntimeException("Patron already has this book on loan");
        }

        // Create new loan with PENDING_PAYMENT status
        System.out.println("Creating loan...");
        Loan loan = new Loan(book, patron);
        loan.setBookCopy(availableCopy); // Gán copy cụ thể
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusWeeks(2)); // 2 weeks loan period
        loan.setStatus(Loan.LoanStatus.PENDING_PAYMENT); // Chờ thanh toán
        
        System.out.println("Saving loan...");
        Loan savedLoan = loanRepository.save(loan);
        System.out.println("Saved loan with ID: " + savedLoan.getId());
        
        // Increment loan count in membership
        try {
            membershipService.incrementLoanCount(patron.getId());
        } catch (Exception e) {
            System.err.println("Failed to increment loan count: " + e.getMessage());
        }
        
        // Create payment record
        System.out.println("Creating payment...");
        LoanPayment payment = new LoanPayment();
        payment.setLoan(savedLoan);
        payment.setPatron(patron);
        payment.setAmount(new BigDecimal("50000")); // 50,000 VND phí đặt cọc
        
        try {
            payment.setPaymentMethod(LoanPayment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Invalid payment method");
            throw new RuntimeException("Invalid payment method: " + paymentMethod);
        }
        
        payment.setPaymentStatus(LoanPayment.PaymentStatus.PENDING);
        payment.setDescription("Phí đặt cọc mượn sách: " + book.getTitle());
        
        System.out.println("Saving payment...");
        loanPaymentRepository.save(payment);
        System.out.println("Payment saved successfully");
        
        // Note: Copy status sẽ được cập nhật khi payment được confirmed
        // Không cập nhật ngay lập tức
        
        System.out.println("Converting to DTO...");
        LoanDTO dto = convertToDTO(savedLoan);
        System.out.println("LoanService.borrowBookWithPayment - DONE");
        return dto;
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

        // Đặt ngày trả và tính phí quá hạn tự động
        LocalDate today = LocalDate.now();
        loan.setReturnDate(today);
        
        // Tính phí phạt quá hạn
        if (today.isAfter(loan.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), today);
            BigDecimal finePerDay = new BigDecimal("5000"); // 5,000 VND/ngày
            BigDecimal totalFine = finePerDay.multiply(BigDecimal.valueOf(daysOverdue));
            loan.setFineAmount(totalFine);
            loan.setStatus(Loan.LoanStatus.RETURNED); // Vẫn đặt RETURNED, nhưng có fine
        } else {
            loan.setStatus(Loan.LoanStatus.RETURNED);
        }
        
        // Cập nhật copy status về AVAILABLE
        if (loan.getBookCopy() != null) {
            bookCopyService.updateCopyStatus(loan.getBookCopy().getId(), BookCopy.CopyStatus.AVAILABLE);
        }
        
        // Update book availability
        bookService.returnBook(loan.getBook().getId());
        
        // Award points for on-time return and update membership stats
        try {
            if (!today.isAfter(loan.getDueDate())) {
                // On-time return: award 10 points
                membershipService.addPoints(loan.getPatron().getId(), 10);
            } else {
                // Late return: increment violation count
                membershipService.incrementViolationCount(loan.getPatron().getId());
            }
        } catch (Exception e) {
            // Log but don't fail if membership service unavailable
            System.err.println("Failed to update membership: " + e.getMessage());
        }
        
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

    /**
     * Trả sách với phí phạt hỏng sách
     * Tính tổng phí = phí quá hạn + phí hỏng sách
     */
    public LoanDTO returnBookWithDamageFine(Long loanId, BigDecimal damageFine, String damageNotes) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new RuntimeException("Loan not found");
        }

        Loan loan = loanOpt.get();
        LocalDate today = LocalDate.now();
        loan.setReturnDate(today);
        
        // Tính phí quá hạn
        BigDecimal overdueFine = BigDecimal.ZERO;
        if (today.isAfter(loan.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), today);
            BigDecimal finePerDay = new BigDecimal("5000"); // 5,000 VND/ngày
            overdueFine = finePerDay.multiply(BigDecimal.valueOf(daysOverdue));
        }
        
        // Tổng phí = quá hạn + hỏng sách
        BigDecimal totalFine = overdueFine.add(damageFine != null ? damageFine : BigDecimal.ZERO);
        loan.setFineAmount(totalFine);
        loan.setStatus(Loan.LoanStatus.RETURNED);
        
        // Cập nhật trạng thái copy
        if (loan.getBookCopy() != null) {
            BookCopy copy = loan.getBookCopy();
            
            if (damageFine != null && damageFine.compareTo(BigDecimal.ZERO) > 0) {
                // Sách bị hỏng - cập nhật notes và status
                String existingNotes = copy.getNotes() != null ? copy.getNotes() + " | " : "";
                copy.setNotes(existingNotes + "Hỏng khi trả (" + today + "): " + 
                    (damageNotes != null ? damageNotes : "Không có ghi chú"));
                // Set status thành REPAIRING
                bookCopyService.updateCopyStatus(copy.getId(), BookCopy.CopyStatus.REPAIRING);
            } else {
                // Sách bình thường - trả về AVAILABLE
                bookCopyService.updateCopyStatus(copy.getId(), BookCopy.CopyStatus.AVAILABLE);
            }
        }
        
        // Update book availability
        bookService.returnBook(loan.getBook().getId());
        
        Loan savedLoan = loanRepository.save(loan);
        return convertToDTO(savedLoan);
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
