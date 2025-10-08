package com.example.be_qltv.controller;

import com.example.be_qltv.dto.LoanDTO;
import com.example.be_qltv.entity.LoanPayment;
import com.example.be_qltv.repository.LoanPaymentRepository;
import com.example.be_qltv.service.LoanService;
import com.example.be_qltv.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @PutMapping("/{loanId}/confirm-return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<LoanDTO> confirmReturnBook(@PathVariable Long loanId) {
        try {
            LoanDTO loan = loanService.confirmReturnBook(loanId);
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Autowired
    private LoanService loanService;
    
    @Autowired
    private LoanPaymentRepository loanPaymentRepository;
    
    @Autowired
    private VNPayService vnPayService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        List<LoanDTO> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<LoanDTO> getLoanById(@PathVariable Long id) {
        Optional<LoanDTO> loan = loanService.getLoanById(id);
        return loan.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/patron/{patronId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or #patronId == authentication.principal.id")
    public ResponseEntity<List<LoanDTO>> getLoansByPatronId(@PathVariable Long patronId) {
        List<LoanDTO> loans = loanService.getLoansByPatronId(patronId);
        return ResponseEntity.ok(loans);
    }
    
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<LoanDTO>> getLoansByBookId(@PathVariable Long bookId) {
        List<LoanDTO> loans = loanService.getLoansByBookId(bookId);
        return ResponseEntity.ok(loans);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<LoanDTO>> getActiveLoans() {
        List<LoanDTO> loans = loanService.getActiveLoans();
        return ResponseEntity.ok(loans);
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<LoanDTO>> getOverdueLoans() {
        List<LoanDTO> loans = loanService.getOverdueLoans();
        return ResponseEntity.ok(loans);
    }
    
    @PostMapping("/borrow")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<LoanDTO> borrowBook(@RequestParam Long bookId, @RequestParam Long patronId) {
        try {
            LoanDTO loan = loanService.borrowBook(bookId, patronId);
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Mượn sách với thanh toán (CASH hoặc VNPAY)
     */
    @PostMapping("/borrow-with-payment")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> borrowBookWithPayment(
            @RequestParam Long bookId,
            @RequestParam Long patronId,
            @RequestParam String paymentMethod,
            HttpServletRequest request) {
        try {
            System.out.println("LoanController.borrowBookWithPayment - START");
            System.out.println("Params: bookId=" + bookId + ", patronId=" + patronId + ", paymentMethod=" + paymentMethod);
            
            // Tạo loan và payment
            LoanDTO loan = loanService.borrowBookWithPayment(bookId, patronId, paymentMethod);
            
            Map<String, Object> response = new HashMap<>();
            response.put("loanId", loan.getId());
            
            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                // Tạo VNPay payment URL
                LoanPayment payment = loanPaymentRepository.findByLoanId(loan.getId())
                        .orElseThrow(() -> new RuntimeException("Payment not found"));
                
                String paymentUrl = vnPayService.createLoanPaymentUrl(payment, request);
                
                response.put("paymentUrl", paymentUrl);
                response.put("paymentMethod", "VNPAY");
                response.put("message", "Vui lòng thanh toán để hoàn tất mượn sách");
                response.put("amount", payment.getAmount());
            } else {
                // Cash payment
                response.put("paymentMethod", "CASH");
                response.put("message", "Vui lòng đến quầy thủ thư để thanh toán tiền mặt và nhận sách");
                response.put("amount", 50000);
            }
            
            System.out.println("LoanController.borrowBookWithPayment - SUCCESS");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("LoanController.borrowBookWithPayment - ERROR");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("type", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{loanId}/return")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<LoanDTO> returnBook(@PathVariable Long loanId) {
        try {
            LoanDTO loan = loanService.returnBook(loanId);
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Trả sách với phí phạt hỏng sách (Admin/Librarian only)
     * PUT /api/loans/{loanId}/return-with-damage?damageFine=50000&damageNotes=...
     */
    @PutMapping("/{loanId}/return-with-damage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<?> returnBookWithDamage(
            @PathVariable Long loanId,
            @RequestParam java.math.BigDecimal damageFine,
            @RequestParam(required = false) String damageNotes) {
        try {
            LoanDTO loan = loanService.returnBookWithDamageFine(loanId, damageFine, damageNotes);
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{loanId}/renew")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<LoanDTO> renewLoan(@PathVariable Long loanId) {
        try {
            LoanDTO loan = loanService.renewLoan(loanId);
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/patron/{patronId}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or #patronId == authentication.principal.id")
    public ResponseEntity<List<LoanDTO>> getPatronLoanHistory(@PathVariable Long patronId) {
        try {
            List<LoanDTO> loans = loanService.getPatronLoanHistory(patronId);
            return ResponseEntity.ok(loans);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/fines")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<LoanDTO>> getLoansWithFines() {
        List<LoanDTO> loans = loanService.getLoansWithFines();
        return ResponseEntity.ok(loans);
    }
}
