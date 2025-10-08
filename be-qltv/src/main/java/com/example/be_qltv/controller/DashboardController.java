package com.example.be_qltv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.be_qltv.service.ReportService;
import com.example.be_qltv.service.AnalyticsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
public class DashboardController {
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            // Get statistics from ReportService
            Map<String, Object> reportStats = reportService.getDashboardStatistics();
            
            // Map to frontend DashboardStats format
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBooks", reportStats.get("totalBooks"));
            stats.put("totalUsers", reportStats.get("totalUsers"));
            stats.put("activeLoans", reportStats.get("activeLoans"));
            stats.put("overdueLoans", reportStats.get("overdueLoans"));
            stats.put("revenue", reportStats.get("totalRevenue"));
            stats.put("totalCategories", reportStats.get("totalCategories"));
            stats.put("totalAuthors", reportStats.get("totalAuthors"));
            
            // Monthly statistics (placeholder - can be enhanced)
            stats.put("newBooksThisMonth", 0);
            stats.put("newUsersThisMonth", 0);
            stats.put("totalPublishers", 0);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.err.println("Error in getDashboardStats: " + e.getMessage());
            e.printStackTrace();
            
            // Return default stats on error
            Map<String, Object> stats = new HashMap<>();
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
    public ResponseEntity<List<Map<String, Object>>> getPopularBooks(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Map<String, Object>> popularBooks = analyticsService.getTopBorrowedBooks(limit);
            return ResponseEntity.ok(popularBooks);
        } catch (Exception e) {
            System.err.println("Error in getPopularBooks: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/recent-activities")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // Return empty for now - can be implemented later with activity tracking
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get monthly loan statistics for chart
     * Returns data for line chart showing loan trends
     * @param months Number of months to include (default: 6)
     */
    @GetMapping("/monthly-loans")
    public ResponseEntity<Map<String, Object>> getMonthlyLoanStats(
            @RequestParam(defaultValue = "6") int months) {
        
        try {
            List<Map<String, Object>> loanTrends = analyticsService.getLoanTrends(months);
            
            // Transform data for Chart.js format
            List<String> labels = new ArrayList<>();
            List<Integer> values = new ArrayList<>();
            
            for (Map<String, Object> trend : loanTrends) {
                // Extract period and totalLoans from AnalyticsService format
                String period = (String) trend.get("period");
                Integer totalLoans = (Integer) trend.get("totalLoans");
                
                if (period != null) {
                    labels.add(period);
                    values.add(totalLoans != null ? totalLoans : 0);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("labels", labels);
            response.put("values", values);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getMonthlyLoanStats: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("labels", List.of());
            response.put("values", List.of());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get category distribution for pie chart
     * Returns book count by category
     */
    @GetMapping("/category-distribution")
    public ResponseEntity<Map<String, Object>> getCategoryDistribution() {
        try {
            List<Map<String, Object>> distribution = 
                analyticsService.getCategoryDistribution();
            
            // Transform data for Chart.js format
            List<String> labels = new ArrayList<>();
            List<Long> values = new ArrayList<>();
            
            for (Map<String, Object> item : distribution) {
                String categoryName = (String) item.get("categoryName");
                Long bookCount = (Long) item.get("bookCount");
                
                if (categoryName != null && bookCount != null) {
                    labels.add(categoryName);
                    values.add(bookCount);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("labels", labels);
            response.put("values", values);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getCategoryDistribution: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("labels", List.of());
            response.put("values", List.of());
            return ResponseEntity.ok(response);
        }
    }
}
