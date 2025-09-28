package com.example.be_qltv.service;

import com.example.be_qltv.entity.Book;
import com.example.be_qltv.entity.Loan;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.repository.BookRepository;
import com.example.be_qltv.repository.LoanRepository;
import com.example.be_qltv.repository.PatronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PatronRepository patronRepository;

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        stats.put("totalBooks", bookRepository.count());
        stats.put("totalPatrons", patronRepository.count());
        stats.put("totalLoans", loanRepository.count());
        stats.put("availableBooks", bookRepository.findAvailableBooks().size());
        
        // Current status
        stats.put("activeLoans", loanRepository.findActiveLoan().size());
        stats.put("overdueLoans", loanRepository.findOverdueLoans().size());
        stats.put("activePatrons", patronRepository.countActivePatrons());
        
        // Recent activity
        LocalDate today = LocalDate.now();
        stats.put("loansToday", loanRepository.findLoansByDateRange(today, today).size());
        stats.put("loansDueToday", loanRepository.findLoansDueToday().size());
        
        return stats;
    }

    public Map<String, Object> getMonthlyLoanReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<Loan> loans = loanRepository.findLoansByDateRange(startDate, endDate);
        
        // Group by month
        Map<String, Long> monthlyData = loans.stream()
            .collect(Collectors.groupingBy(
                loan -> loan.getLoanDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + 
                       " " + loan.getLoanDate().getYear(),
                Collectors.counting()
            ));
        
        report.put("monthlyData", monthlyData);
        report.put("totalLoans", loans.size());
        report.put("period", startDate + " to " + endDate);
        
        return report;
    }

    public Map<String, Object> getPopularBooksReport(int limit) {
        Map<String, Object> report = new HashMap<>();
        
        List<Object[]> popularBooks = loanRepository.findMostBorrowedBooks();
        
        List<Map<String, Object>> bookData = popularBooks.stream()
            .limit(limit)
            .map(row -> {
                Map<String, Object> bookInfo = new HashMap<>();
                Book book = (Book) row[0];
                Long count = (Long) row[1];
                
                bookInfo.put("book", book);
                bookInfo.put("borrowCount", count);
                return bookInfo;
            })
            .collect(Collectors.toList());
        
        report.put("popularBooks", bookData);
        report.put("limit", limit);
        
        return report;
    }

    public Map<String, Object> getActivePatronsReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<Patron> activePatrons = patronRepository.findByIsActiveTrue();
        
        // Group by role
        Map<String, Long> roleDistribution = activePatrons.stream()
            .collect(Collectors.groupingBy(
                patron -> patron.getRole().name(),
                Collectors.counting()
            ));
        
        report.put("totalActivePatrons", activePatrons.size());
        report.put("roleDistribution", roleDistribution);
        report.put("activePatrons", activePatrons);
        
        return report;
    }

    public Map<String, Object> getOverdueLoansReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<Loan> overdueLoans = loanRepository.findOverdueLoans();
        
        // Calculate total fines
        BigDecimal totalFines = overdueLoans.stream()
            .map(Loan::getFineAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Group by patron
        Map<Patron, List<Loan>> loansByPatron = overdueLoans.stream()
            .collect(Collectors.groupingBy(Loan::getPatron));
        
        report.put("overdueLoans", overdueLoans);
        report.put("totalOverdue", overdueLoans.size());
        report.put("totalFines", totalFines);
        report.put("affectedPatrons", loansByPatron.size());
        report.put("loansByPatron", loansByPatron);
        
        return report;
    }

    public Map<String, Object> getFinesReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<Loan> loansWithFines = loanRepository.findLoansWithFines();
        
        BigDecimal totalFines = loansWithFines.stream()
            .map(Loan::getFineAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        report.put("loansWithFines", loansWithFines);
        report.put("totalFines", totalFines);
        report.put("fineCount", loansWithFines.size());
        
        return report;
    }

    public Map<String, Object> getGenreDistributionReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<Book> allBooks = bookRepository.findAll();
        
        // Group by genre
        Map<String, Long> genreDistribution = allBooks.stream()
            .filter(book -> book.getGenre() != null && !book.getGenre().trim().isEmpty())
            .collect(Collectors.groupingBy(
                Book::getGenre,
                Collectors.counting()
            ));
        
        // Calculate percentages
        long totalBooks = allBooks.size();
        Map<String, Double> genrePercentages = genreDistribution.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (entry.getValue() * 100.0) / totalBooks
            ));
        
        report.put("genreDistribution", genreDistribution);
        report.put("genrePercentages", genrePercentages);
        report.put("totalBooks", totalBooks);
        
        return report;
    }

    public Map<String, Object> getDailyLoanReport(LocalDate date) {
        Map<String, Object> report = new HashMap<>();
        
        List<Loan> dailyLoans = loanRepository.findLoansByDateRange(date, date);
        List<Loan> dailyReturns = loanRepository.findReturnsByDate(date);
        List<Loan> dueToday = loanRepository.findLoansDueToday();
        
        report.put("date", date);
        report.put("newLoans", dailyLoans.size());
        report.put("returns", dailyReturns.size());
        report.put("dueToday", dueToday.size());
        report.put("loans", dailyLoans);
        report.put("returns", dailyReturns);
        
        return report;
    }

    public Map<String, Object> getInventoryReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<Book> allBooks = bookRepository.findAll();
        List<Book> lowStockBooks = bookRepository.findBooksWithLowStock(3); // threshold = 3
        
        // Calculate total inventory value (assuming we had a price field)
        long totalCopies = allBooks.stream()
            .mapToLong(Book::getTotalCopies)
            .sum();
        
        long availableCopies = allBooks.stream()
            .mapToLong(Book::getAvailableCopies)
            .sum();
        
        report.put("totalBooks", allBooks.size());
        report.put("totalCopies", totalCopies);
        report.put("availableCopies", availableCopies);
        report.put("onLoanCopies", totalCopies - availableCopies);
        report.put("lowStockBooks", lowStockBooks);
        report.put("lowStockCount", lowStockBooks.size());
        
        return report;
    }
}
