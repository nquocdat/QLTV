package com.example.be_qltv.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    
    // VNPay Payment URL
    public static final String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    
    // VNPay Configuration
    public static final String vnp_TmnCode = "NDYCNE7G";
    public static final String secretKey = "6QLSH3HHOHZJK72EQNXCYVEP41JI8779";
    public static final String vnp_ReturnUrl = "http://localhost:8081/api/payment/vnpay-return";
    public static final String vnp_Version = "2.1.0";
    public static final String vnp_Command = "pay";
    public static final String vnp_OrderType = "other";
}
