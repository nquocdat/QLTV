package com.example.be_qltv.repository;

import com.example.be_qltv.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    
    Optional<UserMembership> findByPatronId(Long patronId);
    
    List<UserMembership> findByTierId(Long tierId);
    
    @Query("SELECT um FROM UserMembership um WHERE um.totalLoans >= :minLoans")
    List<UserMembership> findUsersEligibleForUpgrade(Integer minLoans);
    
    @Query("SELECT COUNT(um) FROM UserMembership um WHERE um.tierId = :tierId")
    Long countByTierId(Long tierId);
    
    @Query("SELECT um FROM UserMembership um ORDER BY um.currentPoints DESC")
    List<UserMembership> findTopUsersByPoints();
}
