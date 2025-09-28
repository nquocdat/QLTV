package com.example.be_qltv.controller;

import com.example.be_qltv.dto.JwtResponse;
import com.example.be_qltv.dto.LoginRequest;
import com.example.be_qltv.dto.PatronDTO;
import com.example.be_qltv.dto.RegisterRequest;
import com.example.be_qltv.service.PatronService;
import com.example.be_qltv.service.UserPrincipal;
import com.example.be_qltv.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    PatronService patronService;
    
    @Autowired
    JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Debug: Log received data
        System.out.println("Backend received login request:");
        System.out.println("Email: " + loginRequest.getEmail());
        System.out.println("Password length: " + (loginRequest.getPassword() != null ? loginRequest.getPassword().length() : "null"));
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateJwtToken(authentication);
            
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            
            System.out.println("Authentication successful for user: " + userDetails.getUsername());
            
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getName(),
                    userDetails.getUsername(),
                    userDetails.getAuthorities().iterator().next().getAuthority()));
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Lỗi: Email hoặc mật khẩu không đúng!");
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            PatronDTO patronDTO = patronService.createPatron(registerRequest);
            return ResponseEntity.ok(patronDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
}
