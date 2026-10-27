# Shopora — Monolithic Backend

Shopora uses one Spring Boot monolithic backend application. All backend modules run in a single process and share the configured Supabase PostgreSQL database.

## Project structure
- `monolith-backend/` — the only backend application
- `storefront/` — React frontend
- `supabase/` — database migrations and SQL utilities
- `local-env.ps1` — loads local environment variables and securely prompts for the database password
- `run-monolith.ps1` — starts the monolith

## Requirements
- Java 17
- Maven 3.9+
- Supabase PostgreSQL credentials

## Run backend
From the project root in PowerShell:

```powershell
.\run-monolith.ps1
```

The application runs on port `8080` by default. API documentation, when enabled, is available at `/swagger-ui.html` or `/swagger-ui/index.html`.

Set database connection values, `JWT_SECRET`, and Razorpay test credentials through the local environment setup. Do not commit or share passwords/secrets.

## Seed data
Seed/fixture/demo-data scripts have been removed. The application does not automatically seed product data. Existing rows already stored in Supabase are separate from local seed scripts and must be removed with a database cleanup query if required.
