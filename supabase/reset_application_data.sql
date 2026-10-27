-- Shopora application-data reset
-- DESTRUCTIVE: run manually in Supabase SQL Editor only when you intend to erase
-- every row from application tables in public and the six service schemas.
-- Table definitions and schema definitions are preserved; identity sequences reset.
-- Supabase-managed schemas (auth, storage, extensions, realtime, vault, etc.) are excluded.
-- This script does not run automatically and has NOT been executed by the local project.

DO $$
DECLARE
    target_schemas text[] := ARRAY[
        'public',
        'auth_service',
        'product_service',
        'cart_service',
        'order_service',
        'payment_service',
        'user_service'
    ];
    truncate_list text;
BEGIN
    SELECT string_agg(format('%I.%I', schemaname, tablename), ', ' ORDER BY schemaname, tablename)
      INTO truncate_list
      FROM pg_tables
     WHERE schemaname = ANY(target_schemas);

    IF truncate_list IS NULL THEN
        RAISE NOTICE 'No application tables found in the selected schemas.';
    ELSE
        RAISE NOTICE 'Truncating application tables: %', truncate_list;
        EXECUTE 'TRUNCATE TABLE ' || truncate_list || ' RESTART IDENTITY CASCADE';
    END IF;
END $$;
