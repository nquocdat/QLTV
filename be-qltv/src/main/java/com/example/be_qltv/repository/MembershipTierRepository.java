package com.example.be_qltv.repository;

import com.example.be_qltv.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {
    
    Optional<MembershipTier> findByLevel(MembershipTier.TierLevel level);
    
    List<MembershipTier> findAllByOrderByMinLoansRequiredAsc();
    
    @Query("SELECT mt FROM MembershipTier mt WHERE mt.minLoansRequired <= :totalLoans " +
           "AND mt.minPointsRequired <= :points ORDER BY mt.minLoansRequired DESC")
    List<MembershipTier> findEligibleTiers(Integer totalLoans, Integer points);
    
    @Query("SELECT mt FROM MembershipTier mt WHERE mt.minLoansRequired > :currentLoans " +
           "OR mt.minPointsRequired > :currentPoints ORDER BY mt.minLoansRequired ASC LIMIT 1")
    Optional<MembershipTier> findNextTier(Integer currentLoans, Integer currentPoints);
}
