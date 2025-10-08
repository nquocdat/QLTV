package com.example.be_qltv.repository;

import com.example.be_qltv.entity.LoanPayment;
import com.example.be_qltv.entity.Patron;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {
    
    /**
     * Tìm payment theo loan ID
     */
    Optional<LoanPayment> findByLoanId(Long loanId);
    
    /**
     * Tìm tất cả payments của một patron
     */
    List<LoanPayment> findByPatronId(Long patronId);
    
    /**
     * Tìm payment đang pending theo patron ID
     */
    @Query("SELECT lp FROM LoanPayment lp WHERE lp.patron.id = :patronId AND lp.paymentStatus = 'PENDING'")
    List<LoanPayment> findPendingPaymentsByPatronId(@Param("patronId") Long patronId);
    
    /**
     * Tìm tất cả cash payments chờ xác nhận
     */
    @Query("SELECT lp FROM LoanPayment lp WHERE lp.paymentMethod = 'CASH' AND lp.paymentStatus = 'PENDING' ORDER BY lp.createdDate DESC")
    List<LoanPayment> findPendingCashPayments();
    
    /**
     * Tìm payment theo transaction number (VNPay)
     */
    Optional<LoanPayment> findByTransactionNo(String transactionNo);
    
    /**
     * Tìm tất cả VNPay payments
     */
    @Query("SELECT lp FROM LoanPayment lp WHERE lp.paymentMethod = 'VNPAY' ORDER BY lp.createdDate DESC")
    List<LoanPayment> findAllVNPayPayments();
    
    /**
     * Tìm payments theo status
     */
    @Query("SELECT lp FROM LoanPayment lp WHERE lp.paymentStatus = :status ORDER BY lp.createdDate DESC")
    List<LoanPayment> findByPaymentStatus(@Param("status") LoanPayment.PaymentStatus status);
    
    /**
     * Tìm payments theo method
     */
    @Query("SELECT lp FROM LoanPayment lp WHERE lp.paymentMethod = :method ORDER BY lp.createdDate DESC")
    List<LoanPayment> findByPaymentMethod(@Param("method") LoanPayment.PaymentMethod method);
    
    /**
     * Đếm số lượng payments chờ xác nhận
     */
    @Query("SELECT COUNT(lp) FROM LoanPayment lp WHERE lp.paymentStatus = 'PENDING'")
    Long countPendingPayments();
    
    /**
     * Tìm payments đã được xác nhận bởi một admin/librarian
     */
    List<LoanPayment> findByConfirmedBy(Patron confirmedBy);
}
