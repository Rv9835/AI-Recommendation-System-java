Title: feat(tests/security): add CSRF/session hardening, tests and docs

Description:

- Adds comprehensive test hardening and logging, `SessionConfigListener` for cookie hardening.
- Uses GitHub Actions secrets for DB creds in integration job (update repository secrets: `DB_URL`, `DB_USER`, `DB_PASSWORD`).
- Adds documentation: `docs/github-secrets.md`, `docs/maven-docker.md`, `docs/slides.md`, and a minimal Postman collection `docs/postman_collection.json`.
- Fixes and improvements to `MySQLConnection` and DI for testability; added README with architecture and run instructions.

How to test locally:

1. Ensure Maven is installed, or use Docker (see `docs/maven-docker.md`).
2. For local integration testing using Testcontainers, ensure Docker is available.
3. Run `mvn -B verify` to run static analysis and tests.

If push fails from this environment, push branch and open PR using:

```bash
git push -u origin add-tests-csrf-throttling
# Then create PR via GitHub UI or via CLI:
gh pr create --base main --head add-tests-csrf-throttling --title "feat(tests/security): add CSRF/session hardening, tests and docs" --body-file PR_BODY.md
```
