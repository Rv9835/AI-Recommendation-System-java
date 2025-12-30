package db;

import db.mysql.MySQLConnection;
import javax.sql.DataSource;

/**
 * Factory for obtaining `DBConnection` instances. Production code uses the default
 * provider (MySQL). Tests may override the provider via `setProviderForTests` to
 * supply mock or in-memory implementations.
 */
public class DBConnectionFactory {
    private static final String DEFAULT_DB = "mysql";

    // Provider allows tests to inject a fake DBConnection supplier
    private static java.util.function.Supplier<DBConnection> provider = null;

    public static void setProviderForTests(java.util.function.Supplier<DBConnection> p) {
        provider = p;
    }

    /**
     * Convenience helper for tests: set a {@link DataSource} to be used when
     * creating {@link DBConnection} instances. This will configure the provider
     * to return a {@link db.mysql.MySQLConnection} backed by the supplied
     * DataSource.
     */
    public static void setDataSourceForTests(DataSource ds) {
        provider = () -> new MySQLConnection(ds);
    }

    public static void resetProvider() {
        provider = null;
    }

    public static DBConnection getConnection(String db) {
        if (provider != null) return provider.get();
        switch (db) {
            case "mysql":
                return new MySQLConnection();
            default:
                throw new IllegalArgumentException("Invalid db: " + db);
        }
    }

    public static DBConnection getConnection() {
        return getConnection(DEFAULT_DB);
    }
}
