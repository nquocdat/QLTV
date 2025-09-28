package com.example.be_qltv.service;

import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.repository.PatronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    PatronRepository patronRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("UserDetailsService: Looking for user with email: " + email);
        
        Patron patron = patronRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("UserDetailsService: User not found with email: " + email);
                    return new UsernameNotFoundException("User Not Found with email: " + email);
                });
        
        System.out.println("UserDetailsService: Found user: " + patron.getName() + " with role: " + patron.getRole());
        System.out.println("UserDetailsService: User password hash starts with: " + (patron.getPassword() != null ? patron.getPassword().substring(0, 10) + "..." : "null"));
        
        return UserPrincipal.create(patron);
    }
}
