# AI Recommendation System (Java Servlet)

This project is a Java Servlet backend for a personalized search & recommendation system. The repository has been restructured to a standard Maven webapp layout with a DAO layer (MySQL), a service layer, servlets, and utility helpers.

Quick status
- Packaging: WAR via Maven
- Servlet API: javax.servlet-api 4.0.1
- JSON: org.json
- MySQL connector included

Environment / configuration
- Set these environment variables (or adjust values in src):
  - DB_URL (default: jdbc:mysql://localhost:3306/recommendation?serverTimezone=UTC&useSSL=false)
  - DB_USER (default: root)
  - DB_PASSWORD (default: root)

Database schema (minimal)
Run these statements to create required tables (example):

```
CREATE TABLE users (
  user_id VARCHAR(100) PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100)
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

```bash
mvn -DskipTests=false test
```

Connection lifecycle note
- The project uses HikariCP via `DataSourceManager`. `MySQLConnection` currently obtains a pooled `Connection` when constructed and closes it in `close()`.
- Best practice: prefer obtaining a `Connection` per operation (try-with-resources) to avoid long-lived connections in application-scoped objects. For Review 1/2 you can either refactor DAO methods to get a Connection per method or document the lifecycle (above). Future PR: refactor `MySQLConnection` to acquire short-lived connections for each DB operation.

Files of interest
- src/main/java/db/mysql/MySQLConnection.java
- src/main/java/db/DBConnection.java
- src/main/java/rpc/*.java (servlets)
- src/main/java/service/*.java
- src/main/java/util/RpcHelper.java

If you want, I can also add sample index.html and minimal JS to exercise the endpoints.

CSRF protection (front-end guidance)
- The server exposes a `XSRF-TOKEN` cookie for JavaScript clients on non-state requests and after login. Include this token in the `X-XSRF-TOKEN` header for all state-changing requests (POST/PUT/DELETE).

Example (JS):

```
function readCookie(name) {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

const xsrf = readCookie('XSRF-TOKEN');
fetch('/history', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-XSRF-TOKEN': xsrf
  },
  credentials: 'same-origin',
  body: JSON.stringify({ favorite: ['item1'] })
});
```

Notes:
- The filter bypasses CSRF checks for `/login` and `/register` to allow unauthenticated sign-up/login flows.
- Ensure your front-end reads the `XSRF-TOKEN` cookie and sends it in the `X-XSRF-TOKEN` header for state changes.
