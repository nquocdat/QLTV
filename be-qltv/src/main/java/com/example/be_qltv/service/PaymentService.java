package com.example.be_qltv.service;

import com.example.be_qltv.config.VNPayConfig;
import com.example.be_qltv.entity.PaymentTransaction;
import com.example.be_qltv.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PaymentService {
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    public PaymentTransaction saveTransaction(PaymentTransaction transaction) {
        transaction.setCreatedDate(LocalDateTime.now());
        return paymentTransactionRepository.save(transaction);
    }

    public String createVNPayUrl(Long amount, String orderId, String orderInfo, String tmnCode, String hashSecret) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderType = "other";
        String vnp_CurrCode = "VND";
        String vnp_Locale = "vn";
        String vnp_TxnRef = orderId;
        String vnp_Amount = String.valueOf(amount * 100);
        String vnp_IpAddr = "127.0.0.1";
        String vnp_CreateDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String vnp_ExpireDate = java.time.LocalDateTime.now().plusMinutes(15).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        java.util.Map<String, String> vnp_Params = new java.util.HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", (tmnCode != null && !tmnCode.isEmpty()) ? tmnCode : VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // --- Tạo hash ---
        java.util.Map<String, String> sortedParams = new java.util.TreeMap<>();
        for (java.util.Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            if (!"vnp_SecureHash".equals(entry.getKey()) && !"vnp_SecureHashType".equals(entry.getKey()) && entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }
        sortedParams = new java.util.TreeMap<>(sortedParams); // sort ASC
        StringBuilder hashData = new StringBuilder();
        for (java.util.Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (hashData.length() > 0) hashData.append('&');
            hashData.append(entry.getKey()).append('=')
                    .append(entry.getValue());
        }
        String secret = (hashSecret != null && !hashSecret.isEmpty()) ? hashSecret : VNPayConfig.secretKey;
        String vnp_SecureHash = org.apache.commons.codec.digest.HmacUtils.hmacSha512Hex(
                secret, hashData.toString()
        );
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

        // --- Log debug VNPay ---
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PaymentService.class);
        logger.info("VNPay Params: {}", vnp_Params);
        logger.info("VNPay HashData: {}", hashData.toString());
        logger.info("VNPay SecureHash: {}", vnp_SecureHash);

        // --- Build URL ---
        StringBuilder url = new StringBuilder(VNPayConfig.vnp_PayUrl + "?");
        for (java.util.Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            url.append(entry.getKey()).append("=")
                    .append(java.net.URLEncoder.encode(entry.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                    .append("&");
        }
        url.setLength(url.length() - 1); // bỏ dấu &
        logger.info("VNPay URL: {}", url.toString());
        return url.toString();
    }

    // Xử lý callback IPN từ VNPay
    public void handleVNPayIpn(java.util.Map<String, String> params) {
        // TODO: Xác thực chữ ký, cập nhật trạng thái giao dịch vào DB
    }

    public boolean verifyVNPaySignature(java.util.Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        java.util.Map<String, String> sortedParams = new java.util.TreeMap<>();
        for (String key : params.keySet()) {
            if (!key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                sortedParams.put(key, params.get(key));
            }
        }
        StringBuilder hashData = new StringBuilder();
        for (java.util.Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (hashData.length() > 0) hashData.append('&');
            hashData.append(entry.getKey()).append('=').append(entry.getValue());
        }
        String calculatedHash = org.apache.commons.codec.digest.HmacUtils.hmacSha512Hex(VNPayConfig.secretKey, hashData.toString());
        return receivedHash != null && receivedHash.equalsIgnoreCase(calculatedHash);
    }

    // Xử lý callback return từ VNPay
    public String handleVNPayReturn(java.util.Map<String, String> params) {
        boolean valid = verifyVNPaySignature(params);
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        PaymentTransaction transaction = paymentTransactionRepository.findAll().stream()
            .filter(t -> txnRef.equals(t.getOrderId()))
            .findFirst().orElse(null);
        if (transaction != null) {
            transaction.setPaidDate(LocalDateTime.now());
            transaction.setResponseCode(responseCode);
            transaction.setRawResponse(params.toString());
            if (valid && "00".equals(responseCode)) {
                transaction.setStatus("SUCCESS");
                paymentTransactionRepository.save(transaction);
                return "Thanh toán thành công!";
            } else {
                transaction.setStatus("FAILED");
                paymentTransactionRepository.save(transaction);
                return "Thanh toán thất bại!";
            }
        }
        return "Không tìm thấy giao dịch!";
    }

    public PaymentTransaction confirmCashPayment(Long amount, String orderId, String description) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setPaymentMethod("CASH");
        transaction.setStatus("SUCCESS");
        transaction.setDescription(description);
        transaction.setCreatedDate(LocalDateTime.now());
        transaction.setPaidDate(LocalDateTime.now());
        return paymentTransactionRepository.save(transaction);
    }

    // Thêm các hàm xử lý sinh URL VNPay, xác thực callback, thống kê, báo cáo...
}
