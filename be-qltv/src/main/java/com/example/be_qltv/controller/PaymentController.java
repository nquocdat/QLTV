package com.example.be_qltv.controller;

import com.example.be_qltv.entity.Fine;
import com.example.be_qltv.entity.PaymentTransaction;
import com.example.be_qltv.repository.FineRepository;
import com.example.be_qltv.service.PaymentService;
import com.example.be_qltv.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private FineRepository fineRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Tạo URL thanh toán VNPay cho Fine
     * POST /api/payment/create-payment-url
     * Body: { "fineId": 1 }
     */
    @PostMapping("/create-payment-url")
    public ResponseEntity<?> createPaymentUrl(@RequestBody Map<String, Object> request, 
                                              HttpServletRequest httpRequest) {
        try {
            Long fineId = Long.valueOf(request.get("fineId").toString());
            log.info("Creating payment URL for Fine ID: {}", fineId);
            
            Fine fine = fineRepository.findById(fineId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phí phạt với ID: " + fineId));
            
            if ("PAID".equals(fine.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Phí phạt đã được thanh toán"));
            }
            
            String paymentUrl = vnPayService.createPaymentUrl(fine, httpRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("fineId", fineId);
            response.put("amount", fine.getAmount());
            
            log.info("Payment URL created successfully for Fine ID: {}", fineId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating payment URL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi tạo URL thanh toán: " + e.getMessage()));
        }
    }
    
    /**
     * Xử lý callback từ VNPay sau khi thanh toán
     * GET /api/payment/vnpay-return?vnp_xxx=...
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("========== VNPAY CALLBACK RECEIVED ==========");
        log.info("Params: {}", params);
        
        try {
            Map<String, Object> result = vnPayService.verifyPaymentReturn(params);
            
            log.info("Payment verification result: {}", result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing VNPay callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "message", "Lỗi xử lý callback: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Lấy danh sách Fine chưa thanh toán của Patron
     * GET /api/payment/unpaid-fines/{patronId}
     */
    @GetMapping("/unpaid-fines/{patronId}")
    public ResponseEntity<?> getUnpaidFines(@PathVariable Long patronId) {
        try {
            List<Fine> unpaidFines = fineRepository.findByPatronIdAndStatus(patronId, "UNPAID");
            log.info("Found {} unpaid fines for Patron ID: {}", unpaidFines.size(), patronId);
            return ResponseEntity.ok(unpaidFines);
        } catch (Exception e) {
            log.error("Error fetching unpaid fines: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi lấy danh sách phí phạt: " + e.getMessage()));
        }
    }
    
    /**
     * Legacy endpoints - giữ lại để tương thích
     */
    @PostMapping("/cash")
    public ResponseEntity<?> confirmCashPayment(@RequestParam Long amount, 
                                                @RequestParam String orderId, 
                                                @RequestParam String description) {
        try {
            PaymentTransaction transaction = paymentService.confirmCashPayment(amount, orderId, description);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Error confirming cash payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
