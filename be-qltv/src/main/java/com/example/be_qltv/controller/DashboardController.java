package com.example.be_qltv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.be_qltv.service.BookService;
import com.example.be_qltv.service.LoanService;
import com.example.be_qltv.service.PatronService;
import com.example.be_qltv.service.CategoryService;
import com.example.be_qltv.service.AuthorService;
import com.example.be_qltv.service.PublisherService;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
public class DashboardController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private LoanService loanService;
    
    @Autowired
    private PatronService patronService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private PublisherService publisherService;
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get current counts using available methods
            long totalBooks = bookService.getAllBooks().size();
            long totalUsers = patronService.getAllPatrons().size();
            long activeLoans = loanService.getAllLoans().stream()
                    .filter(loan -> loan.getStatus().equals("ACTIVE"))
                    .count();
            long overdueLoans = loanService.getAllLoans().stream()
                    .filter(loan -> loan.getStatus().equals("OVERDUE"))
                    .count();
            long totalCategories = categoryService.getAllCategories().size();
            long totalAuthors = authorService.getAllAuthors().size();
            long totalPublishers = publisherService.getAllPublishers().size();
            
            // Get monthly statistics (simplified for now)
            long newBooksThisMonth = 0; // TODO: Implement proper counting
            long newUsersThisMonth = 0; // TODO: Implement proper counting
            
            stats.put("totalBooks", totalBooks);
            stats.put("totalUsers", totalUsers);
            stats.put("activeLoans", activeLoans);
            stats.put("overdueLoans", overdueLoans);
            stats.put("revenue", 0); // TODO: Calculate actual revenue if applicable
            stats.put("newBooksThisMonth", newBooksThisMonth);
            stats.put("newUsersThisMonth", newUsersThisMonth);
            stats.put("totalCategories", totalCategories);
            stats.put("totalAuthors", totalAuthors);
            stats.put("totalPublishers", totalPublishers);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            // Return default stats if services are not available
            stats.put("totalBooks", 0);
            stats.put("totalUsers", 0);
            stats.put("activeLoans", 0);
            stats.put("overdueLoans", 0);
            stats.put("revenue", 0);
            stats.put("newBooksThisMonth", 0);
            stats.put("newUsersThisMonth", 0);
            stats.put("totalCategories", 0);
            stats.put("totalAuthors", 0);
            stats.put("totalPublishers", 0);
            
            return ResponseEntity.ok(stats);
        }
    }
    
    @GetMapping("/popular-books")
    public ResponseEntity<Object> getPopularBooks() {
        try {
            // TODO: Implement actual popular books logic based on loan frequency
            return ResponseEntity.ok("[]"); // Return empty array for now
        } catch (Exception e) {
            return ResponseEntity.ok("[]");
        }
    }
    
    @GetMapping("/recent-activities")
    public ResponseEntity<Object> getRecentActivities() {
        try {
            // TODO: Implement actual recent activities logic
            return ResponseEntity.ok("[]"); // Return empty array for now
        } catch (Exception e) {
            return ResponseEntity.ok("[]");
        }
    }
}
