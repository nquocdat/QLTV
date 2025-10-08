package com.example.be_qltv.service;

import com.example.be_qltv.dto.BookCopyDTO;
import com.example.be_qltv.entity.Book;
import com.example.be_qltv.entity.BookCopy;
import com.example.be_qltv.repository.BookCopyRepository;
import com.example.be_qltv.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookCopyService {

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private BookRepository bookRepository;

    /**
     * Lấy tất cả copies của một book
     */
    public List<BookCopyDTO> getCopiesByBookId(Long bookId) {
        return bookCopyRepository.findByBookIdWithDetails(bookId).stream()
                .map(BookCopyDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Lấy copy by ID
     */
    public Optional<BookCopyDTO> getCopyById(Long id) {
        return bookCopyRepository.findById(id)
                .map(BookCopyDTO::new);
    }

    /**
     * Tìm copy available đầu tiên của book
     */
    public Optional<BookCopyDTO> getFirstAvailableCopy(Long bookId) {
        return bookCopyRepository.findFirstByBookIdAndStatusOrderByCopyNumberAsc(bookId, BookCopy.CopyStatus.AVAILABLE)
                .map(BookCopyDTO::new);
    }

    /**
     * Đếm số copies available
     */
    public Long countAvailableCopies(Long bookId) {
        return bookCopyRepository.countAvailableCopiesByBookId(bookId);
    }

    /**
     * Tìm copy theo barcode
     */
    public Optional<BookCopyDTO> getCopyByBarcode(String barcode) {
        return bookCopyRepository.findByBarcode(barcode)
                .map(BookCopyDTO::new);
    }

    /**
     * Tạo copy mới
     */
    public BookCopyDTO createCopy(Long bookId, BookCopyDTO copyDTO) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        // Generate copy number
        Integer maxCopyNumber = bookCopyRepository.findMaxCopyNumberByBookId(bookId);
        Integer newCopyNumber = (maxCopyNumber != null ? maxCopyNumber : 0) + 1;

        // Generate barcode if not provided
        String barcode = copyDTO.getBarcode();
        if (barcode == null || barcode.isEmpty()) {
            barcode = generateBarcode(book, newCopyNumber);
        }

        // Validate barcode uniqueness
        if (bookCopyRepository.existsByBarcode(barcode)) {
            throw new RuntimeException("Barcode already exists: " + barcode);
        }

        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setCopyNumber(newCopyNumber);
        copy.setBarcode(barcode);
        
        if (copyDTO.getConditionStatus() != null) {
            copy.setConditionStatus(BookCopy.ConditionStatus.valueOf(copyDTO.getConditionStatus()));
        }
        if (copyDTO.getStatus() != null) {
            copy.setStatus(BookCopy.CopyStatus.valueOf(copyDTO.getStatus()));
        }
        
        copy.setLocation(copyDTO.getLocation() != null ? copyDTO.getLocation() : "Kho chính");
        copy.setAcquisitionDate(copyDTO.getAcquisitionDate() != null ? copyDTO.getAcquisitionDate() : LocalDate.now());
        copy.setPrice(copyDTO.getPrice());
        copy.setNotes(copyDTO.getNotes());

        BookCopy savedCopy = bookCopyRepository.save(copy);
        
        // Update book's total_copies
        book.setTotalCopies(book.getTotalCopies() + 1);
        if (copy.getStatus() == BookCopy.CopyStatus.AVAILABLE) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        bookRepository.save(book);

        return new BookCopyDTO(savedCopy);
    }

    /**
     * Tạo nhiều copies cùng lúc
     */
    public List<BookCopyDTO> createMultipleCopies(Long bookId, int quantity, String location, BigDecimal price) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        Integer currentMaxCopyNumber = bookCopyRepository.findMaxCopyNumberByBookId(bookId);
        int startCopyNumber = (currentMaxCopyNumber != null ? currentMaxCopyNumber : 0) + 1;

        List<BookCopy> copies = new java.util.ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            int copyNumber = startCopyNumber + i;
            String barcode = generateBarcode(book, copyNumber);

            BookCopy copy = new BookCopy();
            copy.setBook(book);
            copy.setCopyNumber(copyNumber);
            copy.setBarcode(barcode);
            copy.setConditionStatus(BookCopy.ConditionStatus.GOOD);
            copy.setStatus(BookCopy.CopyStatus.AVAILABLE);
            copy.setLocation(location != null ? location : "Kho chính");
            copy.setAcquisitionDate(LocalDate.now());
            copy.setPrice(price);

            copies.add(copy);
        }

        List<BookCopy> savedCopies = bookCopyRepository.saveAll(copies);

        // Update book counts
        book.setTotalCopies(book.getTotalCopies() + quantity);
        book.setAvailableCopies(book.getAvailableCopies() + quantity);
        bookRepository.save(book);

        return savedCopies.stream()
                .map(BookCopyDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật copy
     */
    public BookCopyDTO updateCopy(Long id, BookCopyDTO copyDTO) {
        BookCopy copy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Copy not found with id: " + id));

        BookCopy.CopyStatus oldStatus = copy.getStatus();

        // Update fields
        if (copyDTO.getConditionStatus() != null) {
            copy.setConditionStatus(BookCopy.ConditionStatus.valueOf(copyDTO.getConditionStatus()));
        }
        if (copyDTO.getStatus() != null) {
            copy.setStatus(BookCopy.CopyStatus.valueOf(copyDTO.getStatus()));
        }
        if (copyDTO.getLocation() != null) {
            copy.setLocation(copyDTO.getLocation());
        }
        if (copyDTO.getPrice() != null) {
            copy.setPrice(copyDTO.getPrice());
        }
        if (copyDTO.getNotes() != null) {
            copy.setNotes(copyDTO.getNotes());
        }

        BookCopy updatedCopy = bookCopyRepository.save(copy);

        // Update book's available_copies if status changed
        if (oldStatus != updatedCopy.getStatus()) {
            updateBookAvailableCopies(updatedCopy.getBook().getId());
        }

        return new BookCopyDTO(updatedCopy);
    }

    /**
     * Xóa copy
     */
    public void deleteCopy(Long id) {
        BookCopy copy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Copy not found with id: " + id));

        if (copy.getStatus() == BookCopy.CopyStatus.BORROWED) {
            throw new RuntimeException("Cannot delete a borrowed copy");
        }

        Long bookId = copy.getBook().getId();
        bookCopyRepository.delete(copy);

        // Update book counts
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            book.setTotalCopies(book.getTotalCopies() - 1);
            if (copy.getStatus() == BookCopy.CopyStatus.AVAILABLE) {
                book.setAvailableCopies(book.getAvailableCopies() - 1);
            }
            bookRepository.save(book);
        }
    }

    /**
     * Update status của copy (dùng cho loan service)
     */
    public void updateCopyStatus(Long copyId, BookCopy.CopyStatus newStatus) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Copy not found with id: " + copyId));

        copy.setStatus(newStatus);
        bookCopyRepository.save(copy);

        // Update book's available_copies
        updateBookAvailableCopies(copy.getBook().getId());
    }

    /**
     * Get available copy entity (for LoanService)
     */
    public BookCopy getAvailableCopyEntity(Long bookId) {
        return bookCopyRepository.findFirstByBookIdAndStatusOrderByCopyNumberAsc(bookId, BookCopy.CopyStatus.AVAILABLE)
                .orElseThrow(() -> new RuntimeException("No available copy for book id: " + bookId));
    }

    /**
     * Lấy tất cả copies
     */
    public List<BookCopyDTO> getAllCopies() {
        return bookCopyRepository.findAll().stream()
                .map(BookCopyDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Tìm copies theo status
     */
    public List<BookCopyDTO> getCopiesByStatus(String status) {
        BookCopy.CopyStatus copyStatus = BookCopy.CopyStatus.valueOf(status);
        return bookCopyRepository.findByStatus(copyStatus).stream()
                .map(BookCopyDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Tìm copies cần bảo trì
     */
    public List<BookCopyDTO> getCopiesNeedingMaintenance() {
        return bookCopyRepository.findCopiesNeedingMaintenance().stream()
                .map(BookCopyDTO::new)
                .collect(Collectors.toList());
    }

    // Helper methods

    /**
     * Generate barcode cho copy
     */
    private String generateBarcode(Book book, Integer copyNumber) {
        String prefix = book.getIsbn() != null && !book.getIsbn().isEmpty() 
            ? book.getIsbn().substring(0, Math.min(8, book.getIsbn().length()))
            : String.format("%08d", book.getId());
        
        return String.format("%s-C%03d", prefix, copyNumber);
    }

    /**
     * Cập nhật số lượng available copies của book
     */
    private void updateBookAvailableCopies(Long bookId) {
        Long availableCount = bookCopyRepository.countAvailableCopiesByBookId(bookId);
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            book.setAvailableCopies(availableCount.intValue());
            bookRepository.save(book);
        }
    }
}
