package com.example.be_qltv.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patron_id", nullable = false)
    private Patron patron;

    @NotNull(message = "Loan date is required")
    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.BORROWED;

    @Column(name = "fine_amount", precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Column(name = "is_renewed")
    private Boolean isRenewed = false;

    @Column(name = "renewal_count")
    private Integer renewalCount = 0;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public enum LoanStatus {
    BORROWED, RETURNED, OVERDUE, RENEWED, PENDING_RETURN
    }

    // Constructors
    public Loan() {
        this.createdDate = LocalDate.now();
        this.updatedDate = LocalDate.now();
        this.loanDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusWeeks(2); // Default 2 weeks loan period
    }

    public Loan(Book book, Patron patron) {
        this();
        this.book = book;
        this.patron = patron;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
        this.updatedDate = LocalDate.now();
    }

    public Patron getPatron() {
        return patron;
    }

    public void setPatron(Patron patron) {
        this.patron = patron;
        this.updatedDate = LocalDate.now();
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
        this.updatedDate = LocalDate.now();
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        this.updatedDate = LocalDate.now();
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
        this.updatedDate = LocalDate.now();
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
        this.updatedDate = LocalDate.now();
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
        this.updatedDate = LocalDate.now();
    }

    public Boolean getIsRenewed() {
        return isRenewed;
    }

    public void setIsRenewed(Boolean isRenewed) {
        this.isRenewed = isRenewed;
        this.updatedDate = LocalDate.now();
    }

    public Integer getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(Integer renewalCount) {
        this.renewalCount = renewalCount;
        this.updatedDate = LocalDate.now();
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

    // Helper methods
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && status == LoanStatus.BORROWED;
    }

    public long getDaysOverdue() {
        if (isOverdue()) {
            return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", book=" + (book != null ? book.getTitle() : null) +
                ", patron=" + (patron != null ? patron.getName() : null) +
                ", loanDate=" + loanDate +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}
