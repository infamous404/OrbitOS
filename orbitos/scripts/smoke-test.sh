#!/usr/bin/env bash
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
#
# Starts OrbitOS from the image orbitos/orbitos:dev with a throwaway database and checks
# that it serves the branded UI and API. Cleans up afterwards.
# Usage (repository root): bash orbitos/scripts/smoke-test.sh
set -euo pipefail

COMPOSE="docker compose -f orbitos/docker/compose.yml -p orbitos-smoke"
ENV_FILE=orbitos/docker/.env
BASE=http://127.0.0.1:8080
CREATED_ENV=false

cleanup() {
  $COMPOSE logs orbitos > smoke-test-orbitos.log 2>&1 || true
  # KEEP_RUNNING=1 leaves the stack up for manual checks (stop it with: docker compose -p orbitos-smoke down -v)
  if [ "${KEEP_RUNNING:-}" = "1" ]; then return; fi
  $COMPOSE down -v > /dev/null 2>&1 || true
  if [ "$CREATED_ENV" = true ]; then rm -f "$ENV_FILE"; fi
}
trap cleanup EXIT

PY=python3
"$PY" -c 'pass' 2>/dev/null || PY=python
"$PY" -c 'pass' 2>/dev/null || { echo "Python 3 is required"; exit 1; }

if [ ! -f "$ENV_FILE" ]; then
  echo "ORBITOS_DB_PASSWORD=$(head -c 32 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 40)" > "$ENV_FILE"
  CREATED_ENV=true
fi

fail() { echo "FAIL: $*"; exit 1; }
pass() { echo "ok:   $*"; }

echo "Installing database schema ..."
$COMPOSE --profile install run --rm orbitos-install > smoke-test-install.log 2>&1 || { tail -50 smoke-test-install.log; fail "install"; }
pass "install"

echo "Starting ..."
$COMPOSE up -d
for i in $(seq 1 90); do
  if curl -fsS "$BASE/api/noauth/branding" > smoke-test-branding.json 2>/dev/null; then break; fi
  restarts=$(docker inspect -f '{{.RestartCount}}' "$($COMPOSE ps -q orbitos)" 2>/dev/null || echo 0)
  [ "${restarts:-0}" -le 1 ] || fail "server keeps restarting; see smoke-test-orbitos.log"
  sleep 5
done
[ -s smoke-test-branding.json ] || fail "server did not answer within 7.5 minutes"
pass "server answers"

"$PY" - <<'EOF' || fail "branding JSON"
import json
b = json.load(open('smoke-test-branding.json'))
assert b['name'] == 'OrbitOS', b
assert b['logoUrl'].startswith('/api/noauth/branding/logo?v='), b
assert b['login']['title']['fr'].startswith('Bienvenue'), b
EOF
pass "branding JSON (name, logo, French login title)"

headers=$(curl -fsS -D - -o /dev/null "$BASE/api/noauth/branding/logo")
echo "$headers" | grep -qi '^content-type: image/svg+xml' || fail "logo content type"
echo "$headers" | grep -qi '^content-security-policy:.*sandbox' || fail "logo CSP header"
pass "logo served with sandbox CSP"

curl -fsS "$BASE/" | grep -q '<title>OrbitOS</title>' || fail "index.html title"
pass "UI title"

if curl -fsS "$BASE/assets/locale/locale.constant-en_US.json" | grep -q 'ThingsBoard'; then
  fail "upstream product name in English UI strings"
fi
pass "no upstream product name in English UI strings"

token=$(curl -fsS -H 'Content-Type: application/json' \
  -d '{"username":"sysadmin@thingsboard.org","password":"sysadmin"}' "$BASE/api/auth/login" \
  | "$PY" -c 'import json,sys; print(json.load(sys.stdin)["token"])') || fail "login"
pass "login"

curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/admin/updates" \
  | "$PY" -c 'import json,sys; d=json.load(sys.stdin); assert d["updateAvailable"] is False, d' || fail "update check"
pass "update check is offline"

params=$(curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/system/params")
echo "$params" | "$PY" -c 'import json,sys; d=json.load(sys.stdin); assert not d.get("iotHubBaseUrl"), d' || fail "IoT Hub URL exposed"
pass "IoT Hub disabled"

code=$(curl -s -o /dev/null -w '%{http_code}' -H "X-Authorization: Bearer $token" "$BASE/api/communityGrant/state")
[ "$code" = "404" ] || [ "$code" = "405" ] || fail "Community Grant endpoint still answers ($code)"
pass "Community Grant endpoint removed ($code)"

if $COMPOSE logs orbitos 2>&1 | grep -E 'updates\.thingsboard\.io|license\.thingsboard\.io|iot-hub\.thingsboard\.io'; then
  fail "log mentions a vendor host"
fi
pass "no vendor hosts in server log"

echo "All smoke tests passed."
