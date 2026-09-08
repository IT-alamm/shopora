# E-Commerce Backend (Spring Boot Microservices + Razorpay TEST mode)

## Services / Ports
- eureka-server 8761
- api-gateway 8080 (routes /api/**, JWT filter)
- auth-service 8081 (register/login/JWT done)
- product-service 8082 (skeleton)
- cart-service 8083 (skeleton)
- order-service 8084 (skeleton)
- payment-service 8085 (Razorpay TEST create/verify/webhook done)
- user-service 8086 (skeleton)

## Prerequisites
- Java 17, Maven 3.9+, MySQL80 running
- Razorpay TEST keys from https://dashboard.razorpay.com/app/keys (Test Mode)

## Env vars (do NOT commit secrets)
```powershell
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="YOUR_MYSQL_PASSWORD"
$env:JWT_SECRET="min-32-chars-secret-change-me-1234567890"
$env:RAZORPAY_KEY_ID="rzp_test_xxxxxxxx"
$env:RAZORPAY_KEY_SECRET="your_test_secret"
$env:RAZORPAY_WEBHOOK_SECRET=""  # optional, from webhook setup
```

Each service auto-creates its DB (`createDatabaseIfNotExist=true`):
auth_db, product_db, cart_db, order_db, payment_db, user_db.

## Run order
1. `cd eureka-server; mvn spring-boot:run`
2. `cd api-gateway; mvn spring-boot:run`
3. `cd auth-service; mvn spring-boot:run`
4. `cd payment-service; mvn spring-boot:run`
5. Others as needed.

Verify: http://localhost:8761 , http://localhost:8080/api/auth/health

## Razorpay TEST flow (no real money)
1. POST /api/auth/register -> token
2. POST /api/orders (TODO Phase 10) -> orderId, total
3. POST /api/payments/create-order {orderId, amount} -> {razorpayOrderId, keyId}
   - Frontend uses keyId + razorpayOrderId with Razorpay Checkout.js
4. POST /api/payments/verify {razorpayOrderId, razorpayPaymentId, razorpaySignature}
   - Backend HMAC_SHA256(order_id|payment_id, key_secret) check
5. Webhook POST /api/payments/webhook handles payment.captured/failed for reconciliation.

`key.secret` NEVER goes to frontend — only `key.id` + `razorpay_order_id`.

## Next
- Phase 5-7: Product entity/CRUD/search
- Phase 8-10: Cart/Order with Feign to Product + stock check
- Tell me your custom MySQL user/password to bake into local run scripts.
