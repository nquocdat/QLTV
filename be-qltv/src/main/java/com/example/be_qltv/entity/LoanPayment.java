package com.example.be_qltv.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_payments")
public class LoanPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patron_id", nullable = false)
    private Patron patron;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 10)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "transaction_no", length = 255)
    private String transactionNo;
    
    @Column(name = "bank_code", length = 50)
    private String bankCode;
    
    @Column(name = "vnpay_response_code", length = 10)
    private String vnpayResponseCode;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private Patron confirmedBy;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    // Enums
    public enum PaymentMethod {
        CASH,
        VNPAY
    }
    
    public enum PaymentStatus {
        PENDING,      // Chờ xác nhận (Cash) hoặc chờ thanh toán (VNPay)
        CONFIRMED,    // Đã xác nhận/thanh toán thành công
        FAILED,       // Thanh toán thất bại
        REFUNDED      // Đã hoàn tiền
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
    
    // Constructors
    public LoanPayment() {
    }
    
    public LoanPayment(Loan loan, Patron patron, BigDecimal amount, PaymentMethod paymentMethod) {
        this.loan = loan;
        this.patron = patron;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.PENDING;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Loan getLoan() {
        return loan;
    }
    
    public void setLoan(Loan loan) {
        this.loan = loan;
    }
    
    public Patron getPatron() {
        return patron;
    }
    
    public void setPatron(Patron patron) {
        this.patron = patron;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getTransactionNo() {
        return transactionNo;
    }
    
    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getVnpayResponseCode() {
        return vnpayResponseCode;
    }
    
    public void setVnpayResponseCode(String vnpayResponseCode) {
        this.vnpayResponseCode = vnpayResponseCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getConfirmedDate() {
        return confirmedDate;
    }
    
    public void setConfirmedDate(LocalDateTime confirmedDate) {
        this.confirmedDate = confirmedDate;
    }
    
    public Patron getConfirmedBy() {
        return confirmedBy;
    }
    
    public void setConfirmedBy(Patron confirmedBy) {
        this.confirmedBy = confirmedBy;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
