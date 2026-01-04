# Slides / Presentation Notes

Include these visuals in your submission slides:

1) MVC Architecture (mermaid)

```mermaid
flowchart LR
  A[Client (Browser / SPA)] -->|HTTP| B[Servlets (Controllers)]
  B --> C[Service Layer]
  C --> D[DAO Layer (`DBConnection`) / Repos]
  D --> E[(Database: MySQL / H2)]
  B --> F[SecurityFilter (CSRF, CSP)]
  C --> G[Recommendation Engine]
```

2) Sequence: Signup → Login → Add Favorite

```mermaid
sequenceDiagram
  participant Client
  participant Servlet
  participant AuthService
  participant DB

  Client->>Servlet: POST /signup {user_id, password}
  Servlet->>AuthService: registerUser(...)
  AuthService->>DB: insert users
  DB-->>AuthService: OK
  AuthService-->>Servlet: success
  Client->>Servlet: POST /login {user_id, password}
  Servlet->>AuthService: verifyLogin(...)
  AuthService->>DB: select password
  DB-->>AuthService: hash
  AuthService-->>Servlet: success
  Servlet-->>Client: sets session + XSRF cookie
  Client->>Servlet: POST /history (X-XSRF-TOKEN header)
  Servlet->>DB: insert history
  DB-->>Servlet: OK
  Servlet-->>Client: success
```

Notes:
- Add screenshots or bullet points explaining throttling, CSRF, and ErrorHandler with trace-id.
