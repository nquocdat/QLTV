package com.example.be_qltv.dto;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long patronId;
    private String patronName;
    private Long loanId;
    private Integer rating;
    private String comment;
    private Boolean approved;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Constructors
    public ReviewDTO() {}

    public ReviewDTO(Long id, Long bookId, String bookTitle, Long patronId, String patronName, 
                     Long loanId, Integer rating, String comment, Boolean approved, 
                     LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.patronId = patronId;
        this.patronName = patronName;
        this.loanId = loanId;
        this.rating = rating;
        this.comment = comment;
        this.approved = approved;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
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

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
