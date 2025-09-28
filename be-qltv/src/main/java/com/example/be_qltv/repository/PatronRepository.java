package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Patron;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatronRepository extends JpaRepository<Patron, Long> {
    
    // Find patron by email
    Optional<Patron> findByEmail(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find patrons by name (case insensitive)
    List<Patron> findByNameContainingIgnoreCase(String name);
    
    // Find patrons by role
    List<Patron> findByRole(Patron.Role role);
    
    // Find active patrons
    List<Patron> findByIsActiveTrue();
    
    // Find inactive patrons
    List<Patron> findByIsActiveFalse();
    
    // Find patrons by phone number
    Optional<Patron> findByPhoneNumber(String phoneNumber);
    
    // Search patrons by name, email, or phone
    @Query("SELECT p FROM Patron p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "p.phoneNumber LIKE CONCAT('%', :searchTerm, '%')")
    List<Patron> searchPatrons(@Param("searchTerm") String searchTerm);
    
    // Count active patrons
    @Query("SELECT COUNT(p) FROM Patron p WHERE p.isActive = true")
    Long countActivePatrons();
    
    // Count patrons by role
    @Query("SELECT COUNT(p) FROM Patron p WHERE p.role = :role")
    Long countByRole(@Param("role") Patron.Role role);
    
    // ============ PAGINATION METHODS ============
    
    // Find patrons by name with pagination
    Page<Patron> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Find patrons by role with pagination
    Page<Patron> findByRole(Patron.Role role, Pageable pageable);
    
    // Find active patrons with pagination
    Page<Patron> findByIsActiveTrue(Pageable pageable);
    
    // Find inactive patrons with pagination
    Page<Patron> findByIsActiveFalse(Pageable pageable);
    
    // Search patrons with pagination
    @Query("SELECT p FROM Patron p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "p.phoneNumber LIKE CONCAT('%', :searchTerm, '%')")
    Page<Patron> searchPatrons(@Param("searchTerm") String searchTerm, Pageable pageable);
}
