# Shopora Monolithic Backend

This is a single Spring Boot application consolidating Auth, Product, Cart, Order, Payment, and User modules. It runs on port 8080; Eureka, API Gateway, and Feign network calls are not needed.

## Run on Windows
From the project root:
```powershell
. .\local-env.ps1
cd .\monolith-backend
mvn spring-boot:run
```
If local-env prompts for the Supabase password, enter it in PowerShell. Set Razorpay test credentials as environment variables before testing payments.

The monolith retains the existing Supabase schemas (auth_service, product_service, cart_service, order_service, payment_service, user_service) and maps each entity to its schema. This avoids a risky database migration while consolidating runtime into one app.

## Important
This is a migration workspace alongside the original services; the original microservice folders are kept unchanged for rollback. Validate database table/column names and run end-to-end tests before using in production.
