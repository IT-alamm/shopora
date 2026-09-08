package com.ecommerce.payment.service;

import com.ecommerce.payment.client.OrderClient;
import com.ecommerce.payment.dto.PaymentDtos.*;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository payments;
    private final RazorpayClient razorpayClient;
    private final OrderClient orderClient;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    /**
     * Creates a Razorpay Order (TEST mode when test keys are used).
     * Amount is converted to paise (INR smallest unit).
     */
    @Transactional
    public CreateOrderResponse createRazorpayOrder(CreateOrderRequest req) throws Exception {
        long amountPaise = req.getAmount().multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).longValueExact();

        JSONObject options = new JSONObject();
        options.put("amount", amountPaise);
        options.put("currency", "INR");
        options.put("receipt", "order_" + req.getOrderId() + "_" + System.currentTimeMillis());
        options.put("payment_capture", 1);

        log.info("Payment initiated: orderId={} amount={}", req.getOrderId(), req.getAmount());
        Order rzpOrder = razorpayClient.orders.create(options);
        String rzpOrderId = rzpOrder.get("id");

        Payment payment = Payment.builder()
                .orderId(req.getOrderId())
                .razorpayOrderId(rzpOrderId)
                .amount(req.getAmount())
                .currency("INR")
                .status("CREATED")
                .build();
        payment = payments.save(payment);

        // NOTE: only keyId + razorpayOrderId go to frontend. Secret NEVER leaves backend.
        return CreateOrderResponse.builder()
                .paymentId(payment.getId())
                .orderId(req.getOrderId())
                .razorpayOrderId(rzpOrderId)
                .keyId(keyId)
                .amount(req.getAmount())
                .currency("INR")
                .status("CREATED")
                .build();
    }

    /** Verifies Razorpay signature: HMAC_SHA256(order_id + "|" + payment_id, key_secret). */
    @Transactional
    public VerifyResponse verifyPayment(VerifyRequest req) {
        String data = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();
        String expected = hmacSha256(data, keySecret);
        boolean valid = expected != null && expected.equals(req.getRazorpaySignature());

        Payment payment = payments.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElse(null);
        if (payment != null) {
            payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
            payment.setSignature(req.getRazorpaySignature());
            payment.setStatus(valid ? "SUCCESS" : "FAILED");
            payments.save(payment);
        }
        if (valid) {
            log.info("Payment successful: razorpayPaymentId={}", req.getRazorpayPaymentId());
            // Auto-confirm the order so it leaves PAYMENT_PENDING without manual step.
            // Best-effort: payment stays SUCCESS even if order-service is briefly down.
            if (payment != null) {
                try {
                    orderClient.updateStatus(payment.getOrderId(), "CONFIRMED");
                    log.info("Order confirmed: orderId={}", payment.getOrderId());
                } catch (Exception e) {
                    log.error("Order auto-confirm failed: orderId={} err={}", payment.getOrderId(), e.getMessage());
                }
            }
            return VerifyResponse.builder().success(true).status("SUCCESS").message("Payment verified").build();
        }
        log.error("Payment verification failed: razorpayOrderId={}", req.getRazorpayOrderId());
        return VerifyResponse.builder().success(false).status("FAILED").message("Invalid signature").build();
    }

    public static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
