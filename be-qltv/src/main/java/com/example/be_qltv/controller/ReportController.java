package com.example.be_qltv.controller;

import com.example.be_qltv.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
public class ReportController {

    @Autowired
    private ReportService reportService;

        /**
         * API trả về báo cáo tổng quan (overview)
         * Yêu cầu quyền ADMIN hoặc LIBRARIAN
         */
        @GetMapping("/overview")
        public ResponseEntity<Map<String, Object>> getOverviewReport() {
            Map<String, Object> stats = reportService.getDashboardStatistics();
            return ResponseEntity.ok(stats);
        }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = reportService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/loans/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyLoanReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.getMonthlyLoanReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/books/popular")
    public ResponseEntity<Map<String, Object>> getPopularBooksReport(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> report = reportService.getPopularBooksReport(limit);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/patrons/active")
    public ResponseEntity<Map<String, Object>> getActivePatronsReport() {
        Map<String, Object> report = reportService.getActivePatronsReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/overdue")
    public ResponseEntity<Map<String, Object>> getOverdueReport() {
        Map<String, Object> report = reportService.getOverdueLoansReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/fines")
    public ResponseEntity<Map<String, Object>> getFinesReport() {
        Map<String, Object> report = reportService.getFinesReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/books/genre-distribution")
    public ResponseEntity<Map<String, Object>> getGenreDistributionReport() {
        Map<String, Object> report = reportService.getGenreDistributionReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/loans/daily")
    public ResponseEntity<Map<String, Object>> getDailyLoanReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> report = reportService.getDailyLoanReport(date);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> getInventoryReport() {
        Map<String, Object> report = reportService.getInventoryReport();
        return ResponseEntity.ok(report);
    }
}
