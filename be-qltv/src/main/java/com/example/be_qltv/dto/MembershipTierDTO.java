package com.example.be_qltv.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MembershipTierDTO {
    private Long id;
    private String name;
    private String level;
    private Integer maxBooks;
    private Integer loanDurationDays;
    private BigDecimal lateFeeDiscount;
    private Boolean reservationPriority;
    private Boolean earlyAccess;
    private Integer minLoansRequired;
    private Integer minPointsRequired;
    private Integer maxViolationsAllowed;
    private String color;
    private String icon;
    private LocalDateTime createdDate;
    private List<MembershipBenefitDTO> benefits;
    private MembershipRequirementDTO requirements;

    // Nested classes
    public static class MembershipBenefitDTO {
        private String type;
        private Object value;
        private String description;

        public MembershipBenefitDTO() {}

        public MembershipBenefitDTO(String type, Object value, String description) {
            this.type = type;
            this.value = value;
            this.description = description;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class MembershipRequirementDTO {
        private Integer minLoans;
        private Integer minPoints;
        private Integer maxViolations;

        public MembershipRequirementDTO() {}

        public MembershipRequirementDTO(Integer minLoans, Integer minPoints, Integer maxViolations) {
            this.minLoans = minLoans;
            this.minPoints = minPoints;
            this.maxViolations = maxViolations;
        }

        // Getters and Setters
        public Integer getMinLoans() { return minLoans; }
        public void setMinLoans(Integer minLoans) { this.minLoans = minLoans; }

        public Integer getMinPoints() { return minPoints; }
        public void setMinPoints(Integer minPoints) { this.minPoints = minPoints; }

        public Integer getMaxViolations() { return maxViolations; }
        public void setMaxViolations(Integer maxViolations) { this.maxViolations = maxViolations; }
    }

    // Constructors
    public MembershipTierDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

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

    public List<MembershipBenefitDTO> getBenefits() { return benefits; }
    public void setBenefits(List<MembershipBenefitDTO> benefits) { this.benefits = benefits; }

    public MembershipRequirementDTO getRequirements() { return requirements; }
    public void setRequirements(MembershipRequirementDTO requirements) { this.requirements = requirements; }
}
