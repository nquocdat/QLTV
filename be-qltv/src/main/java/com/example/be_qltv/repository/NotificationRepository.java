package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Notification;
import com.example.be_qltv.entity.Patron;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find notifications by patron
    List<Notification> findByPatron(Patron patron);
    Page<Notification> findByPatron(Patron patron, Pageable pageable);
    
    // Find notifications by patron ID
    List<Notification> findByPatronId(Long patronId);
    Page<Notification> findByPatronId(Long patronId, Pageable pageable);
    
    // Find unread notifications
    List<Notification> findByPatronAndIsReadFalse(Patron patron);
    Page<Notification> findByPatronAndIsReadFalse(Patron patron, Pageable pageable);
    
    // Find unread notifications by patron ID
    List<Notification> findByPatronIdAndIsReadFalse(Long patronId);
    
    // Find notifications by type
    List<Notification> findByPatronAndType(Patron patron, Notification.NotificationType type);
    
    // Count unread notifications
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.patron = :patron AND n.isRead = false")
    Long countUnreadByPatron(@Param("patron") Patron patron);
    
    // Find notifications created after a certain date
    @Query("SELECT n FROM Notification n WHERE n.patron = :patron AND n.createdDate > :since ORDER BY n.createdDate DESC")
    List<Notification> findByPatronSince(@Param("patron") Patron patron, @Param("since") LocalDateTime since);
    
    // Find notifications by loan ID
    List<Notification> findByLoanId(Long loanId);
    
    // Find notifications by book ID
    List<Notification> findByBookId(Long bookId);
    
    // Delete old read notifications
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readDate < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}
