package com.example.be_qltv.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class để generate BCrypt password hash
 * Chạy file này để tạo password hash cho tài khoản mới
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Danh sách passwords cần hash
        String[] passwords = {
            "password",      // Password mặc định
            "admin123",      // Admin password
            "librarian123",  // Librarian password
            "user123"        // User password
        };
        
        System.out.println("=".repeat(80));
        System.out.println("BCRYPT PASSWORD HASH GENERATOR");
        System.out.println("=".repeat(80));
        System.out.println();
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            
            System.out.println("Plain text: " + password);
            System.out.println("BCrypt hash: " + hash);
            System.out.println("Hash length: " + hash.length());
            
            // Verify hash
            boolean matches = encoder.matches(password, hash);
            System.out.println("Verification: " + (matches ? "✅ VALID" : "❌ INVALID"));
            System.out.println("-".repeat(80));
        }
        
        System.out.println();
        System.out.println("SQL UPDATE STATEMENTS:");
        System.out.println("=".repeat(80));
        
        // Generate SQL statements
        String hash = encoder.encode("password");
        System.out.println("-- Update Librarian accounts with password='password'");
        System.out.println("UPDATE patron SET password = '" + hash + "' WHERE email = 'librarian1@qltv.com';");
        System.out.println("UPDATE patron SET password = '" + hash + "' WHERE email = 'librarian2@qltv.com';");
        System.out.println();
        
        System.out.println("-- Test query");
        System.out.println("SELECT id, name, email, role, LEFT(password, 30) as pwd FROM patron WHERE role = 'LIBRARIAN';");
    }
}
