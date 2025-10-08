package com.example.be_qltv.dto;

import com.example.be_qltv.entity.BookCopy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for BookCopy entity
 */
public class BookCopyDTO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private String bookCoverImage;
    private Integer copyNumber;
    private String barcode;
    private String conditionStatus;
    private String conditionStatusDisplay;
    private String status;
    private String statusDisplay;
    private String location;
    private LocalDate acquisitionDate;
    private BigDecimal price;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public BookCopyDTO() {
    }

    public BookCopyDTO(BookCopy copy) {
        this.id = copy.getId();
        this.copyNumber = copy.getCopyNumber();
        this.barcode = copy.getBarcode();
        this.conditionStatus = copy.getConditionStatus() != null ? 
            copy.getConditionStatus().name() : null;
        this.conditionStatusDisplay = copy.getConditionStatus() != null ? 
            copy.getConditionStatus().getDisplayName() : null;
        this.status = copy.getStatus() != null ? copy.getStatus().name() : null;
        this.statusDisplay = copy.getStatus() != null ? copy.getStatus().getDisplayName() : null;
        this.location = copy.getLocation();
        this.acquisitionDate = copy.getAcquisitionDate();
        this.price = copy.getPrice();
        this.notes = copy.getNotes();
        this.createdAt = copy.getCreatedAt();
        this.updatedAt = copy.getUpdatedAt();

        // Book info
        if (copy.getBook() != null) {
            this.bookId = copy.getBook().getId();
            this.bookTitle = copy.getBook().getTitle();
            this.bookAuthor = copy.getBook().getAuthor();
            this.bookIsbn = copy.getBook().getIsbn();
            this.bookCoverImage = copy.getBook().getImageUrl();
        }
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

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public String getBookCoverImage() {
        return bookCoverImage;
    }

    public void setBookCoverImage(String bookCoverImage) {
        this.bookCoverImage = bookCoverImage;
    }

    public Integer getCopyNumber() {
        return copyNumber;
    }

    public void setCopyNumber(Integer copyNumber) {
        this.copyNumber = copyNumber;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(String conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public String getConditionStatusDisplay() {
        return conditionStatusDisplay;
    }

    public void setConditionStatusDisplay(String conditionStatusDisplay) {
        this.conditionStatusDisplay = conditionStatusDisplay;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
