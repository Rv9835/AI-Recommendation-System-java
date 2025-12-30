#!/usr/bin/env bash
# Simple integration test script that uses curl and a cookie jar to test endpoints.
set -e
COOKIEJAR=$(mktemp)
BASE_URL="http://localhost:8080/ai-recommendation"

echo "Registering test user..."
curl -s -X POST -H "Content-Type: application/json" -d '{"user_id":"testuser","password":"pass123","first_name":"Test","last_name":"User"}' -c "$COOKIEJAR" "$BASE_URL/register" | jq '.'

echo "Logging in..."
curl -s -X POST -H "Content-Type: application/json" -d '{"user_id":"testuser","password":"pass123"}' -b "$COOKIEJAR" -c "$COOKIEJAR" "$BASE_URL/login" | jq '.'

echo "Fetching history (should be empty)..."
curl -s -X GET -b "$COOKIEJAR" "$BASE_URL/history" | jq '.'

echo "Logging out..."
curl -s -X POST -b "$COOKIEJAR" -c "$COOKIEJAR" "$BASE_URL/logout" | jq '.'

rm -f "$COOKIEJAR"

echo "Integration script finished."
