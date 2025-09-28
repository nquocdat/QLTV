package com.example.be_qltv.controller;

import com.example.be_qltv.dto.PublisherDTO;
import com.example.be_qltv.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    @Autowired
    private PublisherService publisherService;
    
    @GetMapping
    public ResponseEntity<List<PublisherDTO>> getAllPublishers() {
        List<PublisherDTO> publishers = publisherService.getAllPublishers();
        return ResponseEntity.ok(publishers);
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<PublisherDTO>> getAllPublishersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PublisherDTO> publishers = publisherService.getAllPublishersWithPagination(pageable);
        return ResponseEntity.ok(publishers);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PublisherDTO>> searchPublishers(@RequestParam String q) {
        List<PublisherDTO> publishers = publisherService.searchPublishers(q);
        return ResponseEntity.ok(publishers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherDTO> getPublisherById(@PathVariable Long id) {
        Optional<PublisherDTO> publisher = publisherService.getPublisherById(id);
        return publisher.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getPublisherSuggestions(@RequestParam String q) {
        List<String> suggestions = publisherService.getPublisherSuggestions(q);
        return ResponseEntity.ok(suggestions);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublisherDTO> createPublisher(@RequestBody PublisherDTO publisherDTO) {
        try {
            PublisherDTO createdPublisher = publisherService.createPublisher(publisherDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPublisher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublisherDTO> updatePublisher(@PathVariable Long id, @RequestBody PublisherDTO publisherDTO) {
        try {
            PublisherDTO updatedPublisher = publisherService.updatePublisher(id, publisherDTO);
            return updatedPublisher != null ? ResponseEntity.ok(updatedPublisher) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePublisher(@PathVariable Long id) {
        try {
            boolean deleted = publisherService.deletePublisher(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
