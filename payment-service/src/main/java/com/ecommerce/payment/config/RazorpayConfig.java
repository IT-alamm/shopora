package com.ecommerce.payment.config;

import com.razorpay.RazorpayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.slf4j.*;

@Configuration
public class RazorpayConfig {

    private static final Logger log = LoggerFactory.getLogger(RazorpayConfig.class);

    @Bean
    public RazorpayClient razorpayClient(
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret) throws Exception {
        if (keyId.startsWith("rzp_test_dummy") || keySecret.startsWith("dummy-secret")) {
            log.warn("Razorpay TEST keys not set! Set RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET env vars. Using dummy client will fail on API calls (expected until keys are set).");
        }
        return new RazorpayClient(keyId, keySecret);
    }
}
