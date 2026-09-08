package com.ecommerce.payment.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

public class PaymentDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateOrderRequest {
        @NotNull private Long orderId;
        @NotNull @DecimalMin("1.0") private BigDecimal amount;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateOrderResponse {
        private Long paymentId;
        private Long orderId;
        private String razorpayOrderId;
        private String keyId;      // safe to expose to frontend
        private BigDecimal amount;
        private String currency;
        private String status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VerifyRequest {
        @NotBlank private String razorpayOrderId;
        @NotBlank private String razorpayPaymentId;
        @NotBlank private String razorpaySignature;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VerifyResponse {
        private boolean success;
        private String status;
        private String message;
    }
}
