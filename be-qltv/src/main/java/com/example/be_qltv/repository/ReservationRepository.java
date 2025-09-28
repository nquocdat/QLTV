package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByPatronId(Long patronId);
    
    List<Reservation> findByBookId(Long bookId);
    
    List<Reservation> findByStatus(String status);
    
    Page<Reservation> findByPatronId(Long patronId, Pageable pageable);
    
    Page<Reservation> findByBookId(Long bookId, Pageable pageable);
    
    Page<Reservation> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT r FROM Reservation r WHERE r.patron.id = :patronId AND r.status = :status")
    List<Reservation> findByPatronIdAndStatus(@Param("patronId") Long patronId, @Param("status") String status);
    
    @Query("SELECT r FROM Reservation r WHERE r.book.id = :bookId AND r.status = :status ORDER BY r.reservationDate ASC")
    List<Reservation> findByBookIdAndStatusOrderByReservationDate(@Param("bookId") Long bookId, @Param("status") String status);
    
    @Query("SELECT r FROM Reservation r WHERE r.expiryDate < :date AND r.status = 'ACTIVE'")
    List<Reservation> findExpiredReservations(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book.id = :bookId AND r.status = 'ACTIVE'")
    Long countActiveReservationsByBook(@Param("bookId") Long bookId);
    
    @Query("SELECT r FROM Reservation r WHERE r.patron.id = :patronId AND r.book.id = :bookId AND r.status = 'ACTIVE'")
    List<Reservation> findActiveReservationByPatronAndBook(@Param("patronId") Long patronId, @Param("bookId") Long bookId);
}
