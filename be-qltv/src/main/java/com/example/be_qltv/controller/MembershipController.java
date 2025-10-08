package com.example.be_qltv.controller;

import com.example.be_qltv.dto.MembershipTierDTO;
import com.example.be_qltv.dto.UserMembershipDTO;
import com.example.be_qltv.service.MembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/membership")
@CrossOrigin(origins = "http://localhost:4200")
public class MembershipController {

    @Autowired
    private MembershipService membershipService;

    // ==================== MEMBERSHIP TIER ENDPOINTS ====================

    /**
     * Get all membership tiers
     * GET /api/membership/tiers
     */
    @GetMapping("/tiers")
    public ResponseEntity<List<MembershipTierDTO>> getAllTiers() {
        try {
            List<MembershipTierDTO> tiers = membershipService.getAllTiers();
            return ResponseEntity.ok(tiers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get membership tier by ID
     * GET /api/membership/tiers/{id}
     */
    @GetMapping("/tiers/{id}")
    public ResponseEntity<MembershipTierDTO> getTierById(@PathVariable Long id) {
        try {
            MembershipTierDTO tier = membershipService.getTierById(id);
            return ResponseEntity.ok(tier);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get membership tier by level
     * GET /api/membership/tiers/level/{level}
     */
    @GetMapping("/tiers/level/{level}")
    public ResponseEntity<MembershipTierDTO> getTierByLevel(@PathVariable String level) {
        try {
            MembershipTierDTO tier = membershipService.getTierByLevel(level);
            return ResponseEntity.ok(tier);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== USER MEMBERSHIP ENDPOINTS ====================

    /**
     * Get user membership by user ID
     * GET /api/membership/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserMembershipDTO> getUserMembership(@PathVariable Long userId) {
        try {
            UserMembershipDTO membership = membershipService.getUserMembership(userId);
            return ResponseEntity.ok(membership);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create default membership for user
     * POST /api/membership/users/{userId}/create
     */
    @PostMapping("/users/{userId}/create")
    public ResponseEntity<UserMembershipDTO> createDefaultMembership(@PathVariable Long userId) {
        try {
            UserMembershipDTO membership = membershipService.createDefaultMembership(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(membership);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update user membership
     * PUT /api/membership/users/{userId}
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<UserMembershipDTO> updateMembership(
            @PathVariable Long userId,
            @RequestBody UserMembershipDTO dto) {
        try {
            UserMembershipDTO updated = membershipService.updateMembership(userId, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Upgrade user membership to new tier
     * POST /api/membership/users/{userId}/upgrade
     */
    @PostMapping("/users/{userId}/upgrade")
    public ResponseEntity<UserMembershipDTO> upgradeMembership(
            @PathVariable Long userId,
            @RequestBody Map<String, Long> request) {
        try {
            Long newTierId = request.get("tierId");
            UserMembershipDTO upgraded = membershipService.upgradeMembership(userId, newTierId);
            return ResponseEntity.ok(upgraded);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add points to user membership
     * POST /api/membership/users/{userId}/points
     */
    @PostMapping("/users/{userId}/points")
    public ResponseEntity<Map<String, String>> addPoints(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer points = request.get("points");
            membershipService.addPoints(userId, points);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Points added successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Increment loan count for user
     * POST /api/membership/users/{userId}/increment-loan
     */
    @PostMapping("/users/{userId}/increment-loan")
    public ResponseEntity<Map<String, String>> incrementLoanCount(@PathVariable Long userId) {
        try {
            membershipService.incrementLoanCount(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Loan count incremented successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Increment violation count for user
     * POST /api/membership/users/{userId}/increment-violation
     */
    @PostMapping("/users/{userId}/increment-violation")
    public ResponseEntity<Map<String, String>> incrementViolationCount(@PathVariable Long userId) {
        try {
            membershipService.incrementViolationCount(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Violation count incremented successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== STATISTICS ENDPOINTS ====================

    /**
     * Get all user memberships
     * GET /api/membership/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserMembershipDTO>> getAllUserMemberships() {
        try {
            List<UserMembershipDTO> memberships = membershipService.getAllUserMemberships();
            return ResponseEntity.ok(memberships);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get member count for a specific tier
     * GET /api/membership/tiers/{tierId}/count
     */
    @GetMapping("/tiers/{tierId}/count")
    public ResponseEntity<Map<String, Long>> getTierMemberCount(@PathVariable Long tierId) {
        try {
            Long count = membershipService.getTierMemberCount(tierId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
