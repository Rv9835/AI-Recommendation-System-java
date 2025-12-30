HikariCP configuration and tuning

This project uses HikariCP as the JDBC connection pool via `DataSourceManager`.

Environment variables
- `DB_URL` — JDBC URL (default: `jdbc:mysql://localhost:3306/recommendation?serverTimezone=UTC&useSSL=false`)
- `DB_USER` — DB user (default: `root`)
- `DB_PASSWORD` — DB password (default: `root`)

Basic tuning in `DataSourceManager`:
- `maximumPoolSize` = 10
- `minimumIdle` = 1
- `cachePrepStmts` = true
- `prepStmtCacheSize` = 250
- `prepStmtCacheSqlLimit` = 2048

Production suggestions
- Increase `maximumPoolSize` to match expected concurrent DB load and available DB connections.
- Configure connection timeouts, leakDetectionThreshold, and validationTimeout as needed.
- Use environment-specific overrides (e.g., via container secrets or external config).

See `src/main/java/db/DataSourceManager.java` for the implementation used by the app.
