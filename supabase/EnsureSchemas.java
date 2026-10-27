import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class EnsureSchemas {
    private static final String[] SCHEMAS = {
        "auth_service", "product_service", "cart_service",
        "order_service", "payment_service", "user_service"
    };

    public static void main(String[] args) throws Exception {
        String host = required("SUPABASE_DB_HOST");
        String port = valueOr("SUPABASE_DB_PORT", "5432");
        String database = valueOr("SUPABASE_DB_NAME", "postgres");
        String user = required("SUPABASE_DB_USER");
        String password = required("SUPABASE_DB_PASSWORD");
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            for (String schema : SCHEMAS) {
                statement.execute("CREATE SCHEMA IF NOT EXISTS " + schema + " AUTHORIZATION CURRENT_USER");
                statement.execute("GRANT USAGE, CREATE ON SCHEMA " + schema + " TO CURRENT_USER");
            }
            System.out.println("Supabase login succeeded; all six application schemas exist and are writable.");
        }
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }

    private static String valueOr(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
