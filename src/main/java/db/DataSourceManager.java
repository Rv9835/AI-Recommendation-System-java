package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class DataSourceManager {
    private static volatile HikariDataSource ds;

    private DataSourceManager() {}

    /**
     * Obtain the shared HikariCP {@link javax.sql.DataSource} for the application.
     * The data source is lazily initialized and reused. Call {@link #close()} on
     * application shutdown to release the pool.
     */
    public static DataSource getDataSource() {
        if (ds == null) {
            synchronized (DataSourceManager.class) {
                if (ds == null) {
                    HikariConfig cfg = new HikariConfig();
                    String url = System.getProperty("DB_URL", System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/?user=root"));
                    String user = System.getProperty("DB_USER", System.getenv().getOrDefault("DB_USER", "root"));
                    String pass = System.getProperty("DB_PASSWORD", System.getenv().getOrDefault("DB_PASSWORD", "R@nvijay"));
                    cfg.setJdbcUrl(url);
                    cfg.setUsername(user);
                    cfg.setPassword(pass);
                    cfg.setMaximumPoolSize(10);
                    cfg.setMinimumIdle(1);
                    cfg.setPoolName("ai-rec-pool");
                    cfg.addDataSourceProperty("cachePrepStmts", "true");
                    cfg.addDataSourceProperty("prepStmtCacheSize", "250");
                    cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                    ds = new HikariDataSource(cfg);
                }
            }
        }
        return ds;
    }

    public static void close() {
        if (ds != null) {
            ds.close();
        }
    }
}
