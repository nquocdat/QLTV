package com.example.be_qltv.security;

import com.example.be_qltv.service.UserDetailsServiceImpl;
import com.example.be_qltv.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        System.out.println("AuthTokenFilter: Processing request: " + request.getMethod() + " " + request.getRequestURI());
        
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip JWT validation for public paths
        if (uri.startsWith("/uploads/")) {
            System.out.println("AuthTokenFilter: Public static resource /uploads/**, skip JWT validation");
            filterChain.doFilter(request, response);
            return;
        }
        
        // Chỉ bỏ qua xác thực cho GET /api/books/**
        if (uri.startsWith("/api/books/") && method.equalsIgnoreCase("GET")) {
            System.out.println("AuthTokenFilter: Public GET /api/books/**, skip JWT validation");
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String jwt = parseJwt(request);
            System.out.println("AuthTokenFilter: JWT token: " + (jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) + "..." : "null"));
            
            if (jwt != null && jwtUtil.validateJwtToken(jwt)) {
                String username = jwtUtil.getUserNameFromJwtToken(jwt);
                System.out.println("AuthTokenFilter: JWT valid, username: " + username);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("AuthTokenFilter: User authorities: " + userDetails.getAuthorities());
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("AuthTokenFilter: Authentication set for user: " + username);
            } else {
                System.out.println("AuthTokenFilter: JWT validation failed or no token");
            }
        } catch (Exception e) {
            System.err.println("AuthTokenFilter error: " + e.getMessage());
            e.printStackTrace();
        }
        filterChain.doFilter(request, response);
    }
    
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        System.out.println("AuthTokenFilter parseJwt: Authorization header: " + headerAuth);
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            System.out.println("AuthTokenFilter parseJwt: Extracted token: " + token.substring(0, Math.min(20, token.length())) + "...");
            return token;
        }
        
        System.out.println("AuthTokenFilter parseJwt: No valid Bearer token found");
        return null;
    }
}
