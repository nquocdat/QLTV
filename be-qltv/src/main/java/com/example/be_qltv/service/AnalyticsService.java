package com.example.be_qltv.service;

import com.example.be_qltv.entity.Book;
import com.example.be_qltv.entity.Loan;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.repository.BookRepository;
import com.example.be_qltv.repository.LoanRepository;
import com.example.be_qltv.repository.PatronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PatronRepository patronRepository;

    /**
     * Get comprehensive library analytics
     */
    public Map<String, Object> getLibraryAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("bookStatusStats", getBookStatusStats());
        analytics.put("loanTrends", getLoanTrends(12));
        analytics.put("membershipDistribution", getMembershipDistribution());
        
        // Map to frontend interface
        analytics.put("topReaders", getTopActivePatrons(10)); // topPatrons -> topReaders
        analytics.put("frequentLateReturners", getFrequentLateReturners(10));
        analytics.put("popularBooks", getTopBorrowedBooks(10)); // topBorrowedBooks -> popularBooks
        
        return analytics;
    }

    /**
     * Get book status statistics
     */
    public Map<String, Long> getBookStatusStats() {
        List<Book> books = bookRepository.findAll();
        
        Map<String, Long> statusStats = books.stream()
            .collect(Collectors.groupingBy(
                book -> book.getStatus() != null ? book.getStatus().name() : "UNKNOWN",
                Collectors.counting()
            ));
        
        // Ensure all statuses are present
        statusStats.putIfAbsent("AVAILABLE", 0L);
        statusStats.putIfAbsent("BORROWED", 0L);
        statusStats.putIfAbsent("RESERVED", 0L);
        statusStats.putIfAbsent("MAINTENANCE", 0L);
        
        return statusStats;
    }

    /**
     * Get loan trends for the specified number of months
     */
    public List<Map<String, Object>> getLoanTrends(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        List<Loan> loans = loanRepository.findLoansByDateRange(startDate, endDate);
        
        // Group loans by month
        Map<YearMonth, List<Loan>> loansByMonth = loans.stream()
            .collect(Collectors.groupingBy(
                loan -> YearMonth.from(loan.getLoanDate())
            ));
        
        // Build trend data
        List<Map<String, Object>> trends = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);
        
        while (!current.isAfter(end)) {
            Map<String, Object> monthData = new HashMap<>();
            List<Loan> monthLoans = loansByMonth.getOrDefault(current, new ArrayList<>());
            
            monthData.put("period", current.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + 
                                   " " + current.getYear());
            monthData.put("totalLoans", monthLoans.size());
            
            // Calculate return rate
            long returnedCount = monthLoans.stream()
                .filter(loan -> loan.getReturnDate() != null)
                .count();
            double returnRate = monthLoans.isEmpty() ? 0 : 
                (returnedCount * 100.0 / monthLoans.size());
            monthData.put("returnRate", Math.round(returnRate * 100.0) / 100.0);
            
            // New members count (placeholder - can be enhanced if createdAt field is added to Patron)
            monthData.put("newMembers", 0);
            
            trends.add(monthData);
            current = current.plusMonths(1);
        }
        
        return trends;
    }

    /**
     * Get membership tier distribution
     */
    public List<Map<String, Object>> getMembershipDistribution() {
        List<Patron> patrons = patronRepository.findAll();
        long totalPatrons = patrons.size();
        
        // Group by role as proxy for membership tiers
        Map<String, Long> roleDistribution = patrons.stream()
            .collect(Collectors.groupingBy(
                patron -> patron.getRole() != null ? patron.getRole().name() : "USER",
                Collectors.counting()
            ));
        
        // Color mapping for different roles
        Map<String, String> roleColors = new HashMap<>();
        roleColors.put("ADMIN", "#EF4444"); // red
        roleColors.put("LIBRARIAN", "#F59E0B"); // amber
        roleColors.put("USER", "#10B981"); // green
        
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Map.Entry<String, Long> entry : roleDistribution.entrySet()) {
            Map<String, Object> tierData = new HashMap<>();
            tierData.put("tierName", entry.getKey()); // Changed from tier to tierName
            tierData.put("memberCount", entry.getValue()); // Changed from count to memberCount
            
            // Calculate percentage
            double percentage = totalPatrons > 0 ? (entry.getValue() * 100.0 / totalPatrons) : 0;
            tierData.put("percentage", Math.round(percentage * 100.0) / 100.0);
            
            tierData.put("color", roleColors.getOrDefault(entry.getKey(), "#6B7280"));
            distribution.add(tierData);
        }
        
        return distribution;
    }

    /**
     * Get top borrowed books
     */
    public List<Map<String, Object>> getTopBorrowedBooks(int limit) {
        List<Loan> allLoans = loanRepository.findAll();
        
        // Count loans per book
        Map<Long, Long> loanCounts = allLoans.stream()
            .filter(loan -> loan.getBook() != null)
            .collect(Collectors.groupingBy(
                loan -> loan.getBook().getId(),
                Collectors.counting()
            ));
        
        // Get top books
        List<Map<String, Object>> topBooks = new ArrayList<>();
        loanCounts.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(limit)
            .forEach(entry -> {
                Book book = bookRepository.findById(entry.getKey()).orElse(null);
                if (book != null) {
                    Map<String, Object> bookData = new HashMap<>();
                    bookData.put("bookId", book.getId()); // Changed from id to bookId
                    bookData.put("title", book.getTitle());
                    bookData.put("author", book.getAuthor());
                    bookData.put("loanCount", entry.getValue()); // Changed from borrowCount to loanCount
                    bookData.put("rating", 4.5); // Default rating
                    bookData.put("category", book.getCategory() != null ? book.getCategory().getName() : "Uncategorized");
                    topBooks.add(bookData);
                }
            });
        
        return topBooks;
    }

    /**
     * Get top active patrons
     */
    public List<Map<String, Object>> getTopActivePatrons(int limit) {
        List<Loan> allLoans = loanRepository.findAll();
        
        // Count loans per patron
        Map<Long, Long> patronLoanCounts = allLoans.stream()
            .filter(loan -> loan.getPatron() != null)
            .collect(Collectors.groupingBy(
                loan -> loan.getPatron().getId(),
                Collectors.counting()
            ));
        
        // Get top patrons
        List<Map<String, Object>> topPatrons = new ArrayList<>();
        patronLoanCounts.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(limit)
            .forEach(entry -> {
                Patron patron = patronRepository.findById(entry.getKey()).orElse(null);
                if (patron != null) {
                    Map<String, Object> patronData = new HashMap<>();
                    patronData.put("userId", patron.getId()); // Changed from id to userId
                    patronData.put("userName", patron.getName()); // Changed from name to userName
                    patronData.put("email", patron.getEmail());
                    patronData.put("totalLoans", entry.getValue()); // Changed from loanCount to totalLoans
                    patronData.put("currentTier", patron.getRole() != null ? patron.getRole().name() : "USER");
                    topPatrons.add(patronData);
                }
            });
        
        return topPatrons;
    }

    /**
     * Get frequent late returners
     */
    public List<Map<String, Object>> getFrequentLateReturners(int limit) {
        List<Loan> allLoans = loanRepository.findAll();
        
        // Count late returns per patron
        Map<Long, Map<String, Long>> patronStats = new HashMap<>();
        
        for (Loan loan : allLoans) {
            if (loan.getPatron() == null) continue;
            
            Long patronId = loan.getPatron().getId();
            patronStats.putIfAbsent(patronId, new HashMap<>());
            Map<String, Long> stats = patronStats.get(patronId);
            
            // Count total loans
            stats.put("totalLoans", stats.getOrDefault("totalLoans", 0L) + 1);
            
            // Count late returns
            if (loan.getReturnDate() != null && loan.getDueDate() != null) {
                if (loan.getReturnDate().isAfter(loan.getDueDate())) {
                    stats.put("lateReturns", stats.getOrDefault("lateReturns", 0L) + 1);
                }
            }
        }
        
        // Get top late returners
        List<Map<String, Object>> lateReturners = new ArrayList<>();
        patronStats.entrySet().stream()
            .filter(entry -> entry.getValue().getOrDefault("lateReturns", 0L) > 0)
            .sorted((e1, e2) -> Long.compare(
                e2.getValue().getOrDefault("lateReturns", 0L),
                e1.getValue().getOrDefault("lateReturns", 0L)
            ))
            .limit(limit)
            .forEach(entry -> {
                Patron patron = patronRepository.findById(entry.getKey()).orElse(null);
                if (patron != null) {
                    Map<String, Object> returnerData = new HashMap<>();
                    returnerData.put("userId", patron.getId());
                    returnerData.put("userName", patron.getName());
                    returnerData.put("lateReturns", entry.getValue().getOrDefault("lateReturns", 0L));
                    returnerData.put("totalLoans", entry.getValue().getOrDefault("totalLoans", 0L));
                    
                    // Calculate rating based on late return ratio
                    long totalLoans = entry.getValue().getOrDefault("totalLoans", 0L);
                    long lateReturns = entry.getValue().getOrDefault("lateReturns", 0L);
                    double lateRatio = totalLoans > 0 ? (lateReturns * 100.0 / totalLoans) : 0;
                    
                    String rating = "GOOD";
                    if (lateRatio > 50) rating = "POOR";
                    else if (lateRatio > 20) rating = "FAIR";
                    
                    returnerData.put("rating", rating);
                    lateReturners.add(returnerData);
                }
            });
        
        return lateReturners;
    }

    /**
     * Get category distribution for pie chart
     * Returns book count by category
     */
    public List<Map<String, Object>> getCategoryDistribution() {
        try {
            List<Book> books = bookRepository.findAll();
            
            // Group books by category
            Map<String, Long> categoryCount = books.stream()
                .filter(book -> book.getCategory() != null)
                .collect(Collectors.groupingBy(
                    book -> book.getCategory().getName(),
                    Collectors.counting()
                ));
            
            // Convert to list format
            List<Map<String, Object>> distribution = new ArrayList<>();
            for (Map.Entry<String, Long> entry : categoryCount.entrySet()) {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("categoryName", entry.getKey());
                categoryData.put("bookCount", entry.getValue());
                distribution.add(categoryData);
            }
            
            // Sort by book count descending
            distribution.sort((a, b) -> 
                Long.compare((Long) b.get("bookCount"), (Long) a.get("bookCount"))
            );
            
            return distribution;
        } catch (Exception e) {
            System.err.println("Error in getCategoryDistribution: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
