package com.example.be_qltv.controller;

import com.example.be_qltv.dto.ChangePasswordRequest;
import com.example.be_qltv.dto.PatronDTO;
import com.example.be_qltv.service.PatronService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/patrons")
public class PatronController {
    
    @Autowired
    private PatronService patronService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<PatronDTO>> getAllPatrons() {
        List<PatronDTO> patrons = patronService.getAllPatrons();
        return ResponseEntity.ok(patrons);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatronDTO> createPatron(@Valid @RequestBody PatronDTO patronDTO) {
        PatronDTO createdPatron = patronService.createPatron(patronDTO);
        return ResponseEntity.ok(createdPatron);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or #id == authentication.principal.id")
    public ResponseEntity<PatronDTO> getPatronById(@PathVariable Long id) {
        Optional<PatronDTO> patron = patronService.getPatronById(id);
        return patron.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or #id == authentication.principal.id")
    public ResponseEntity<PatronDTO> updatePatron(@PathVariable Long id, @Valid @RequestBody PatronDTO patronDTO) {
        Optional<PatronDTO> updatedPatron = patronService.updatePatron(id, patronDTO);
        return updatedPatron.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatron(@PathVariable Long id) {
        boolean deleted = patronService.deletePatron(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<PatronDTO>> searchPatrons(@RequestParam String q) {
        List<PatronDTO> patrons = patronService.searchPatrons(q);
        return ResponseEntity.ok(patrons);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<PatronDTO>> getActivePatrons() {
        List<PatronDTO> patrons = patronService.getActivePatrons();
        return ResponseEntity.ok(patrons);
    }
    
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PatronDTO>> getPatronsByRole(@PathVariable String role) {
        List<PatronDTO> patrons = patronService.getPatronsByRole(role);
        return ResponseEntity.ok(patrons);
    }
    
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivatePatron(@PathVariable Long id) {
        boolean deactivated = patronService.deactivatePatron(id);
        return deactivated ? ResponseEntity.ok().build() 
                          : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activatePatron(@PathVariable Long id) {
        boolean activated = patronService.activatePatron(id);
        return activated ? ResponseEntity.ok().build() 
                        : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatronDTO> togglePatronStatus(@PathVariable Long id) {
        Optional<PatronDTO> toggledPatron = patronService.toggleStatus(id);
        return toggledPatron.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePatronRole(@PathVariable Long id, @RequestParam String role) {
        boolean updated = patronService.updateRole(id, role);
        return updated ? ResponseEntity.ok().build() 
                      : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/change-password")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id, 
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            boolean changed = patronService.changePassword(id, request);
            Map<String, String> response = new HashMap<>();
            if (changed) {
                response.put("message", "Password changed successfully!");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to change password");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
