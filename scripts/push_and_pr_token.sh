#!/usr/bin/env bash
set -euo pipefail

# Usage: export GITHUB_TOKEN="..."; ./scripts/push_and_pr_token.sh [owner] [repo]
# Defaults: owner=sudarshan81026 repo=AI-Recommendation-System-java

OWNER=${1:-sudarshan81026}
REPO=${2:-AI-Recommendation-System-java}
BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [ -z "${GITHUB_TOKEN:-}" ]; then
  echo "GITHUB_TOKEN environment variable is required. Export it and retry." >&2
  exit 1
fi

API_URL="https://api.github.com"

echo "Creating repository ${OWNER}/${REPO} (if it doesn't exist)..."
create_resp=$(curl -s -o /dev/stderr -w "%{http_code}" -H "Authorization: token ${GITHUB_TOKEN}" \
  -d "{\"name\": \"${REPO}\", \"private\": false}" ${API_URL}/user/repos) || true

# If create_resp indicates success (201) or already exists (422) continue
if [ "$create_resp" != "201" ] && [ "$create_resp" != "422" ]; then
  echo "Warning: unexpected response code when creating repo: ${create_resp}" >&2
fi

REMOTE_URL="https://${GITHUB_TOKEN}@github.com/${OWNER}/${REPO}.git"
echo "Setting remote origin to HTTPS URL (token will be used for push)..."
git remote set-url origin "${REMOTE_URL}"

echo "Pushing branch ${BRANCH} to remote..."
git push -u origin "${BRANCH}"

# Prepare PR body
PR_BODY_FILE=PR_BODY.md
if [ -f "${PR_BODY_FILE}" ]; then
  PR_BODY=$(cat "${PR_BODY_FILE}")
else
  PR_BODY="Automated PR: push branch ${BRANCH}"
fi

echo "Creating Pull Request via GitHub API..."
create_pr_resp=$(curl -s -X POST -H "Authorization: token ${GITHUB_TOKEN}" -H "Content-Type: application/json" \
  -d "{\"title\": \"feat(tests/security): add CSRF/session hardening, tests and docs\", \"head\": \"${BRANCH}\", \"base\": \"main\", \"body\": \"${PR_BODY//"/\"}\"}" \
  ${API_URL}/repos/${OWNER}/${REPO}/pulls)

echo "PR creation response:"
echo "${create_pr_resp}"

echo "Done. If the PR was created, you'll see it above."
