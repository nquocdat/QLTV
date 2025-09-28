package com.example.be_qltv.dto;

import java.time.LocalDate;
import java.math.BigDecimal;

public class LoanDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private Long patronId;
    private String patronName;
    private String patronEmail;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;
    private BigDecimal fineAmount;
    private Boolean isRenewed;
    private Integer renewalCount;

    // Constructors
    public LoanDTO() {}

    public LoanDTO(Long bookId, Long patronId) {
        this.bookId = bookId;
        this.patronId = patronId;
        this.loanDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusWeeks(2);
        this.status = "BORROWED";
        this.fineAmount = BigDecimal.ZERO;
        this.isRenewed = false;
        this.renewalCount = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public Long getPatronId() {
        return patronId;
    }

    public void setPatronId(Long patronId) {
        this.patronId = patronId;
    }

    public String getPatronName() {
        return patronName;
    }

    public void setPatronName(String patronName) {
        this.patronName = patronName;
    }

    public String getPatronEmail() {
        return patronEmail;
    }

    public void setPatronEmail(String patronEmail) {
        this.patronEmail = patronEmail;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public Boolean getIsRenewed() {
        return isRenewed;
    }

    public void setIsRenewed(Boolean isRenewed) {
        this.isRenewed = isRenewed;
    }

    public Integer getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(Integer renewalCount) {
        this.renewalCount = renewalCount;
    }
}
