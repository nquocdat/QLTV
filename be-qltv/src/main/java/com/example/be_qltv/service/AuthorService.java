package com.example.be_qltv.service;

import com.example.be_qltv.dto.AuthorDTO;
import com.example.be_qltv.entity.Author;
import com.example.be_qltv.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthorService {
    
    @Autowired
    private AuthorRepository authorRepository;
    
    public List<AuthorDTO> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<AuthorDTO> getAllAuthorsWithPagination(Pageable pageable) {
        Page<Author> authors = authorRepository.findAll(pageable);
        return authors.map(this::convertToDTO);
    }
    
    public List<AuthorDTO> searchAuthors(String query) {
        return authorRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<AuthorDTO> searchAuthorsWithPagination(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            // Trả về tất cả tác giả nếu không có từ khóa
            return authorRepository.findAll(pageable).map(this::convertToDTO);
        }
        return authorRepository.findByNameContainingIgnoreCase(query, pageable).map(this::convertToDTO);
    }
    
    public List<String> getAuthorSuggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return authorRepository.findByNameContainingIgnoreCase(query).stream()
                .map(Author::getName)
                .limit(10)
                .collect(Collectors.toList());
    }
    
    public AuthorDTO createAuthor(AuthorDTO authorDTO) {
        Author author = convertToEntity(authorDTO);
        Author savedAuthor = authorRepository.save(author);
        return convertToDTO(savedAuthor);
    }
    
    public AuthorDTO updateAuthor(Long id, AuthorDTO authorDTO) {
        Optional<Author> existingAuthor = authorRepository.findById(id);
        if (existingAuthor.isPresent()) {
            Author author = existingAuthor.get();
            author.setName(authorDTO.getName());
            author.setBiography(authorDTO.getBiography());
            author.setBirthDate(authorDTO.getBirthDate());
            author.setNationality(authorDTO.getNationality());
            Author savedAuthor = authorRepository.save(author);
            return convertToDTO(savedAuthor);
        }
        throw new RuntimeException("Author not found with id: " + id);
    }
    
    public void deleteAuthor(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new RuntimeException("Author not found with id: " + id);
        }
        authorRepository.deleteById(id);
    }
    
    public Optional<Author> findOrCreateAuthor(String authorName) {
        Optional<Author> existingAuthor = authorRepository.findByName(authorName);
        if (existingAuthor.isPresent()) {
            return existingAuthor;
        }
        
        Author newAuthor = new Author(authorName);
        Author savedAuthor = authorRepository.save(newAuthor);
        return Optional.of(savedAuthor);
    }
    
    private AuthorDTO convertToDTO(Author author) {
        if (author == null) return null;
        
        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setName(author.getName());
        dto.setBiography(author.getBiography());
        dto.setBirthDate(author.getBirthDate());
        dto.setNationality(author.getNationality());
        return dto;
    }
    
    private Author convertToEntity(AuthorDTO dto) {
        if (dto == null) return null;
        
        Author author = new Author();
        author.setId(dto.getId());
        author.setName(dto.getName());
        author.setBiography(dto.getBiography());
        author.setBirthDate(dto.getBirthDate());
        author.setNationality(dto.getNationality());
        return author;
    }
}
