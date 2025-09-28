package com.example.be_qltv.service;

import com.example.be_qltv.dto.PatronDTO;
import com.example.be_qltv.dto.BookDTO;
import com.example.be_qltv.dto.LoanDTO;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.repository.PatronRepository;
import com.example.be_qltv.repository.BookRepository;
import com.example.be_qltv.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    @Autowired
    private PatronRepository patronRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PatronService patronService;

    @Autowired
    private BookService bookService;

    @Autowired
    private LoanService loanService;

    // User Management Methods
    public List<PatronDTO> getAllUsers() {
        return patronService.getAllPatrons();
    }

    public Page<PatronDTO> getAllUsersWithPagination(Pageable pageable) {
        return patronService.getAllPatronsWithPagination(pageable);
    }

    public Page<PatronDTO> searchUsersWithPagination(String searchTerm, Pageable pageable) {
        return patronService.searchPatronsWithPagination(searchTerm, pageable);
    }

    public List<PatronDTO> getUsersByRole(String role) {
        return patronService.getPatronsByRole(role);
    }

    public Page<PatronDTO> getUsersByRoleWithPagination(String role, Pageable pageable) {
        return patronService.getPatronsByRoleWithPagination(role, pageable);
    }

    public List<PatronDTO> getActiveUsers() {
        return patronService.getActivePatrons();
    }

    public Page<PatronDTO> getActiveUsersWithPagination(Pageable pageable) {
        return patronService.getActivePatronsWithPagination(pageable);
    }

    public boolean updateUserRole(Long userId, String newRole) {
        return patronService.updateRole(userId, newRole);
    }

    public boolean activateUser(Long userId) {
        return patronService.activatePatron(userId);
    }

    public boolean deactivateUser(Long userId) {
        return patronService.deactivatePatron(userId);
    }

    public boolean deleteUser(Long userId) {
        return patronService.deletePatron(userId);
    }

    // System Statistics
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // User statistics
        long totalUsers = patronRepository.count();
        long activeUsers = patronRepository.countActivePatrons();
        long adminUsers = patronRepository.countByRole(Patron.Role.ADMIN);
        long librarianUsers = patronRepository.countByRole(Patron.Role.LIBRARIAN);
        long regularUsers = patronRepository.countByRole(Patron.Role.USER);
        
        // Book statistics
        long totalBooks = bookRepository.count();
        long availableBooks = bookRepository.findAvailableBooks().size();
        
        // Loan statistics
        long totalLoans = loanRepository.count();
        long activeLoans = loanRepository.findActiveLoan().size();
        long overdueLoans = loanRepository.findOverdueLoans().size();
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("librarianUsers", librarianUsers);
        stats.put("regularUsers", regularUsers);
        stats.put("totalBooks", totalBooks);
        stats.put("availableBooks", availableBooks);
        stats.put("totalLoans", totalLoans);
        stats.put("activeLoans", activeLoans);
        stats.put("overdueLoans", overdueLoans);
        
        return stats;
    }

    // Advanced User Management
    public Map<String, Object> getUserStatistics(Long userId) {
        Map<String, Object> userStats = new HashMap<>();
        
        List<LoanDTO> userLoans = loanService.getLoansByPatronId(userId);
        long totalLoans = userLoans.size();
        long activeLoans = userLoans.stream()
            .filter(loan -> "BORROWED".equals(loan.getStatus()))
            .count();
        long overdueLoans = userLoans.stream()
            .filter(loan -> "OVERDUE".equals(loan.getStatus()))
            .count();
        
        userStats.put("totalLoans", totalLoans);
        userStats.put("activeLoans", activeLoans);
        userStats.put("overdueLoans", overdueLoans);
        
        return userStats;
    }

    // System Health Check
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check database connectivity
            long userCount = patronRepository.count();
            long bookCount = bookRepository.count();
            long loanCount = loanRepository.count();
            
            health.put("status", "healthy");
            health.put("database", "connected");
            health.put("userCount", userCount);
            health.put("bookCount", bookCount);
            health.put("loanCount", loanCount);
            
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    // Bulk Operations
    public Map<String, Object> bulkUpdateUserRole(List<Long> userIds, String newRole) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (Long userId : userIds) {
            try {
                if (patronService.updateRole(userId, newRole)) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                failureCount++;
            }
        }
        
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("totalProcessed", userIds.size());
        
        return result;
    }

    public Map<String, Object> bulkDeactivateUsers(List<Long> userIds) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (Long userId : userIds) {
            try {
                if (patronService.deactivatePatron(userId)) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                failureCount++;
            }
        }
        
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("totalProcessed", userIds.size());
        
        return result;
    }
}
