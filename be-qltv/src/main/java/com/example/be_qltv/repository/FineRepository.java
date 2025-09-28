package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    
    List<Fine> findByPatronId(Long patronId);
    
    List<Fine> findByStatus(String status);
    
    Page<Fine> findByPatronId(Long patronId, Pageable pageable);
    
    Page<Fine> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT f FROM Fine f WHERE f.patron.id = :patronId AND f.status = :status")
    List<Fine> findByPatronIdAndStatus(@Param("patronId") Long patronId, @Param("status") String status);
    
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.patron.id = :patronId AND f.status = 'UNPAID'")
    BigDecimal getTotalUnpaidFinesByPatron(@Param("patronId") Long patronId);
    
    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.status = 'UNPAID'")
    BigDecimal getTotalUnpaidFines();
    
    @Query("SELECT COUNT(f) FROM Fine f WHERE f.status = 'UNPAID'")
    Long countUnpaidFines();
    
    @Query("SELECT f FROM Fine f WHERE f.loan.id = :loanId")
    List<Fine> findByLoanId(@Param("loanId") Long loanId);
}
