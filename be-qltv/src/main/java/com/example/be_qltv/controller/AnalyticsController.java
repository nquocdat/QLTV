package com.example.be_qltv.controller;

import com.example.be_qltv.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Get comprehensive library analytics
     */
    @GetMapping("/library")
    public ResponseEntity<Map<String, Object>> getLibraryAnalytics() {
        Map<String, Object> analytics = analyticsService.getLibraryAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get book status statistics
     */
    @GetMapping("/books/status")
    public ResponseEntity<Map<String, Long>> getBookStatusStats() {
        Map<String, Long> stats = analyticsService.getBookStatusStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get loan trends over specified number of months
     */
    @GetMapping("/loans/trends")
    public ResponseEntity<List<Map<String, Object>>> getLoanTrends(
            @RequestParam(defaultValue = "12") int months
    ) {
        List<Map<String, Object>> trends = analyticsService.getLoanTrends(months);
        return ResponseEntity.ok(trends);
    }

    /**
     * Get membership tier distribution
     */
    @GetMapping("/membership/distribution")
    public ResponseEntity<List<Map<String, Object>>> getMembershipDistribution() {
        List<Map<String, Object>> distribution = analyticsService.getMembershipDistribution();
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get top borrowed books
     */
    @GetMapping("/books/top-borrowed")
    public ResponseEntity<List<Map<String, Object>>> getTopBorrowedBooks(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Map<String, Object>> topBooks = analyticsService.getTopBorrowedBooks(limit);
        return ResponseEntity.ok(topBooks);
    }

    /**
     * Get top active patrons
     */
    @GetMapping("/patrons/top-active")
    public ResponseEntity<List<Map<String, Object>>> getTopActivePatrons(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Map<String, Object>> topPatrons = analyticsService.getTopActivePatrons(limit);
        return ResponseEntity.ok(topPatrons);
    }
}
