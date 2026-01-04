## Configure GitHub Repository Secrets

To allow CI integration tests to run without embedding credentials in workflows, add these repository secrets:

- `DB_URL` — example: `jdbc:mysql://127.0.0.1:3306/recommendation?serverTimezone=UTC&useSSL=false` or your JDBC URL
- `DB_USER` — example: `root`
-- `DB_PASSWORD` — example: set a secure password for CI (do NOT commit it to the repo)

Steps:

1. Go to your GitHub repository page.
2. Click `Settings` → `Secrets and variables` → `Actions` → `New repository secret`.
3. Add each secret name and value, then save.
4. Confirm `.github/workflows/ci.yml` references these secrets (this repo uses `${{ secrets.DB_URL }}`, `${{ secrets.DB_USER }}`, `${{ secrets.DB_PASSWORD }}`).

Note: For security, do not commit credentials to the repo. Use least-privilege DB accounts for CI where possible.
