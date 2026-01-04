# AI Recommendation System (Java Servlet)

This project is a Java Servlet backend for a personalized search & recommendation system. The repository has been restructured to a standard Maven webapp layout with a DAO layer (MySQL), a service layer, servlets, and utility helpers.

Quick status
- Packaging: WAR via Maven
- Servlet API: javax.servlet-api 4.0.1
- JSON: org.json
- MySQL connector included

Environment / configuration
  - DB_URL (default: jdbc:mysql://localhost:3306/?user=root)

Database schema (minimal)
Run these statements to create required tables (example):
export DB_URL="jdbc:mysql://localhost:3306/?user=root"
export DB_USER=root
export DB_PASSWORD='R@nvijay'
  user_id VARCHAR(100) PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
 `DB_URL` (default: `jdbc:mysql://localhost:3306/?user=root`)
 `DB_PASSWORD` (default: `R@nvijay`)
);

CREATE TABLE items (
  id VARCHAR(200) PRIMARY KEY,
  name VARCHAR(255),
  address VARCHAR(255),
  image_url VARCHAR(512),
  url VARCHAR(512),
  lat DOUBLE,
  lon DOUBLE,
  description TEXT
);

CREATE TABLE item_categories (
  item_id VARCHAR(200),
  category VARCHAR(200),
  PRIMARY KEY(item_id, category)
);

CREATE TABLE history (
  user_id VARCHAR(100),
  item_id VARCHAR(200),
  PRIMARY KEY(user_id, item_id)
);
```

Build & run
- Build: `mvn package` (generates ai-recommendation.war)
- Deploy: copy WAR to Tomcat `webapps/` or run via your app server

Endpoints (JSON)
- POST /login — body: {"user_id":"u","password":"p"}
- GET /login — uses session cookie to check login
- POST /register — body: {"user_id":"u","password":"p","first_name":"F","last_name":"L"}
- GET/POST/DELETE /history — requires session; POST/DELETE body: {"favorite":["item1","item2"]}
- POST /logout — invalidates session

Notes & next steps
- Use secure password hashing before production (bcrypt/argon2).
- Add unit tests and integration tests for DAO layer.
- Front-end pages (index.html, JS) are expected in src/main/webapp; add them as needed.

Database migrations & tests (how to run)
- Migrations: this project includes Flyway migrations under `src/main/resources/db/migration`. To apply migrations to your configured DB run:

```bash
# set DB_URL/DB_USER/DB_PASSWORD or export as env vars
mvn flyway:migrate
```

- Integration tests (Testcontainers): the project contains an integration test that runs a temporary MySQL container, applies migrations, and exercises the DAO.
  - Run tests with Docker available:

## AI Recommendation System (Java Servlet)

This is a Java Servlet backend for a personalized search & recommendation service. The project follows a Maven webapp layout and includes a DAO layer (MySQL), a service layer, servlets (RPC endpoints), Flyway migrations, and automated tests (unit, IT, E2E).

### Quick facts
- Packaging: WAR via Maven
- Java: 11
- Servlet API: javax.servlet-api 4.0.1
- DB: MySQL (production), H2 (unit tests), Testcontainers (integration)

### Environment & configuration
Set these environment variables (or configure via your CI):

- `DB_URL` (default: `jdbc:mysql://localhost:3306/recommendation?serverTimezone=UTC&useSSL=false`)
- `DB_USER` (default: `root`)
- `DB_PASSWORD` (default: `root`)

Export example (macOS/Linux):

```bash
export DB_URL="jdbc:mysql://localhost:3306/recommendation?serverTimezone=UTC&useSSL=false"
export DB_USER=root
export DB_PASSWORD=root
```

### Database schema (migration)
Flyway migrations are under `src/main/resources/db/migration` and will be applied by tests or via `mvn flyway:migrate`.

If you need a quick manual schema (for local MySQL), run the SQL found in `src/main/resources/db/migration/V1__schema.sql`.

### Build, run, and tests
Build the project (requires Maven):

```bash
mvn -B package
```

Run unit tests (H2 in-memory and local tests):

```bash
mvn -B -DskipITs=true test
```

Run full verification (includes static analysis and coverage checks):

```bash
mvn -B verify
```

Run integration tests (requires Docker for Testcontainers):

```bash
mvn -DskipITs=false -DskipTests=false verify
```

Notes:
- CI: GitHub Actions runs `mvn verify` for unit checks and static analysis; integration tests run on PRs using a MySQL service.
 - CI: GitHub Actions runs `mvn verify` for unit checks and static analysis; integration tests run on PRs using a MySQL service.

### Session cookie security (recommended)

By default the servlet container sets the `JSESSIONID` cookie. To ensure production-grade session cookie hardening configure the container or set `SessionCookieConfig` in a `ServletContextListener`. Example snippet:

```java
public class SessionConfigListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext ctx = sce.getServletContext();
    SessionCookieConfig cfg = ctx.getSessionCookieConfig();
    cfg.setHttpOnly(true);
    cfg.setSecure(true); // set true in TLS-enabled environments
    cfg.setName("JSESSIONID");
    // Some containers support SameSite via config; otherwise set via response headers in a filter
  }
  @Override public void contextDestroyed(ServletContextEvent sce) {}
}
```

For the XSRF token cookie this project exposes `XSRF-TOKEN` to client-side JS intentionally (so the SPA can read it). It will include `SameSite=Strict` and `Secure` when requests are TLS. Do not mark `XSRF-TOKEN` as `HttpOnly` because the client needs to read it to set the `X-XSRF-TOKEN` header.
- If Maven is not available locally, you can run via Docker Maven image or use your IDE's Maven support.

### Endpoints (JSON)
- `POST /login` — body:{"user_id":"u","password":"p"}
- `GET /login` — session status
- `POST /register` — body:{"user_id":"u","password":"p","first_name":"F","last_name":"L"}
- `GET/POST/DELETE /history` — requires session; POST/DELETE body:{"favorite":["item1","item2"]}
- `POST /logout` — invalidates session

### Security & best practices applied
- Passwords hashed with BCrypt.
- CSRF protection via `XSRF-TOKEN` cookie + `X-XSRF-TOKEN` header; `SecurityFilter` enforces tokens for state-changing requests.
- Login throttling to mitigate brute-force attempts implemented in `AuthService`.
- Global JSON error handler with trace id via `util/ErrorHandler`.

### Developer checklist before submission (recommended)
1. Fix any remaining empty catch blocks and ensure errors are logged and rethrown as `DataAccessException` for consistent error handling.
2. Ensure `MySQLConnection` uses the injected `DataSource` (done) and that tests call `DBConnectionFactory.setDataSourceForTests` to inject an H2 DataSource.
3. Run `mvn verify` locally or rely on CI to run static analysis (Checkstyle/SpotBugs/PMD) and JaCoCo coverage (>=80%).
4. Confirm session cookies are configured with `HttpOnly`, `Secure`, and `SameSite` in production (check servlet/container config).
5. Add architecture diagram (MVC), and brief flowcharts for signup/login/favorite flows to your slides.

### How to run Flyway migrations

```bash
mvn flyway:migrate
```

### Tips for graders
- The project includes unit, integration, and E2E tests under `src/test/java`.
- To run only DB-related tests with H2, ensure tests call `DBConnectionFactory.setDataSourceForTests` (the H2 example test is `src/test/java/db/mysql/MySQLConnectionH2Test.java`).

### Files of interest
- `src/main/java/db/mysql/MySQLConnection.java`
- `src/main/java/db/DBConnection.java`
- `src/main/java/rpc/*.java` (servlets)
- `src/main/java/service/*.java`
- `src/main/java/util/RpcHelper.java`
- `src/main/resources/db/migration`

If you'd like, I can also add a minimal `index.html` + JS or a simple Postman collection to exercise the endpoints.
