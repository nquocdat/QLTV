package com.example.be_qltv.controller;

import com.example.be_qltv.dto.AuthorDTO;
import com.example.be_qltv.dto.BookDTO;
import com.example.be_qltv.dto.BookStatisticsDTO;
import com.example.be_qltv.dto.CategoryDTO;
import com.example.be_qltv.dto.PublisherDTO;
import com.example.be_qltv.service.BookService;
import com.example.be_qltv.service.AuthorService;
import com.example.be_qltv.service.CategoryService;
import com.example.be_qltv.service.PublisherService;
import com.example.be_qltv.service.FileUploadService;
import com.example.be_qltv.enums.BookStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/books")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private PublisherService publisherService;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<BookStatisticsDTO> getBookStatistics() {
        BookStatisticsDTO statistics = bookService.getBookStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<BookDTO>> getFeaturedBooks() {
        List<BookDTO> featuredBooks = bookService.getFeaturedBooks();
        return ResponseEntity.ok(featuredBooks);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<BookDTO>> getRecentBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<BookDTO> recentBooks = bookService.getRecentBooks(PageRequest.of(page, size));
        return ResponseEntity.ok(recentBooks);
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<BookDTO>> getAllBooksWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        // Chỉ cho phép sortBy là các trường hợp lệ
        String[] allowedSortFields = {"id", "title", "createdDate", "publishedDate", "isbn"};
        boolean validSort = false;
        for (String field : allowedSortFields) {
            if (field.equalsIgnoreCase(sortBy)) {
                validSort = true;
                break;
            }
        }
        if (!validSort) {
            return ResponseEntity.badRequest().body(Page.empty());
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<BookDTO> books = bookService.getAllBooksWithFilters(
            searchTerm,
            categoryId,
            status != null ? BookStatus.valueOf(status.toUpperCase()) : null,
            authorId,
            publisherId,
            yearFrom,
            yearTo,
            pageable
        );
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        Optional<BookDTO> book = bookService.getBookById(id);
        return book.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) {
        try {
            BookDTO createdBook = bookService.createBook(bookDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO bookDTO) {
        try {
            Optional<BookDTO> updatedBook = bookService.updateBook(id, bookDTO);
            return updatedBook.map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            boolean deleted = bookService.deleteBook(id);
            return deleted ? ResponseEntity.noContent().build() 
                         : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/authors")
    public ResponseEntity<List<AuthorDTO>> searchAuthors(@RequestParam String query) {
        List<AuthorDTO> authors = authorService.searchAuthors(query);
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/search/categories") 
    public ResponseEntity<List<CategoryDTO>> searchCategories(@RequestParam String query) {
        List<CategoryDTO> categories = categoryService.searchCategories(query);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search/publishers")
    public ResponseEntity<List<PublisherDTO>> searchPublishers(@RequestParam String query) {
        List<PublisherDTO> publishers = publisherService.searchPublishers(query);
        return ResponseEntity.ok(publishers);
    }

    @GetMapping("/status/summary")
    public ResponseEntity<Map<String, Long>> getStatusSummary() {
        Map<String, Long> summary = bookService.getStatusSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<Page<BookDTO>> getBooksByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            BookStatus bookStatus = BookStatus.valueOf(status.toUpperCase());
            Page<BookDTO> books = bookService.getBooksByStatus(bookStatus, 
                PageRequest.of(page, size));
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/paginated")
    public ResponseEntity<Page<BookDTO>> searchBooksPaginated(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<BookDTO> books = bookService.searchBooksWithPagination(q, pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<Page<BookDTO>> getBooksByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookDTO> books = bookService.getBooksByCategory(categoryId, 
            PageRequest.of(page, size));
        return ResponseEntity.ok(books);
    }

    @GetMapping("/by-author/{authorId}")
    public ResponseEntity<Page<BookDTO>> getBooksByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookDTO> books = bookService.getBooksByAuthor(authorId, 
            PageRequest.of(page, size));
        return ResponseEntity.ok(books);
    }

    @GetMapping("/by-publisher/{publisherId}")
    public ResponseEntity<Page<BookDTO>> getBooksByPublisher(
            @PathVariable Long publisherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookDTO> books = bookService.getBooksByPublisher(publisherId, 
            PageRequest.of(page, size));
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<BookDTO>> searchBooks(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> books = bookService.searchBooksWithPagination(q, pageable);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<BookDTO>> getAvailableBooks() {
        List<BookDTO> books = bookService.getAvailableBooks();
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/available/paginated")
    public ResponseEntity<Page<BookDTO>> getAvailableBooksWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BookDTO> books = bookService.getAvailableBooksWithPagination(pageable);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/authors/suggestions")
    public ResponseEntity<List<String>> getAuthorSuggestions(@RequestParam String q) {
        List<String> suggestions = bookService.getAuthorSuggestions(q);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/publishers/suggestions")  
    public ResponseEntity<List<String>> getPublisherSuggestions(@RequestParam String q) {
        List<String> suggestions = bookService.getPublisherSuggestions(q);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/categories/suggestions")
    public ResponseEntity<List<String>> getCategorySuggestions(@RequestParam String q) {
        List<String> suggestions = bookService.getCategorySuggestions(q);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/genres/suggestions") 
    public ResponseEntity<List<String>> getGenreSuggestions(@RequestParam String q) {
        List<String> suggestions = bookService.getGenreSuggestions(q);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/most-borrowed")
    public ResponseEntity<Page<BookDTO>> getMostBorrowedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookDTO> books = bookService.getFavoriteBooks(PageRequest.of(page, size));
        return ResponseEntity.ok(books);
    }
    
    /**
     * Upload book cover image
     * POST /api/books/upload-cover
     */
    @PostMapping("/upload-cover")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<?> uploadCoverImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileUploadService.uploadFile(file);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
