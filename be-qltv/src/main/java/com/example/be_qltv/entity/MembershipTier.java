package com.example.be_qltv.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "membership_tiers")
public class MembershipTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TierLevel level;

    @Column(name = "max_books", nullable = false)
    private Integer maxBooks = 3;

    @Column(name = "loan_duration_days", nullable = false)
    private Integer loanDurationDays = 14;

    @Column(name = "late_fee_discount", precision = 5, scale = 2)
    private BigDecimal lateFeeDiscount = BigDecimal.ZERO;

    @Column(name = "reservation_priority")
    private Boolean reservationPriority = false;

    @Column(name = "early_access")
    private Boolean earlyAccess = false;

    @Column(name = "min_loans_required")
    private Integer minLoansRequired = 0;

    @Column(name = "min_points_required")
    private Integer minPointsRequired = 0;

    @Column(name = "max_violations_allowed")
    private Integer maxViolationsAllowed = 5;

    @Column(length = 50)
    private String color = "#6B7280";

    @Column(length = 50)
    private String icon = "user";

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    public enum TierLevel {
        BASIC, VIP, PREMIUM
    }

    // Constructors
    public MembershipTier() {}

    public MembershipTier(String name, TierLevel level, Integer maxBooks, Integer loanDurationDays) {
        this.name = name;
        this.level = level;
        this.maxBooks = maxBooks;
        this.loanDurationDays = loanDurationDays;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TierLevel getLevel() { return level; }
    public void setLevel(TierLevel level) { this.level = level; }

    public Integer getMaxBooks() { return maxBooks; }
    public void setMaxBooks(Integer maxBooks) { this.maxBooks = maxBooks; }

    public Integer getLoanDurationDays() { return loanDurationDays; }
    public void setLoanDurationDays(Integer loanDurationDays) { this.loanDurationDays = loanDurationDays; }

    public BigDecimal getLateFeeDiscount() { return lateFeeDiscount; }
    public void setLateFeeDiscount(BigDecimal lateFeeDiscount) { this.lateFeeDiscount = lateFeeDiscount; }

    public Boolean getReservationPriority() { return reservationPriority; }
    public void setReservationPriority(Boolean reservationPriority) { this.reservationPriority = reservationPriority; }

    public Boolean getEarlyAccess() { return earlyAccess; }
    public void setEarlyAccess(Boolean earlyAccess) { this.earlyAccess = earlyAccess; }

    public Integer getMinLoansRequired() { return minLoansRequired; }
    public void setMinLoansRequired(Integer minLoansRequired) { this.minLoansRequired = minLoansRequired; }

    public Integer getMinPointsRequired() { return minPointsRequired; }
    public void setMinPointsRequired(Integer minPointsRequired) { this.minPointsRequired = minPointsRequired; }

    public Integer getMaxViolationsAllowed() { return maxViolationsAllowed; }
    public void setMaxViolationsAllowed(Integer maxViolationsAllowed) { this.maxViolationsAllowed = maxViolationsAllowed; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
