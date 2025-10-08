package com.example.be_qltv.service;

import com.example.be_qltv.dto.PublisherDTO;
import com.example.be_qltv.entity.Publisher;
import com.example.be_qltv.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PublisherService {

    @Autowired
    private PublisherRepository publisherRepository;

    public List<PublisherDTO> getAllPublishers() {
        List<Publisher> publishers = publisherRepository.findAll();
        return publishers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<PublisherDTO> getAllPublishersWithPagination(Pageable pageable) {
        Page<Publisher> publishers = publisherRepository.findAll(pageable);
        List<PublisherDTO> publisherDTOs = publishers.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(publisherDTOs, pageable, publishers.getTotalElements());
    }

    public Optional<PublisherDTO> getPublisherById(Long id) {
        return publisherRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<PublisherDTO> getPublisherByName(String name) {
        return publisherRepository.findByName(name)
                .map(this::convertToDTO);
    }

    public List<PublisherDTO> searchPublishers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPublishers();
        }
        List<Publisher> publishers = publisherRepository.searchPublishers(query.trim());
        return publishers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<String> getPublisherSuggestions(String partialName) {
        if (partialName == null || partialName.trim().isEmpty()) {
            return List.of();
        }
        List<Publisher> publishers = publisherRepository.findByNameContainingIgnoreCase(partialName.trim());
        return publishers.stream()
                .map(Publisher::getName)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    public PublisherDTO createPublisher(PublisherDTO publisherDTO) {
        Publisher publisher = convertToEntity(publisherDTO);
        publisher.setCreatedDate(LocalDateTime.now());
        publisher.setUpdatedDate(LocalDateTime.now());
        Publisher savedPublisher = publisherRepository.save(publisher);
        return convertToDTO(savedPublisher);
    }

    public PublisherDTO updatePublisher(Long id, PublisherDTO publisherDTO) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found with id: " + id));
        
        publisher.setName(publisherDTO.getName());
        publisher.setAddress(publisherDTO.getAddress());
        publisher.setPhone(publisherDTO.getPhone());
        publisher.setEmail(publisherDTO.getEmail());
        publisher.setWebsite(publisherDTO.getWebsite());
        publisher.setCountry(publisherDTO.getCountry());
        publisher.setEstablishedYear(publisherDTO.getEstablishedYear());
        publisher.setDescription(publisherDTO.getDescription());
        publisher.setUpdatedDate(LocalDateTime.now());
        
        Publisher updatedPublisher = publisherRepository.save(publisher);
        return convertToDTO(updatedPublisher);
    }

    public boolean deletePublisher(Long id) {
        if (publisherRepository.existsById(id)) {
            publisherRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Publisher> getPublishersByCountry(String country) {
        return publisherRepository.findByCountry(country);
    }

    public List<Publisher> getPublishersWithBooks() {
        return publisherRepository.findPublishersWithBooks();
    }

    public List<Publisher> getPublishersByEstablishedYearRange(Integer startYear, Integer endYear) {
        return publisherRepository.findByEstablishedYearBetween(startYear, endYear);
    }

    // Legacy methods for backward compatibility
    public Publisher getOrCreatePublisher(String publisherName) {
        if (publisherName == null || publisherName.trim().isEmpty()) {
            return null;
        }

        Optional<Publisher> existingPublisher = publisherRepository.findByName(publisherName.trim());
        if (existingPublisher.isPresent()) {
            return existingPublisher.get();
        }

        // Create new publisher with minimal information
        Publisher newPublisher = new Publisher();
        newPublisher.setName(publisherName.trim());
        newPublisher.setDescription("Publisher created automatically");
        newPublisher.setCreatedDate(LocalDateTime.now());
        newPublisher.setUpdatedDate(LocalDateTime.now());
        return publisherRepository.save(newPublisher);
    }
    
    private PublisherDTO convertToDTO(Publisher publisher) {
        PublisherDTO dto = new PublisherDTO();
        dto.setId(publisher.getId());
        dto.setName(publisher.getName());
        dto.setAddress(publisher.getAddress());
        dto.setPhone(publisher.getPhone());
        dto.setEmail(publisher.getEmail());
        dto.setWebsite(publisher.getWebsite());
        dto.setCountry(publisher.getCountry());
        dto.setEstablishedYear(publisher.getEstablishedYear());
        dto.setDescription(publisher.getDescription());
        dto.setCreatedDate(publisher.getCreatedDate());
        return dto;
    }
    
    private Publisher convertToEntity(PublisherDTO dto) {
        Publisher publisher = new Publisher();
        if (dto.getId() != null) {
            publisher.setId(dto.getId());
        }
        publisher.setName(dto.getName());
        publisher.setAddress(dto.getAddress());
        publisher.setPhone(dto.getPhone());
        publisher.setEmail(dto.getEmail());
        publisher.setWebsite(dto.getWebsite());
        publisher.setCountry(dto.getCountry());
        publisher.setEstablishedYear(dto.getEstablishedYear());
        publisher.setDescription(dto.getDescription());
        return publisher;
    }
}
