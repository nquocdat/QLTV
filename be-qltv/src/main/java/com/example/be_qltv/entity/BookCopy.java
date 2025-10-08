package com.example.be_qltv.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho bản sao vật lý của sách
 * Mỗi Book có thể có nhiều BookCopy
 */
@Entity
@Table(name = "book_copies", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "copy_number"}))
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @NotNull(message = "Book is required")
    private Book book;

    @NotNull(message = "Copy number is required")
    @Min(value = 1, message = "Copy number must be at least 1")
    @Column(name = "copy_number", nullable = false)
    private Integer copyNumber;

    @NotBlank(message = "Barcode is required")
    @Size(max = 50, message = "Barcode must not exceed 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", nullable = false)
    private ConditionStatus conditionStatus = ConditionStatus.GOOD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CopyStatus status = CopyStatus.AVAILABLE;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Column(length = 100)
    private String location = "Kho chính";

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @DecimalMin(value = "0.0", message = "Price must be positive")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum ConditionStatus {
        NEW("Mới"),
        GOOD("Tốt"),
        FAIR("Khá"),
        POOR("Cũ"),
        DAMAGED("Hỏng");

        private final String displayName;

        ConditionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CopyStatus {
        AVAILABLE("Có sẵn"),
        BORROWED("Đang mượn"),
        RESERVED("Đã đặt trước"),
        LOST("Thất lạc"),
        REPAIRING("Đang sửa chữa");

        private final String displayName;

        CopyStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (acquisitionDate == null) {
            acquisitionDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public BookCopy() {
    }

    public BookCopy(Book book, Integer copyNumber, String barcode) {
        this.book = book;
        this.copyNumber = copyNumber;
        this.barcode = barcode;
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

    public ConditionStatus getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(ConditionStatus conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
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

    // Helper methods
    public boolean isAvailable() {
        return status == CopyStatus.AVAILABLE;
    }

    public boolean isBorrowed() {
        return status == CopyStatus.BORROWED;
    }

    public String getFullIdentifier() {
        return book != null ? book.getTitle() + " - Copy #" + copyNumber : "Copy #" + copyNumber;
    }

    @Override
    public String toString() {
        return "BookCopy{" +
                "id=" + id +
                ", copyNumber=" + copyNumber +
                ", barcode='" + barcode + '\'' +
                ", status=" + status +
                ", condition=" + conditionStatus +
                ", location='" + location + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookCopy)) return false;
        BookCopy bookCopy = (BookCopy) o;
        return id != null && id.equals(bookCopy.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
