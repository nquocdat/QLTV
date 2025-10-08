package com.example.be_qltv.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_memberships")
public class UserMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patron_id", nullable = false)
    private Long patronId;

    @Column(name = "tier_id", nullable = false)
    private Long tierId;

    @Column(name = "current_points")
    private Integer currentPoints = 0;

    @Column(name = "total_loans")
    private Integer totalLoans = 0;

    @Column(name = "violation_count")
    private Integer violationCount = 0;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "upgrade_date")
    private LocalDate upgradeDate;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patron_id", insertable = false, updatable = false)
    private Patron patron;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tier_id", insertable = false, updatable = false)
    private MembershipTier tier;

    // Constructors
    public UserMembership() {
        this.joinDate = LocalDate.now();
    }

    public UserMembership(Long patronId, Long tierId) {
        this();
        this.patronId = patronId;
        this.tierId = tierId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatronId() { return patronId; }
    public void setPatronId(Long patronId) { 
        this.patronId = patronId;
        this.updatedDate = LocalDateTime.now();
    }

    public Long getTierId() { return tierId; }
    public void setTierId(Long tierId) { 
        this.tierId = tierId;
        this.updatedDate = LocalDateTime.now();
    }

    public Integer getCurrentPoints() { return currentPoints; }
    public void setCurrentPoints(Integer currentPoints) { 
        this.currentPoints = currentPoints;
        this.updatedDate = LocalDateTime.now();
    }

    public Integer getTotalLoans() { return totalLoans; }
    public void setTotalLoans(Integer totalLoans) { 
        this.totalLoans = totalLoans;
        this.updatedDate = LocalDateTime.now();
    }

    public Integer getViolationCount() { return violationCount; }
    public void setViolationCount(Integer violationCount) { 
        this.violationCount = violationCount;
        this.updatedDate = LocalDateTime.now();
    }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public LocalDate getUpgradeDate() { return upgradeDate; }
    public void setUpgradeDate(LocalDate upgradeDate) { this.upgradeDate = upgradeDate; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public Patron getPatron() { return patron; }
    public void setPatron(Patron patron) { this.patron = patron; }

    public MembershipTier getTier() { return tier; }
    public void setTier(MembershipTier tier) { this.tier = tier; }
}
