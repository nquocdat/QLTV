package com.example.be_qltv.controller;

import com.example.be_qltv.dto.LoanPaymentDTO;
import com.example.be_qltv.service.LoanPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/loan-payments")
public class LoanPaymentController {
    
    @Autowired
    private LoanPaymentService loanPaymentService;
    
    /**
     * Lấy tất cả loan payments (Admin/Librarian)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<LoanPaymentDTO>> getAllPayments() {
        List<LoanPaymentDTO> payments = loanPaymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Lấy payment theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<LoanPaymentDTO> getPaymentById(@PathVariable Long id) {
        Optional<LoanPaymentDTO> payment = loanPaymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Lấy payment theo loan ID
     */
    @GetMapping("/loan/{loanId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('USER')")
    public ResponseEntity<LoanPaymentDTO> getPaymentByLoanId(@PathVariable Long loanId) {
        Optional<LoanPaymentDTO> payment = loanPaymentService.getPaymentByLoanId(loanId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Lấy tất cả payments của một patron
     */
    @GetMapping("/patron/{patronId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or #patronId == authentication.principal.id")
    public ResponseEntity<List<LoanPaymentDTO>> getPaymentsByPatronId(@PathVariable Long patronId) {
        List<LoanPaymentDTO> payments = loanPaymentService.getPaymentsByPatronId(patronId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Lấy danh sách cash payments chờ xác nhận (Admin/Librarian)
     */
    @GetMapping("/pending-cash")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<LoanPaymentDTO>> getPendingCashPayments() {
        List<LoanPaymentDTO> payments = loanPaymentService.getPendingCashPayments();
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Lấy pending payments của patron
     */
    @GetMapping("/patron/{patronId}/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or #patronId == authentication.principal.id")
    public ResponseEntity<List<LoanPaymentDTO>> getPendingPaymentsByPatronId(@PathVariable Long patronId) {
        List<LoanPaymentDTO> payments = loanPaymentService.getPendingPaymentsByPatronId(patronId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Xác nhận thanh toán tiền mặt (Admin/Librarian)
     */
    @PutMapping("/{paymentId}/confirm-cash")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<?> confirmCashPayment(
            @PathVariable Long paymentId,
            @RequestParam Long confirmedBy) {
        try {
            LoanPaymentDTO payment = loanPaymentService.confirmCashPayment(paymentId, confirmedBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xác nhận thanh toán thành công!");
            response.put("payment", payment);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * VNPay callback endpoint
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> vnpayCallback(@RequestParam Map<String, String> params) {
        try {
            String orderId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String transactionNo = params.get("vnp_TransactionNo");
            String bankCode = params.get("vnp_BankCode");
            
            LoanPaymentDTO payment = loanPaymentService.processVNPayCallback(
                orderId, responseCode, transactionNo, bankCode
            );
            
            Map<String, Object> response = new HashMap<>();
            
            if ("00".equals(responseCode)) {
                response.put("success", true);
                response.put("message", "Thanh toán thành công! Bạn đã mượn sách thành công.");
                response.put("payment", payment);
            } else {
                response.put("success", false);
                response.put("message", "Thanh toán thất bại. Vui lòng thử lại.");
                response.put("responseCode", responseCode);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Lỗi xử lý callback: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Hủy payment
     */
    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<?> cancelPayment(@PathVariable Long paymentId) {
        try {
            loanPaymentService.cancelPayment(paymentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã hủy thanh toán");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Đếm số lượng payments chờ xác nhận
     */
    @GetMapping("/pending-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<Map<String, Long>> countPendingPayments() {
        Long count = loanPaymentService.countPendingPayments();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}
