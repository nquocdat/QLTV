package com.example.be_qltv.service;

import com.example.be_qltv.dto.MembershipTierDTO;
import com.example.be_qltv.dto.UserMembershipDTO;
import com.example.be_qltv.entity.MembershipTier;
import com.example.be_qltv.entity.UserMembership;
import com.example.be_qltv.repository.MembershipTierRepository;
import com.example.be_qltv.repository.PatronRepository;
import com.example.be_qltv.repository.UserMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MembershipService {

    @Autowired
    private MembershipTierRepository membershipTierRepository;

    @Autowired
    private UserMembershipRepository userMembershipRepository;

    @Autowired
    private PatronRepository patronRepository;

    // ==================== MEMBERSHIP TIER OPERATIONS ====================
    
    public List<MembershipTierDTO> getAllTiers() {
        return membershipTierRepository.findAllByOrderByMinLoansRequiredAsc()
                .stream()
                .map(this::convertTierToDTO)
                .collect(Collectors.toList());
    }

    public MembershipTierDTO getTierById(Long id) {
        return membershipTierRepository.findById(id)
                .map(this::convertTierToDTO)
                .orElseThrow(() -> new RuntimeException("Membership tier not found with id: " + id));
    }

    public MembershipTierDTO getTierByLevel(String level) {
        MembershipTier.TierLevel tierLevel = MembershipTier.TierLevel.valueOf(level.toUpperCase());
        return membershipTierRepository.findByLevel(tierLevel)
                .map(this::convertTierToDTO)
                .orElseThrow(() -> new RuntimeException("Membership tier not found with level: " + level));
    }

    // ==================== USER MEMBERSHIP OPERATIONS ====================
    
    public UserMembershipDTO getUserMembership(Long userId) {
        try {
            Optional<UserMembership> membership = userMembershipRepository.findByPatronId(userId);
            
            if (membership.isPresent()) {
                return convertMembershipToDTO(membership.get());
            } else {
                // Auto-create default BASIC membership if not exists
                System.out.println("User " + userId + " does not have membership, creating default BASIC membership");
                return createDefaultMembership(userId);
            }
        } catch (Exception e) {
            System.err.println("Error getting user membership: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get or create membership for user: " + userId, e);
        }
    }

    public UserMembershipDTO createDefaultMembership(Long userId) {
        // Verify user exists
        patronRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get BASIC tier
        MembershipTier basicTier = membershipTierRepository.findByLevel(MembershipTier.TierLevel.BASIC)
                .orElseThrow(() -> new RuntimeException("BASIC tier not found"));

        // Create new membership
        UserMembership membership = new UserMembership(userId, basicTier.getId());
        membership.setJoinDate(LocalDate.now());
        membership = userMembershipRepository.save(membership);

        return convertMembershipToDTO(membership);
    }

    public UserMembershipDTO updateMembership(Long userId, UserMembershipDTO dto) {
        UserMembership membership = userMembershipRepository.findByPatronId(userId)
                .orElseThrow(() -> new RuntimeException("Membership not found for user: " + userId));

        if (dto.getCurrentPoints() != null) {
            membership.setCurrentPoints(dto.getCurrentPoints());
        }
        if (dto.getTotalLoans() != null) {
            membership.setTotalLoans(dto.getTotalLoans());
        }
        if (dto.getViolationCount() != null) {
            membership.setViolationCount(dto.getViolationCount());
        }

        membership = userMembershipRepository.save(membership);
        return convertMembershipToDTO(membership);
    }

    public UserMembershipDTO upgradeMembership(Long userId, Long newTierId) {
        UserMembership membership = userMembershipRepository.findByPatronId(userId)
                .orElseThrow(() -> new RuntimeException("Membership not found for user: " + userId));

        // Verify tier exists
        membershipTierRepository.findById(newTierId)
                .orElseThrow(() -> new RuntimeException("Tier not found with id: " + newTierId));

        membership.setTierId(newTierId);
        membership.setUpgradeDate(LocalDate.now());
        membership = userMembershipRepository.save(membership);

        return convertMembershipToDTO(membership);
    }

    // ==================== POINTS & STATS OPERATIONS ====================
    
    public void addPoints(Long userId, Integer points) {
        UserMembership membership = userMembershipRepository.findByPatronId(userId)
                .orElseThrow(() -> new RuntimeException("Membership not found for user: " + userId));

        membership.setCurrentPoints(membership.getCurrentPoints() + points);
        userMembershipRepository.save(membership);
        
        // Check for auto-upgrade
        checkAndAutoUpgrade(membership);
    }

    public void incrementLoanCount(Long userId) {
        UserMembership membership = userMembershipRepository.findByPatronId(userId)
                .orElseThrow(() -> new RuntimeException("Membership not found for user: " + userId));

        membership.setTotalLoans(membership.getTotalLoans() + 1);
        userMembershipRepository.save(membership);
        
        // Award points for borrowing
        addPoints(userId, 5); // 5 points per loan
    }

    public void incrementViolationCount(Long userId) {
        UserMembership membership = userMembershipRepository.findByPatronId(userId)
                .orElseThrow(() -> new RuntimeException("Membership not found for user: " + userId));

        membership.setViolationCount(membership.getViolationCount() + 1);
        userMembershipRepository.save(membership);
        
        // Check if violations exceed tier limit
        checkViolationLimit(membership);
    }

    // ==================== AUTO UPGRADE LOGIC ====================
    
    private void checkAndAutoUpgrade(UserMembership membership) {
        MembershipTier currentTier = membershipTierRepository.findById(membership.getTierId())
                .orElse(null);
        
        if (currentTier == null) return;

        // Find next eligible tier
        Optional<MembershipTier> nextTier = membershipTierRepository.findNextTier(
                membership.getTotalLoans(), 
                membership.getCurrentPoints()
        );

        if (nextTier.isPresent()) {
            MembershipTier next = nextTier.get();
            
            // Check if user meets all requirements
            if (membership.getTotalLoans() >= next.getMinLoansRequired() &&
                membership.getCurrentPoints() >= next.getMinPointsRequired() &&
                membership.getViolationCount() <= next.getMaxViolationsAllowed()) {
                
                // Auto upgrade
                membership.setTierId(next.getId());
                membership.setUpgradeDate(LocalDate.now());
                userMembershipRepository.save(membership);
            }
        }
    }

    private void checkViolationLimit(UserMembership membership) {
        MembershipTier currentTier = membershipTierRepository.findById(membership.getTierId())
                .orElse(null);
        
        if (currentTier == null) return;

        // If violations exceed limit, downgrade to BASIC
        if (membership.getViolationCount() > currentTier.getMaxViolationsAllowed()) {
            MembershipTier basicTier = membershipTierRepository.findByLevel(MembershipTier.TierLevel.BASIC)
                    .orElse(null);
            
            if (basicTier != null && !currentTier.getLevel().equals(MembershipTier.TierLevel.BASIC)) {
                membership.setTierId(basicTier.getId());
                membership.setUpgradeDate(LocalDate.now());
                userMembershipRepository.save(membership);
            }
        }
    }

    // ==================== STATISTICS ====================
    
    public List<UserMembershipDTO> getAllUserMemberships() {
        return userMembershipRepository.findAll()
                .stream()
                .map(this::convertMembershipToDTO)
                .collect(Collectors.toList());
    }

    public Long getTierMemberCount(Long tierId) {
        return userMembershipRepository.countByTierId(tierId);
    }

    // ==================== CONVERSION METHODS ====================
    
    private MembershipTierDTO convertTierToDTO(MembershipTier tier) {
        MembershipTierDTO dto = new MembershipTierDTO();
        dto.setId(tier.getId());
        dto.setName(tier.getName());
        dto.setLevel(tier.getLevel().toString());
        dto.setMaxBooks(tier.getMaxBooks());
        dto.setLoanDurationDays(tier.getLoanDurationDays());
        dto.setLateFeeDiscount(tier.getLateFeeDiscount());
        dto.setReservationPriority(tier.getReservationPriority());
        dto.setEarlyAccess(tier.getEarlyAccess());
        dto.setMinLoansRequired(tier.getMinLoansRequired());
        dto.setMinPointsRequired(tier.getMinPointsRequired());
        dto.setMaxViolationsAllowed(tier.getMaxViolationsAllowed());
        dto.setColor(tier.getColor());
        dto.setIcon(tier.getIcon());
        dto.setCreatedDate(tier.getCreatedDate());

        // Convert benefits
        List<MembershipTierDTO.MembershipBenefitDTO> benefits = new ArrayList<>();
        benefits.add(new MembershipTierDTO.MembershipBenefitDTO(
                "MAX_BOOKS", tier.getMaxBooks(), "Mượn tối đa " + tier.getMaxBooks() + " cuốn cùng lúc"));
        benefits.add(new MembershipTierDTO.MembershipBenefitDTO(
                "LOAN_DURATION", tier.getLoanDurationDays(), "Thời gian mượn " + tier.getLoanDurationDays() + " ngày"));
        benefits.add(new MembershipTierDTO.MembershipBenefitDTO(
                "LATE_FEE_DISCOUNT", tier.getLateFeeDiscount(), "Giảm " + tier.getLateFeeDiscount() + "% phí phạt trễ hạn"));
        
        if (tier.getReservationPriority()) {
            benefits.add(new MembershipTierDTO.MembershipBenefitDTO(
                    "RESERVATION_PRIORITY", true, "Ưu tiên đặt trước sách"));
        }
        if (tier.getEarlyAccess()) {
            benefits.add(new MembershipTierDTO.MembershipBenefitDTO(
                    "EARLY_ACCESS", true, "Truy cập sớm sách mới"));
        }
        dto.setBenefits(benefits);

        // Convert requirements
        MembershipTierDTO.MembershipRequirementDTO requirements = new MembershipTierDTO.MembershipRequirementDTO(
                tier.getMinLoansRequired(),
                tier.getMinPointsRequired(),
                tier.getMaxViolationsAllowed()
        );
        dto.setRequirements(requirements);

        return dto;
    }

    private UserMembershipDTO convertMembershipToDTO(UserMembership membership) {
        UserMembershipDTO dto = new UserMembershipDTO();
        dto.setId(membership.getId());
        dto.setUserId(membership.getPatronId());
        dto.setTierId(membership.getTierId());
        dto.setCurrentPoints(membership.getCurrentPoints());
        dto.setTotalLoans(membership.getTotalLoans());
        dto.setViolationCount(membership.getViolationCount());
        dto.setJoinDate(membership.getJoinDate());
        dto.setUpgradeDate(membership.getUpgradeDate());
        dto.setCreatedDate(membership.getCreatedDate());
        dto.setUpdatedDate(membership.getUpdatedDate());

        // Add tier info
        if (membership.getTier() != null) {
            dto.setTier(convertTierToDTO(membership.getTier()));
        }

        // Add user info
        if (membership.getPatron() != null) {
            dto.setUserName(membership.getPatron().getName());
            dto.setUserEmail(membership.getPatron().getEmail());
        }

        // Calculate progress to next tier
        dto.setNextTierProgress(calculateNextTierProgress(membership));

        return dto;
    }

    private Double calculateNextTierProgress(UserMembership membership) {
        Optional<MembershipTier> nextTier = membershipTierRepository.findNextTier(
                membership.getTotalLoans(), 
                membership.getCurrentPoints()
        );

        if (nextTier.isPresent()) {
            MembershipTier next = nextTier.get();
            MembershipTier current = membershipTierRepository.findById(membership.getTierId()).orElse(null);
            
            if (current == null) return 0.0;

            // Calculate based on loans requirement
            int currentLoans = membership.getTotalLoans();
            int requiredLoans = next.getMinLoansRequired();
            int previousRequiredLoans = current.getMinLoansRequired();
            
            if (requiredLoans > previousRequiredLoans) {
                double progress = ((double) (currentLoans - previousRequiredLoans) / 
                                  (requiredLoans - previousRequiredLoans)) * 100;
                return Math.min(Math.max(progress, 0), 100);
            }
        }

        return 0.0;
    }
}
