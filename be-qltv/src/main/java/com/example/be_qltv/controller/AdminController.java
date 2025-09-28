package com.example.be_qltv.controller;

import com.example.be_qltv.dto.PatronDTO;
import com.example.be_qltv.service.PatronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private PatronService patronService;

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<PatronDTO>> getAllUsers() {
        List<PatronDTO> users = patronService.getAllPatrons();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/paginated")
    public ResponseEntity<Page<PatronDTO>> getAllUsersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PatronDTO> users = patronService.getAllPatronsWithPagination(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<PatronDTO>> getUsersByRole(@PathVariable String role) {
        List<PatronDTO> users = patronService.getPatronsByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/active")
    public ResponseEntity<List<PatronDTO>> getActiveUsers() {
        List<PatronDTO> users = patronService.getActivePatrons();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id, 
            @RequestBody Map<String, String> roleData) {
        
        String newRole = roleData.get("role");
        boolean updated = patronService.updateRole(id, newRole);
        
        if (updated) {
            return ResponseEntity.ok().body(Map.of("message", "Role updated successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        boolean activated = patronService.activatePatron(id);
        
        if (activated) {
            return ResponseEntity.ok().body(Map.of("message", "User activated successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        boolean deactivated = patronService.deactivatePatron(id);
        
        if (deactivated) {
            return ResponseEntity.ok().body(Map.of("message", "User deactivated successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = patronService.deletePatron(id);
        
        if (deleted) {
            return ResponseEntity.ok().body(Map.of("message", "User deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // System Statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        // This would require implementation in respective services
        Map<String, Object> stats = Map.of(
            "totalUsers", patronService.getAllPatrons().size(),
            "activeUsers", patronService.getActivePatrons().size(),
            "totalBooks", 0, // BookService.getTotalBooks()
            "totalLoans", 0  // LoanService.getTotalLoans()
        );
        return ResponseEntity.ok(stats);
    }
}
