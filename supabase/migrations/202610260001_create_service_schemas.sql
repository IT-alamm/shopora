-- Run once in the Supabase SQL Editor as the project database owner.
-- Each Spring Boot service writes only to its own PostgreSQL schema.
CREATE SCHEMA IF NOT EXISTS auth_service AUTHORIZATION postgres;
CREATE SCHEMA IF NOT EXISTS product_service AUTHORIZATION postgres;
CREATE SCHEMA IF NOT EXISTS cart_service AUTHORIZATION postgres;
CREATE SCHEMA IF NOT EXISTS order_service AUTHORIZATION postgres;
CREATE SCHEMA IF NOT EXISTS payment_service AUTHORIZATION postgres;
CREATE SCHEMA IF NOT EXISTS user_service AUTHORIZATION postgres;

GRANT USAGE, CREATE ON SCHEMA auth_service TO postgres;
GRANT USAGE, CREATE ON SCHEMA product_service TO postgres;
GRANT USAGE, CREATE ON SCHEMA cart_service TO postgres;
GRANT USAGE, CREATE ON SCHEMA order_service TO postgres;
GRANT USAGE, CREATE ON SCHEMA payment_service TO postgres;
GRANT USAGE, CREATE ON SCHEMA user_service TO postgres;
