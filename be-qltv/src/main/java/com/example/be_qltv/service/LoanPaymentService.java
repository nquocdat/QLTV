package com.example.be_qltv.service;

import com.example.be_qltv.dto.LoanPaymentDTO;
import com.example.be_qltv.entity.Book;
import com.example.be_qltv.entity.BookCopy;
import com.example.be_qltv.entity.Loan;
import com.example.be_qltv.entity.LoanPayment;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.repository.LoanPaymentRepository;
import com.example.be_qltv.repository.LoanRepository;
import com.example.be_qltv.repository.PatronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanPaymentService {
    
    @Autowired
    private LoanPaymentRepository loanPaymentRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private PatronRepository patronRepository;
    
    @Autowired
    private BookCopyService bookCopyService;
    
    /**
     * Lấy tất cả loan payments
     */
    public List<LoanPaymentDTO> getAllPayments() {
        return loanPaymentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy payment theo ID
     */
    public Optional<LoanPaymentDTO> getPaymentById(Long id) {
        return loanPaymentRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Lấy payment theo loan ID
     */
    public Optional<LoanPaymentDTO> getPaymentByLoanId(Long loanId) {
        return loanPaymentRepository.findByLoanId(loanId)
                .map(this::convertToDTO);
    }
    
    /**
     * Lấy tất cả payments của một patron
     */
    public List<LoanPaymentDTO> getPaymentsByPatronId(Long patronId) {
        return loanPaymentRepository.findByPatronId(patronId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách cash payments chờ xác nhận (cho Admin/Librarian)
     */
    public List<LoanPaymentDTO> getPendingCashPayments() {
        return loanPaymentRepository.findPendingCashPayments().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy pending payments của patron
     */
    public List<LoanPaymentDTO> getPendingPaymentsByPatronId(Long patronId) {
        return loanPaymentRepository.findPendingPaymentsByPatronId(patronId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Xác nhận thanh toán tiền mặt (Admin/Librarian)
     */
    public LoanPaymentDTO confirmCashPayment(Long paymentId, Long confirmedById) {
        LoanPayment payment = loanPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với ID: " + paymentId));
        
        if (payment.getPaymentMethod() != LoanPayment.PaymentMethod.CASH) {
            throw new RuntimeException("Chỉ thanh toán tiền mặt mới cần xác nhận thủ công");
        }
        
        if (payment.getPaymentStatus() != LoanPayment.PaymentStatus.PENDING) {
            throw new RuntimeException("Thanh toán này đã được xử lý");
        }
        
        // Tìm người xác nhận
        Patron confirmedBy = patronRepository.findById(confirmedById)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người xác nhận"));
        
        // Cập nhật payment status
        payment.setPaymentStatus(LoanPayment.PaymentStatus.CONFIRMED);
        payment.setConfirmedDate(LocalDateTime.now());
        payment.setConfirmedBy(confirmedBy);
        
        // Cập nhật loan status
        Loan loan = payment.getLoan();
        loan.setStatus(Loan.LoanStatus.BORROWED);
        loan.setUpdatedDate(java.time.LocalDate.now());
        
        // Cập nhật book copy status sang BORROWED
        if (loan.getBookCopy() != null) {
            bookCopyService.updateCopyStatus(loan.getBookCopy().getId(), BookCopy.CopyStatus.BORROWED);
        }
        // Note: Không cần giảm available_copies vì đã giảm khi tạo loan
        
        loanRepository.save(loan);
        LoanPayment savedPayment = loanPaymentRepository.save(payment);
        
        return convertToDTO(savedPayment);
    }
    
    /**
     * Xử lý callback từ VNPay
     */
    public LoanPaymentDTO processVNPayCallback(String orderId, String responseCode, 
                                                String transactionNo, String bankCode) {
        // Extract loan ID from orderId (format: LOAN_{loanId}_{timestamp})
        Long loanId = extractLoanIdFromOrderId(orderId);
        
        LoanPayment payment = loanPaymentRepository.findByLoanId(loanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán cho loan ID: " + loanId));
        
        if (payment.getPaymentMethod() != LoanPayment.PaymentMethod.VNPAY) {
            throw new RuntimeException("Payment method không phải VNPay");
        }
        
        // Cập nhật thông tin từ VNPay
        payment.setTransactionNo(transactionNo);
        payment.setBankCode(bankCode);
        payment.setVnpayResponseCode(responseCode);
        
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            payment.setPaymentStatus(LoanPayment.PaymentStatus.CONFIRMED);
            payment.setConfirmedDate(LocalDateTime.now());
            
            // Cập nhật loan status
            Loan loan = payment.getLoan();
            loan.setStatus(Loan.LoanStatus.BORROWED);
            loan.setUpdatedDate(java.time.LocalDate.now());
            
            // Cập nhật book copy status sang BORROWED
            if (loan.getBookCopy() != null) {
                bookCopyService.updateCopyStatus(loan.getBookCopy().getId(), BookCopy.CopyStatus.BORROWED);
            }
            // Note: Không cần giảm available_copies vì đã giảm khi tạo loan
            
            loanRepository.save(loan);
        } else {
            // Thanh toán thất bại
            payment.setPaymentStatus(LoanPayment.PaymentStatus.FAILED);
            
            // Hoàn lại book copy và available copies
            Loan loan = payment.getLoan();
            
            if (loan.getBookCopy() != null) {
                // Hoàn lại book copy status về AVAILABLE
                bookCopyService.updateCopyStatus(loan.getBookCopy().getId(), BookCopy.CopyStatus.AVAILABLE);
            } else {
                // Fallback: Tăng lại available_copies (cho dữ liệu cũ)
                Book book = loan.getBook();
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                if (book.getAvailableCopies() > 0) {
                    book.setStatus(com.example.be_qltv.enums.BookStatus.AVAILABLE);
                }
            }
            
            // Xóa loan vì thanh toán thất bại
            loanRepository.delete(loan);
        }
        
        LoanPayment savedPayment = loanPaymentRepository.save(payment);
        return convertToDTO(savedPayment);
    }
    
    /**
     * Hủy payment và loan nếu user không thanh toán
     */
    public void cancelPayment(Long paymentId) {
        LoanPayment payment = loanPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));
        
        if (payment.getPaymentStatus() != LoanPayment.PaymentStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy payment đang pending");
        }
        
        payment.setPaymentStatus(LoanPayment.PaymentStatus.FAILED);
        loanPaymentRepository.save(payment);
        
        // Hoàn lại book copy và available copies
        Loan loan = payment.getLoan();
        
        if (loan.getBookCopy() != null) {
            // Hoàn lại book copy status về AVAILABLE
            bookCopyService.updateCopyStatus(loan.getBookCopy().getId(), BookCopy.CopyStatus.AVAILABLE);
        } else {
            // Fallback: Tăng lại available_copies (cho dữ liệu cũ)
            Book book = loan.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            if (book.getAvailableCopies() > 0) {
                book.setStatus(com.example.be_qltv.enums.BookStatus.AVAILABLE);
            }
        }
        
        // Xóa loan
        loanRepository.delete(loan);
    }
    
    /**
     * Đếm số lượng payments chờ xác nhận
     */
    public Long countPendingPayments() {
        return loanPaymentRepository.countPendingPayments();
    }
    
    /**
     * Extract loan ID from VNPay order ID
     */
    private Long extractLoanIdFromOrderId(String orderId) {
        try {
            // Format: LOAN_{loanId}_{timestamp}
            String[] parts = orderId.split("_");
            if (parts.length >= 2) {
                return Long.parseLong(parts[1]);
            }
            throw new RuntimeException("Invalid order ID format: " + orderId);
        } catch (Exception e) {
            throw new RuntimeException("Không thể parse loan ID từ order ID: " + orderId, e);
        }
    }
    
    /**
     * Convert Entity to DTO
     */
    private LoanPaymentDTO convertToDTO(LoanPayment payment) {
        LoanPaymentDTO dto = new LoanPaymentDTO();
        dto.setId(payment.getId());
        dto.setLoanId(payment.getLoan().getId());
        dto.setPatronId(payment.getPatron().getId());
        dto.setPatronName(payment.getPatron().getName());
        dto.setPatronEmail(payment.getPatron().getEmail());
        
        if (payment.getLoan() != null && payment.getLoan().getBook() != null) {
            dto.setBookId(payment.getLoan().getBook().getId());
            dto.setBookTitle(payment.getLoan().getBook().getTitle());
        }
        
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod().name());
        dto.setPaymentStatus(payment.getPaymentStatus().name());
        dto.setTransactionNo(payment.getTransactionNo());
        dto.setBankCode(payment.getBankCode());
        dto.setVnpayResponseCode(payment.getVnpayResponseCode());
        dto.setDescription(payment.getDescription());
        dto.setCreatedDate(payment.getCreatedDate());
        dto.setConfirmedDate(payment.getConfirmedDate());
        
        if (payment.getConfirmedBy() != null) {
            dto.setConfirmedBy(payment.getConfirmedBy().getId());
            dto.setConfirmedByName(payment.getConfirmedBy().getName());
        }
        
        dto.setUpdatedDate(payment.getUpdatedDate());
        
        return dto;
    }
}
