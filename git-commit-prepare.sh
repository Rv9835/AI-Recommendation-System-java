#!/usr/bin/env bash
# Creates a branch and commits current changes with a prepared message.
BRANCH=improve/checkstyle-javadoc-tests
COMMIT_MSG_FILE=commit-message.txt

set -e

echo "Creating branch $BRANCH"

git checkout -b "$BRANCH"

git add .

git commit -F "$COMMIT_MSG_FILE"

echo "Branch $BRANCH created and changes committed. Push with: git push -u origin $BRANCH" 
