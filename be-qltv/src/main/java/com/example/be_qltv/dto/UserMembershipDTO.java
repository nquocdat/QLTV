package com.example.be_qltv.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserMembershipDTO {
    private Long id;
    private Long userId;
    private Long tierId;
    private Integer currentPoints;
    private Integer totalLoans;
    private Integer violationCount;
    private LocalDate joinDate;
    private LocalDate upgradeDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Double nextTierProgress;
    
    // Additional fields for frontend
    private MembershipTierDTO tier;
    private String userName;
    private String userEmail;

    // Constructors
    public UserMembershipDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTierId() { return tierId; }
    public void setTierId(Long tierId) { this.tierId = tierId; }

    public Integer getCurrentPoints() { return currentPoints; }
    public void setCurrentPoints(Integer currentPoints) { this.currentPoints = currentPoints; }

    public Integer getTotalLoans() { return totalLoans; }
    public void setTotalLoans(Integer totalLoans) { this.totalLoans = totalLoans; }

    public Integer getViolationCount() { return violationCount; }
    public void setViolationCount(Integer violationCount) { this.violationCount = violationCount; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public LocalDate getUpgradeDate() { return upgradeDate; }
    public void setUpgradeDate(LocalDate upgradeDate) { this.upgradeDate = upgradeDate; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public Double getNextTierProgress() { return nextTierProgress; }
    public void setNextTierProgress(Double nextTierProgress) { this.nextTierProgress = nextTierProgress; }

    public MembershipTierDTO getTier() { return tier; }
    public void setTier(MembershipTierDTO tier) { this.tier = tier; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
