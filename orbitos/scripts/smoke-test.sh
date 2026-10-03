#!/usr/bin/env bash
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
#
# End-to-end test of a fresh install, using the already built image localhost/orbitos:dev:
# docker compose up, first-run setup, French default data, a device sending a value, the dashboard,
# offline rules, backup and restore. Uses other ports and a separate project; cleans up afterwards.
# Usage (repository root): bash orbitos/scripts/smoke-test.sh      (KEEP_RUNNING=1 to leave it up)
set -euo pipefail

export ORBITOS_HTTP_PORT=18080 ORBITOS_MQTT_PORT=11883 ORBITOS_COAP_PORT=15683 ORBITOS_BIND=127.0.0.1
COMPOSE="docker compose -p orbitos-smoke"
BASE=http://127.0.0.1:$ORBITOS_HTTP_PORT
PASSWORD="smoke-$(head -c 24 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 20)"
BACKUP=""

cleanup() {
  $COMPOSE logs orbitos > smoke-test-orbitos.log 2>&1 || true
  if [ -n "$BACKUP" ]; then rm -f "backups/$BACKUP"; fi
  if [ "${KEEP_RUNNING:-}" = "1" ]; then echo "Left running: $BASE (stop: $COMPOSE down -v)"; return; fi
  $COMPOSE down -v > /dev/null 2>&1 || true
}
trap cleanup EXIT

PY=python3
"$PY" -c 'pass' 2>/dev/null || PY=python
"$PY" -c 'pass' 2>/dev/null || { echo "Python 3 is required"; exit 1; }
json() { "$PY" -c "import json,sys; d=json.loads(sys.stdin.buffer.read().decode('utf-8')); $1"; }

fail() { echo "FAIL: $*"; exit 1; }
pass() { echo "ok:   $*"; }

wait_healthy() {
  for _ in $(seq 1 90); do
    if curl -fsS "$BASE/api/noauth/orbitos/health" 2>/dev/null | grep -q UP; then return 0; fi
    restarts=$(docker inspect -f '{{.RestartCount}}' "$($COMPOSE ps -q orbitos)" 2>/dev/null || echo 0)
    [ "${restarts:-0}" -le 1 ] || fail "server keeps restarting; see smoke-test-orbitos.log"
    sleep 5
  done
  fail "server not healthy within 7.5 minutes"
}

post() { curl -s -o smoke-test-body.json -w '%{http_code}' -H 'Content-Type: application/json' -d "$2" "$BASE$1"; }
login() {
  curl -fsS -H 'Content-Type: application/json' -d "{\"username\":\"$1\",\"password\":\"$2\"}" "$BASE/api/auth/login" \
    | json 'print(d["token"])'
}

echo "Starting a fresh install (docker compose up) ..."
$COMPOSE up -d --no-build > smoke-test-up.log 2>&1 || { cat smoke-test-up.log; fail "compose up"; }
wait_healthy
pass "one-command start: database created, server healthy"

[ -z "$(docker port "$($COMPOSE ps -q postgres)")" ] || fail "database port is published"
pass "database not exposed"

# ---- first-run setup ----
curl -fsS "$BASE/api/noauth/orbitos/setup" | json 'assert d["required"] is True, d' || fail "setup should be required"
code=$(curl -s -o /dev/null -w '%{http_code}' -H 'Content-Type: application/json' \
  -d '{"username":"sysadmin@thingsboard.org","password":"sysadmin"}' "$BASE/api/auth/login")
[ "$code" = "401" ] || fail "upstream default account still works ($code)"
pass "no default account or password"

setup_code=$($COMPOSE logs orbitos 2>&1 | grep -oE '[2-9A-Z]{4}-[2-9A-Z]{4}-[2-9A-Z]{4}' | tail -1)
[ -n "$setup_code" ] || fail "setup code not in the log"
pass "setup code printed in the log"

request() {
  printf '{"setupCode":"%s","language":"fr_CA","organizationName":"Essai","email":"admin@example.com","password":"%s","sysadminEmail":"","deviceName":"Capteur 1"}' "$1" "$2"
}
[ "$(post /api/noauth/orbitos/setup "$(request AAAA-BBBB-CCCC "$PASSWORD")")" = "403" ] || fail "wrong code accepted"
[ "$(post /api/noauth/orbitos/setup "$(request "$setup_code" short)")" = "400" ] || fail "short password accepted"
pass "wrong code and short password refused"

[ "$(post /api/noauth/orbitos/setup "$(request "$setup_code" "$PASSWORD")")" = "200" ] || { cat smoke-test-body.json; fail "setup"; }
device_token=$(json 'print(d["accessToken"])' < smoke-test-body.json)
dashboard_id=$(json 'print(d["dashboardId"])' < smoke-test-body.json)
device_id=$(json 'print(d["deviceId"])' < smoke-test-body.json)
pass "setup completed (organization, accounts, device, dashboard)"

curl -fsS "$BASE/api/noauth/orbitos/setup" | json 'assert d["required"] is False, d' || fail "setup still required"
[ "$(post /api/noauth/orbitos/setup "$(request "$setup_code" "$PASSWORD")")" = "409" ] || fail "setup can run twice"
pass "setup page closed after use"

token=$(login admin@example.com "$PASSWORD") || fail "organization admin login"
systoken=$(login sysadmin@local.invalid "$PASSWORD") || fail "platform admin login"
pass "both accounts sign in"

# ---- device, dashboard, French default data ----
curl -fsS -H 'Content-Type: application/json' -d '{"temperature":21.5,"humidity":40}' \
  "$BASE/api/v1/$device_token/telemetry" || fail "device telemetry"
sleep 2
curl -fsS -H "X-Authorization: Bearer $token" \
  "$BASE/api/plugins/telemetry/DEVICE/$device_id/values/timeseries?keys=temperature" \
  | json 'assert d["temperature"][0]["value"] in ("21.5", 21.5), d' || fail "value not stored"
pass "device value received and stored"

curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/dashboard/$dashboard_id" \
  | json 'assert d["title"] == "Mon premier tableau de bord", d["title"]; assert len(d["configuration"]["widgets"]) == 3' \
  || fail "dashboard"
pass "dashboard created in French"

curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/ruleChains?pageSize=20&page=0" \
  | json 'names=[r["name"] for r in d["data"]]; assert any(n.endswith("de r" + chr(232) + "gles racine") for n in names), names' || fail "root rule chain not in French"
curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/notification/templates?pageSize=50&page=0" \
  | json 'names=[t["name"] for t in d["data"]]; assert "Avis de nouvelle alarme" in names, names' || fail "notifications not in French"
pass "default rule chain and notifications in French"

curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/auth/user" \
  | json 'assert d["additionalInfo"]["lang"] == "fr_CA", d["additionalInfo"]' || fail "user language"
pass "account language saved (fr_CA)"

# ---- branding, offline, call-home ----
curl -fsS "$BASE/api/noauth/branding" | json 'assert d["name"] == "OrbitOS", d' || fail "branding"
curl -fsS "$BASE/" | grep -q '<title>OrbitOS</title>' || fail "index.html title"
headers=$(curl -fsS -D - -o /dev/null "$BASE/")
echo "$headers" | grep -qi "^content-security-policy:.*connect-src 'self';" || fail "offline CSP header"
pass "branding and offline browser policy"

if curl -fsS "$BASE/assets/locale/locale.constant-en_US.json" | grep -q 'ThingsBoard'; then
  fail "upstream product name in English UI strings"
fi
pass "no upstream product name in English UI strings"

curl -fsS -H "X-Authorization: Bearer $systoken" "$BASE/api/admin/updates" \
  | json 'assert d["updateAvailable"] is False, d' || fail "update check"
curl -fsS -H "X-Authorization: Bearer $systoken" "$BASE/api/system/params" \
  | json 'assert not d.get("iotHubBaseUrl"), d' || fail "IoT Hub URL exposed"
code=$(curl -s -o /dev/null -w '%{http_code}' -H "X-Authorization: Bearer $systoken" "$BASE/api/communityGrant/state")
[ "$code" = "404" ] || [ "$code" = "405" ] || fail "Community Grant endpoint still answers ($code)"
if $COMPOSE logs orbitos 2>&1 | grep -E 'updates\.thingsboard\.io|license\.thingsboard\.io|iot-hub\.thingsboard\.io'; then
  fail "log mentions a vendor host"
fi
pass "no call-home (updates, IoT Hub, Community Grant)"

# ---- backup and restore ----
$COMPOSE run --rm backup > smoke-test-backup.log 2>&1 || { cat smoke-test-backup.log; fail "backup"; }
BACKUP=$(grep -oE 'orbitos-[0-9]{8}-[0-9]{6}\.dump' smoke-test-backup.log | head -1)
[ -s "backups/$BACKUP" ] || fail "backup file missing"
pass "backup written ($BACKUP)"

curl -fsS -H 'Content-Type: application/json' -d '{"temperature":99}' "$BASE/api/v1/$device_token/telemetry" > /dev/null
$COMPOSE stop orbitos > /dev/null 2>&1
$COMPOSE run --rm restore "$BACKUP" > smoke-test-restore.log 2>&1 || { cat smoke-test-restore.log; fail "restore"; }
$COMPOSE start orbitos > /dev/null 2>&1
wait_healthy
token=$(login admin@example.com "$PASSWORD") || fail "login after restore"
curl -fsS -H "X-Authorization: Bearer $token" \
  "$BASE/api/plugins/telemetry/DEVICE/$device_id/values/timeseries?keys=temperature" \
  | json 'assert d["temperature"][0]["value"] in ("21.5", 21.5), d' || fail "restore did not bring back the backed-up value"
pass "restore brings back the data as it was at backup time"

rm -f smoke-test-body.json
echo "All smoke tests passed."
