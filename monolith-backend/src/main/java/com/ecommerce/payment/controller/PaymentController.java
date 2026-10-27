package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentDtos.*;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    /** Frontend calls this after OrderService creates a PENDING order. Returns keyId + rzp order id (no secret). */
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest req) throws Exception {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(req));
    }

    /** Frontend sends Razorpay checkout result here for signature verification. */
    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(@Valid @RequestBody VerifyRequest req) {
        return ResponseEntity.ok(paymentService.verifyPayment(req));
    }

    /**
     * Razorpay webhook for reconciliation (payment.captured, payment.failed, refund.*).
     * Configure URL in Razorpay dashboard TEST mode + set RAZORPAY_WEBHOOK_SECRET.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload,
                                          @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            String expected = PaymentService.hmacSha256(payload, webhookSecret);
            if (expected == null || !expected.equals(signature)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid webhook signature");
            }
        }
        // TODO: parse payload event -> update payment + notify order-service (Feign/event).
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/health")
    public String health() { return "payment-service OK (Razorpay TEST mode)"; }
}
