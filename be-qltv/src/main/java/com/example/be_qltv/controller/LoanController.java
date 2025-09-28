package com.example.be_qltv.controller;

import com.example.be_qltv.dto.LoanDTO;
import com.example.be_qltv.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
