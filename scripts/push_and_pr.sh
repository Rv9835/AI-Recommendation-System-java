#!/usr/bin/env bash
set -euo pipefail

# Push branch and create PR helper
# Usage: ensure you're authenticated with GitHub CLI (`gh auth login`) and run:
#   ./scripts/push_and_pr.sh

REPO_OWNER=Rv9835
REPO_NAME=AI-Recommendation-System-java
BRANCH=$(git rev-parse --abbrev-ref HEAD)
REMOTE_SSH=git@github.com:${REPO_OWNER}/${REPO_NAME}.git

echo "Current branch: ${BRANCH}"

if ! command -v gh >/dev/null 2>&1; then
  echo "gh CLI not found. Install GitHub CLI: https://cli.github.com/" >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "You are not authenticated with gh. Run: gh auth login" >&2
  exit 1
fi

# Create repo if it doesn't exist
if ! gh repo view ${REPO_OWNER}/${REPO_NAME} >/dev/null 2>&1; then
  echo "Repository ${REPO_OWNER}/${REPO_NAME} not found. Creating..."
  gh repo create ${REPO_OWNER}/${REPO_NAME} --public --confirm
fi

# Ensure remote is set to SSH
echo "Setting remote origin to ${REMOTE_SSH}"
git remote set-url origin ${REMOTE_SSH}

echo "Pushing branch ${BRANCH} to origin..."
git push -u origin ${BRANCH}

PR_TITLE="feat(tests/security): add CSRF/session hardening, tests and docs"
PR_BODY_FILE=PR_BODY.md
if [ ! -f "${PR_BODY_FILE}" ]; then
  echo "PR body file ${PR_BODY_FILE} not found; creating a simple body."
  cat > ${PR_BODY_FILE} <<EOF
See changes in branch ${BRANCH} for CSRF, session hardening, tests and docs.
EOF
fi

echo "Creating Pull Request..."
gh pr create --base main --head ${BRANCH} --title "${PR_TITLE}" --body-file ${PR_BODY_FILE}

echo "Done. PR created (or updated)."
