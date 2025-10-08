package com.example.be_qltv.service;

import com.example.be_qltv.config.VNPayConfig;
import com.example.be_qltv.entity.Fine;
import com.example.be_qltv.entity.LoanPayment;
import com.example.be_qltv.repository.FineRepository;
import com.example.be_qltv.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {
    
    private static final Logger log = LoggerFactory.getLogger(VNPayService.class);
    
    @Autowired
    private FineRepository fineRepository;
    
    /**
     * Tạo URL thanh toán VNPay cho phí phạt
     */
    public String createPaymentUrl(Fine fine, HttpServletRequest request) {
        log.info("========== BẮT ĐẦU TẠO URL THANH TOÁN VNPAY ==========");
        log.info("Fine ID: {}, Amount: {}", fine.getId(), fine.getAmount());
        
        String vnp_TxnRef = VNPayUtil.getRandomNumber(8);
        String vnp_IpAddr = VNPayUtil.getIpAddress(request);
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        
        // Convert BigDecimal to int (VND không có decimal)
        int amount = fine.getAmount().intValue();
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu x100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        
        // Thông tin đơn hàng
        String orderInfo = "Thanh toan phi phat ID:" + fine.getId();
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", VNPayConfig.vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        
        // Thời gian
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        
        // Sắp xếp params theo alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hashData: key KHÔNG encode, value PHẢI encode
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                
                // Build query: giống hashData
                query.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }
        
        // Tạo chữ ký
        String vnp_SecureHash = VNPayUtil.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + query.toString();
        
        log.info("Payment URL created successfully");
        log.info("Transaction Ref: {}", vnp_TxnRef);
        log.info("========== KẾT THÚC TẠO URL THANH TOÁN ==========");
        
        return paymentUrl;
    }
    
    /**
     * Xác thực kết quả thanh toán từ VNPay
     */
    @Transactional
    public Map<String, Object> verifyPaymentReturn(Map<String, String> params) {
        log.info("========== XÁC THỰC KẾT QUẢ THANH TOÁN ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        // Lấy chữ ký từ params
        String vnp_SecureHash = params.get("vnp_SecureHash");
        
        // Remove các field không tham gia hash
        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        
        // Sắp xếp và build hash string
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                sb.append(fieldName).append('=')
                  .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    sb.append('&');
                }
            }
        }
        
        // Tính hash
        String signValue = VNPayUtil.hmacSHA512(VNPayConfig.secretKey, sb.toString());
        
        log.info("Calculated hash: {}", signValue);
        log.info("Received hash: {}", vnp_SecureHash);
        
        // Verify chữ ký
        if (signValue.equals(vnp_SecureHash)) {
            String responseCode = params.get("vnp_ResponseCode");
            String orderInfo = params.get("vnp_OrderInfo");
            boolean isSuccess = "00".equals(responseCode);
            
            result.put("success", isSuccess);
            result.put("message", isSuccess ? "Thanh toán thành công" : "Thanh toán thất bại");
            result.put("transactionId", params.get("vnp_TransactionNo"));
            result.put("amount", params.get("vnp_Amount"));
            result.put("orderInfo", orderInfo);
            result.put("txnRef", params.get("vnp_TxnRef"));
            
            // Tự động cập nhật Fine status nếu thanh toán thành công
            if (isSuccess && orderInfo != null && orderInfo.contains("ID:")) {
                try {
                    String fineIdStr = orderInfo.split("ID:")[1].trim();
                    Long fineId = Long.parseLong(fineIdStr);
                    updateFineAfterPayment(fineId, params.get("vnp_TransactionNo"), responseCode);
                    result.put("fineId", fineId);
                    log.info("Fine #{} updated successfully", fineId);
                } catch (Exception e) {
                    log.error("Error updating Fine from orderInfo: {}", orderInfo, e);
                }
            }
            
            log.info("Payment verification: SUCCESS");
        } else {
            result.put("success", false);
            result.put("message", "Chữ ký không hợp lệ");
            log.error("Payment verification: FAILED - Invalid signature");
        }
        
        log.info("========== KẾT THÚC XÁC THỰC ==========");
        return result;
    }
    
    /**
     * Cập nhật trạng thái Fine sau khi thanh toán thành công
     */
    @Transactional
    public void updateFineAfterPayment(Long fineId, String transactionId, String transactionStatus) {
        Optional<Fine> fineOpt = fineRepository.findById(fineId);
        if (fineOpt.isPresent()) {
            Fine fine = fineOpt.get();
            fine.setStatus("PAID");
            fine.setPaidDate(java.time.LocalDate.now());
            // Có thể thêm field vnpayTransactionId nếu muốn lưu
            fineRepository.save(fine);
            log.info("Updated Fine #{} status to PAID", fineId);
        }
    }
    
    /**
     * Tạo URL thanh toán VNPay cho Loan Payment (phí đặt cọc mượn sách)
     */
    public String createLoanPaymentUrl(LoanPayment loanPayment, HttpServletRequest request) {
        log.info("========== BẮT ĐẦU TẠO URL THANH TOÁN LOAN VNPay ==========");
        log.info("Loan ID: {}, Amount: {}", loanPayment.getLoan().getId(), loanPayment.getAmount());
        
        // Tạo Order ID duy nhất: LOAN_{loanId}_{timestamp}
        String vnp_TxnRef = "LOAN_" + loanPayment.getLoan().getId() + "_" + System.currentTimeMillis();
        String vnp_IpAddr = VNPayUtil.getIpAddress(request);
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        
        // Convert BigDecimal to int (VND không có decimal)
        int amount = loanPayment.getAmount().intValue();
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu x100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        
        // Thông tin đơn hàng
        String orderInfo = "Phi dat coc muon sach: " + 
                          (loanPayment.getLoan().getBook() != null ? 
                           loanPayment.getLoan().getBook().getTitle() : "");
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "billpayment");
        vnp_Params.put("vnp_Locale", "vn");
        
        // Return URL cho loan payment - chuyển về frontend
        String returnUrl = "http://localhost:4200/library/payment-result";
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        
        // Thời gian
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        
        // Sắp xếp params theo alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                
                query.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }
        
        // Tạo chữ ký
        String vnp_SecureHash = VNPayUtil.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + query.toString();
        
        log.info("Loan Payment URL created successfully");
        log.info("Transaction Ref: {}", vnp_TxnRef);
        log.info("Return URL: {}", returnUrl);
        
        return paymentUrl;
    }
}
