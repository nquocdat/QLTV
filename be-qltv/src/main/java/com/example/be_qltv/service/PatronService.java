package com.example.be_qltv.service;

import com.example.be_qltv.dto.ChangePasswordRequest;
import com.example.be_qltv.dto.PatronDTO;
import com.example.be_qltv.dto.RegisterRequest;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.repository.PatronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatronService {

    @Autowired
    private PatronRepository patronRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<PatronDTO> getAllPatrons() {
        return patronRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<PatronDTO> getPatronById(Long id) {
        return patronRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<PatronDTO> getPatronByEmail(String email) {
        return patronRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    public PatronDTO createPatron(RegisterRequest registerRequest) {
        if (patronRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Patron patron = new Patron();
        patron.setName(registerRequest.getName());
        patron.setEmail(registerRequest.getEmail());
        patron.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        patron.setAddress(registerRequest.getAddress());
        patron.setPhoneNumber(registerRequest.getPhoneNumber());
        patron.setRole(Patron.Role.USER);
        patron.setIsActive(true);

        Patron savedPatron = patronRepository.save(patron);
        return convertToDTO(savedPatron);
    }

    public PatronDTO createPatron(PatronDTO patronDTO) {
        if (patronRepository.existsByEmail(patronDTO.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Patron patron = new Patron();
        patron.setName(patronDTO.getName());
        patron.setEmail(patronDTO.getEmail());
        
        // For admin-created users, use provided password or default
        String password = (patronDTO.getPassword() != null && !patronDTO.getPassword().isEmpty()) 
            ? patronDTO.getPassword() 
            : "password"; // Default password: "password"
        patron.setPassword(passwordEncoder.encode(password));
        
        System.out.println("Creating patron: " + patronDTO.getEmail() + " with password: " + password);
        
        patron.setAddress(patronDTO.getAddress());
        patron.setPhoneNumber(patronDTO.getPhoneNumber());
        
        // Set role from DTO, default to USER if not specified
        try {
            patron.setRole(Patron.Role.valueOf(patronDTO.getRole().toUpperCase()));
        } catch (Exception e) {
            patron.setRole(Patron.Role.USER);
        }
        
        patron.setIsActive(patronDTO.getIsActive() != null ? patronDTO.getIsActive() : true);

        Patron savedPatron = patronRepository.save(patron);
        return convertToDTO(savedPatron);
    }

    public Optional<PatronDTO> updatePatron(Long id, PatronDTO patronDTO) {
        return patronRepository.findById(id)
                .map(existingPatron -> {
                    updatePatronFromDTO(existingPatron, patronDTO);
                    Patron savedPatron = patronRepository.save(existingPatron);
                    return convertToDTO(savedPatron);
                });
    }

    public boolean deletePatron(Long id) {
        if (patronRepository.existsById(id)) {
            patronRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<PatronDTO> searchPatrons(String searchTerm) {
        return patronRepository.searchPatrons(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PatronDTO> getActivePatrons() {
        return patronRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PatronDTO> getPatronsByRole(String role) {
        Patron.Role patronRole = Patron.Role.valueOf(role.toUpperCase());
        return patronRepository.findByRole(patronRole).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============ PAGINATION METHODS ============

    public Page<PatronDTO> getAllPatronsWithPagination(Pageable pageable) {
        return patronRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public Page<PatronDTO> searchPatronsWithPagination(String searchTerm, Pageable pageable) {
        return patronRepository.searchPatrons(searchTerm, pageable)
                .map(this::convertToDTO);
    }

    public Page<PatronDTO> getActivePatronsWithPagination(Pageable pageable) {
        return patronRepository.findByIsActiveTrue(pageable)
                .map(this::convertToDTO);
    }

    public Page<PatronDTO> getPatronsByRoleWithPagination(String role, Pageable pageable) {
        Patron.Role patronRole = Patron.Role.valueOf(role.toUpperCase());
        return patronRepository.findByRole(patronRole, pageable)
                .map(this::convertToDTO);
    }

    // ============ EXISTING METHODS ============

    public boolean deactivatePatron(Long id) {
        Optional<Patron> patronOpt = patronRepository.findById(id);
        if (patronOpt.isPresent()) {
            Patron patron = patronOpt.get();
            patron.setIsActive(false);
            patronRepository.save(patron);
            return true;
        }
        return false;
    }

    public boolean activatePatron(Long id) {
        Optional<Patron> patronOpt = patronRepository.findById(id);
        if (patronOpt.isPresent()) {
            Patron patron = patronOpt.get();
            patron.setIsActive(true);
            patronRepository.save(patron);
            return true;
        }
        return false;
    }

    public Optional<PatronDTO> toggleStatus(Long id) {
        Optional<Patron> patronOpt = patronRepository.findById(id);
        if (patronOpt.isPresent()) {
            Patron patron = patronOpt.get();
            patron.setIsActive(!patron.getIsActive());
            Patron savedPatron = patronRepository.save(patron);
            return Optional.of(convertToDTO(savedPatron));
        }
        return Optional.empty();
    }

    public boolean updateRole(Long id, String role) {
        Optional<Patron> patronOpt = patronRepository.findById(id);
        if (patronOpt.isPresent()) {
            Patron patron = patronOpt.get();
            patron.setRole(Patron.Role.valueOf(role.toUpperCase()));
            patronRepository.save(patron);
            return true;
        }
        return false;
    }

    public boolean changePassword(Long id, ChangePasswordRequest request) {
        // Validate that new password matches confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match!");
        }

        Optional<Patron> patronOpt = patronRepository.findById(id);
        if (patronOpt.isEmpty()) {
            return false;
        }

        Patron patron = patronOpt.get();
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), patron.getPassword())) {
            throw new RuntimeException("Current password is incorrect!");
        }

        // Update password
        patron.setPassword(passwordEncoder.encode(request.getNewPassword()));
        patronRepository.save(patron);
        return true;
    }

    // Helper methods
    private PatronDTO convertToDTO(Patron patron) {
        PatronDTO dto = new PatronDTO();
        dto.setId(patron.getId());
        dto.setName(patron.getName());
        dto.setEmail(patron.getEmail());
        dto.setAddress(patron.getAddress());
        dto.setPhoneNumber(patron.getPhoneNumber());
        dto.setRole(patron.getRole().name());
        dto.setIsActive(patron.getIsActive());
        return dto;
    }

    private void updatePatronFromDTO(Patron patron, PatronDTO dto) {
        if (dto.getName() != null) {
            patron.setName(dto.getName());
        }
        if (dto.getAddress() != null) {
            patron.setAddress(dto.getAddress());
        }
        if (dto.getPhoneNumber() != null) {
            patron.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getIsActive() != null) {
            patron.setIsActive(dto.getIsActive());
        }
        if (dto.getRole() != null) {
            patron.setRole(Patron.Role.valueOf(dto.getRole().toUpperCase()));
        }
    }
}
