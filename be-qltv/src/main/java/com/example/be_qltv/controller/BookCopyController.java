package com.example.be_qltv.controller;

import com.example.be_qltv.dto.BookCopyDTO;
import com.example.be_qltv.service.BookCopyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/book-copies")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookCopyController {

    @Autowired
    private BookCopyService bookCopyService;

    /**
     * Lấy tất cả copies của một book
     * GET /api/book-copies/book/{bookId}
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<BookCopyDTO>> getCopiesByBookId(@PathVariable Long bookId) {
        try {
            List<BookCopyDTO> copies = bookCopyService.getCopiesByBookId(bookId);
            return ResponseEntity.ok(copies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy một copy by ID
     * GET /api/book-copies/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookCopyDTO> getCopyById(@PathVariable Long id) {
        return bookCopyService.getCopyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy copy available đầu tiên của book
     * GET /api/book-copies/book/{bookId}/available
     */
    @GetMapping("/book/{bookId}/available")
    public ResponseEntity<BookCopyDTO> getFirstAvailableCopy(@PathVariable Long bookId) {
        return bookCopyService.getFirstAvailableCopy(bookId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Đếm số copies available
     * GET /api/book-copies/book/{bookId}/available/count
     */
    @GetMapping("/book/{bookId}/available/count")
    public ResponseEntity<Map<String, Long>> countAvailableCopies(@PathVariable Long bookId) {
        Long count = bookCopyService.countAvailableCopies(bookId);
        return ResponseEntity.ok(Map.of("availableCount", count));
    }

    /**
     * Tìm copy theo barcode
     * GET /api/book-copies/barcode/{barcode}
     */
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<BookCopyDTO> getCopyByBarcode(@PathVariable String barcode) {
        return bookCopyService.getCopyByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo copy mới (Admin/Librarian only)
     * POST /api/book-copies/book/{bookId}
     */
    @PostMapping("/book/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<?> createCopy(@PathVariable Long bookId, @RequestBody BookCopyDTO copyDTO) {
        try {
            BookCopyDTO createdCopy = bookCopyService.createCopy(bookId, copyDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCopy);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create copy"));
        }
    }

    /**
     * Tạo nhiều copies cùng lúc (Admin/Librarian only)
     * POST /api/book-copies/book/{bookId}/bulk
     * Body: { "quantity": 5, "location": "Kệ A-12", "price": 150000 }
     */
    @PostMapping("/book/{bookId}/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<?> createMultipleCopies(
            @PathVariable Long bookId,
            @RequestBody Map<String, Object> request) {
        try {
            int quantity = (Integer) request.get("quantity");
            String location = (String) request.getOrDefault("location", "Kho chính");
            BigDecimal price = request.containsKey("price") 
                ? new BigDecimal(request.get("price").toString()) 
                : null;

            List<BookCopyDTO> copies = bookCopyService.createMultipleCopies(bookId, quantity, location, price);
            return ResponseEntity.status(HttpStatus.CREATED).body(copies);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create copies"));
        }
    }

    /**
     * Cập nhật copy (Admin/Librarian only)
     * PUT /api/book-copies/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<?> updateCopy(@PathVariable Long id, @RequestBody BookCopyDTO copyDTO) {
        try {
            BookCopyDTO updatedCopy = bookCopyService.updateCopy(id, copyDTO);
            return ResponseEntity.ok(updatedCopy);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update copy"));
        }
    }

    /**
     * Xóa copy (Admin only)
     * DELETE /api/book-copies/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCopy(@PathVariable Long id) {
        try {
            bookCopyService.deleteCopy(id);
            return ResponseEntity.ok(Map.of("message", "Copy deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete copy"));
        }
    }

    /**
     * Lấy tất cả copies (Admin/Librarian only)
     * GET /api/book-copies
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BookCopyDTO>> getAllCopies() {
        List<BookCopyDTO> copies = bookCopyService.getAllCopies();
        return ResponseEntity.ok(copies);
    }

    /**
     * Lấy copies theo status (Admin/Librarian only)
     * GET /api/book-copies/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<?> getCopiesByStatus(@PathVariable String status) {
        try {
            List<BookCopyDTO> copies = bookCopyService.getCopiesByStatus(status);
            return ResponseEntity.ok(copies);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy copies cần bảo trì (Admin/Librarian only)
     * GET /api/book-copies/maintenance
     */
    @GetMapping("/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<BookCopyDTO>> getCopiesNeedingMaintenance() {
        List<BookCopyDTO> copies = bookCopyService.getCopiesNeedingMaintenance();
        return ResponseEntity.ok(copies);
    }
}
