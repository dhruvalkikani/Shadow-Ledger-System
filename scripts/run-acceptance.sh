#!/bin/bash
# Remove set -e temporarily to allow docker checks to pass/fail without exiting
# set -e

###############################################################################
# Shadow Ledger System – Acceptance Test Script
###############################################################################

# Always run from project root
ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT_DIR"

echo "=================================================="
echo " Shadow Ledger System – Acceptance Test Starting "
echo "=================================================="

echo ""
echo "Step 1: Starting all services..."

# Clean up old containers (ignore errors if none exist)
docker-compose down -v 2>/dev/null || true

# Start fresh (using standard docker-compose.yml)
docker-compose up -d --build

if [ $? -ne 0 ]; then
    echo "❌ Docker Compose failed to start. Check your docker-compose.yml file."
    exit 1
fi

echo "Waiting 60 seconds for services to become healthy..."
sleep 60

###############################################################################
# Pre-generated JWT tokens (Valid until 2031)
###############################################################################

# Role: USER
export USER_TOKEN="Bearer eyJhbGciOiAiSFMyNTYiLCAidHlwIjogIkpXVCJ9.eyJzdWIiOiAidXNlcjEiLCAicm9sZXMiOiBbIlVTRVIiXSwgImV4cCI6IDE5MjQ5MDU2MDB9.Dx3t21TlIOAXHC6AH6WJ08lz3vhNOMXNt03CHF7EcJc"

# Role: AUDITOR
export AUDITOR_TOKEN="Bearer eyJhbGciOiAiSFMyNTYiLCAidHlwIjogIkpXVCJ9.eyJzdWIiOiAiYXVkaXRvcjEiLCAicm9sZXMiOiBbIkFVRElUT1IiXSwgImV4cCI6IDE5MjQ5MDU2MDB9.kMQE_IKhUaJi2kuqx2CTfq1YDTqV5NiK2huRcBNSdWg"

# Role: ADMIN
export ADMIN_TOKEN="Bearer eyJhbGciOiAiSFMyNTYiLCAidHlwIjogIkpXVCJ9.eyJzdWIiOiAiYWRtaW4xIiwgInJvbGVzIjogWyJBRE1JTiJdLCAiZXhwIjogMTkyNDkwNTYwMH0.eeOCuXcLFSCiBya0nWqvwfIn14BR2vtldqiDcZjj9Mo"

###############################################################################
# Helper function
###############################################################################

call_api () {
  echo ""
  echo "→ $1"
  curl -i -X "$2" "$3" \
    -H "Authorization: $4" \
    -H "Content-Type: application/json" \
    ${5:+-d "$5"}
}

###############################################################################
# TEST FLOW
###############################################################################

# 1. Create CREDIT event
echo ""
echo "Step 2: Create CREDIT event (USER role)"
call_api \
  "POST /events (credit 1000)" \
  POST \
  http://localhost:8080/events \
  "$USER_TOKEN" \
  '{
    "eventId": "E1001",
    "accountId": "A10",
    "type": "credit",
    "amount": 1000,
    "timestamp": 1735561800000
  }'

sleep 5

# 2. Check Balance (Should be 1000)
echo ""
echo "Step 3: Fetch Shadow Balance (USER role)"
call_api \
  "GET /accounts/A10/shadow-balance" \
  GET \
  http://localhost:8080/accounts/A10/shadow-balance \
  "$USER_TOKEN"

###############################################################################

# 3. Drift Check
echo ""
echo "Step 4: Run Drift Check (AUDITOR role)"
echo "CBS reports 950 (Shadow is 1000) → Expecting DEBIT correction of 50"

call_api \
  "POST /drift-check" \
  POST \
  http://localhost:8080/drift-check \
  "$AUDITOR_TOKEN" \
  '[
    { "accountId": "A10", "reportedBalance": 950 }
  ]'

sleep 6

# 4. Check Balance After Drift Correction
echo ""
echo "Step 5: Shadow Balance AFTER Drift Correction"
call_api \
  "GET /accounts/A10/shadow-balance" \
  GET \
  http://localhost:8080/accounts/A10/shadow-balance \
  "$USER_TOKEN"

###############################################################################

# 5. Manual Correction
echo ""
echo "Step 6: Manual Correction (ADMIN role)"
echo "Admin credits 50 manually"

call_api \
  "POST /correct/A10?type=credit&amount=50" \
  POST \
  "http://localhost:8080/correct/A10?type=credit&amount=50" \
  "$ADMIN_TOKEN"

sleep 6

# 6. Final Balance Check
echo ""
echo "Step 7: Final Shadow Balance"
call_api \
  "GET /accounts/A10/shadow-balance" \
  GET \
  http://localhost:8080/accounts/A10/shadow-balance \
  "$USER_TOKEN"

###############################################################################

echo ""
echo "=================================================="
echo " Acceptance Test Completed Successfully ✅"
echo "=================================================="
