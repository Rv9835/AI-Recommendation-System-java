## Running Maven via Docker (if `mvn` is not installed locally)

If Maven is not available on your machine, use the official Maven Docker image to run builds and tests.

Example: run full verify (equivalent to `mvn -B verify`):

```bash
docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.4-openjdk-11 mvn -B verify
```

Run unit tests only (skip integration tests):

```bash
docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.4-openjdk-11 mvn -B -DskipITs=true test
```

Run Flyway migrations via Docker (ensure `DB_*` env vars are set):

```bash
docker run --rm -v "$PWD":/workspace -w /workspace -e DB_URL -e DB_USER -e DB_PASSWORD maven:3.9.4-openjdk-11 mvn -DskipTests=true flyway:migrate
```

Tip: In CI you still should provide DB credentials via repository secrets.
