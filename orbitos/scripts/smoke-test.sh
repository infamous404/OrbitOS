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
export ORBITOS_MQTT_TLS=true ORBITOS_MQTTS_PORT=18883 ORBITOS_GATEWAY_CONFIG=./.smoke-gateway
COMPOSE="docker compose -p orbitos-smoke --profile gateway --profile gateway-demo"
BASE=http://127.0.0.1:$ORBITOS_HTTP_PORT
PASSWORD="smoke-$(head -c 24 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 20)"
BACKUP=""

cleanup() {
  $COMPOSE logs orbitos > smoke-test-orbitos.log 2>&1 || true
  $COMPOSE logs gateway > smoke-test-gateway.log 2>&1 || true
  if [ -n "$BACKUP" ]; then rm -f "backups/$BACKUP"; fi
  docker rm -f orbitos-smoke-opcua > /dev/null 2>&1 || true
  if [ "${KEEP_RUNNING:-}" = "1" ]; then echo "Left running: $BASE (stop: $COMPOSE down -v)"; return; fi
  $COMPOSE down -v > /dev/null 2>&1 || true
  rm -rf .smoke-gateway smoke-test-mqtt.crt
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
$COMPOSE up -d --no-build orbitos > smoke-test-up.log 2>&1 || { cat smoke-test-up.log; fail "compose up"; }
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
SYSADMIN_EMAIL=$(json 'print(d["sysadminEmail"])' < smoke-test-body.json)
SYSADMIN_FIRST=$(json 'print(d["sysadminPassword"])' < smoke-test-body.json)
[ "$SYSADMIN_FIRST" != "$PASSWORD" ] && [ ${#SYSADMIN_FIRST} -ge 20 ] || fail "platform admin password not generated"
pass "setup completed (organization, accounts, device, dashboard)"
pass "platform admin got its own generated password (${#SYSADMIN_FIRST} characters)"

curl -fsS "$BASE/api/noauth/orbitos/setup" | json 'assert d["required"] is False, d' || fail "setup still required"
[ "$(post /api/noauth/orbitos/setup "$(request "$setup_code" "$PASSWORD")")" = "409" ] || fail "setup can run twice"
pass "setup page closed after use"

token=$(login admin@example.com "$PASSWORD") || fail "organization admin login"
pass "organization admin signs in"

# ---- Phase 4: forced password change at first sign-in ----
code=$(post /api/auth/login "{\"username\":\"$SYSADMIN_EMAIL\",\"password\":\"$PASSWORD\"}")
[ "$code" = "401" ] || fail "platform admin accepts the organization admin's password ($code)"
code=$(post /api/auth/login "{\"username\":\"$SYSADMIN_EMAIL\",\"password\":\"$SYSADMIN_FIRST\"}")
[ "$code" = "401" ] || fail "generated password works without a change ($code)"
reset_token=$(json 'assert d["errorCode"] == 15, d; print(d["resetToken"])' < smoke-test-body.json) || fail "no forced change"
[ "$(post /api/noauth/resetPassword "{\"resetToken\":\"$reset_token\",\"password\":\"too-short\"}")" = "400" ] \
  || fail "new password shorter than 12 characters accepted"
SYSADMIN_PASSWORD="new-$PASSWORD"
[ "$(post /api/noauth/resetPassword "{\"resetToken\":\"$reset_token\",\"password\":\"$SYSADMIN_PASSWORD\"}")" = "200" ] \
  || { cat smoke-test-body.json; fail "password change"; }
systoken=$(login "$SYSADMIN_EMAIL" "$SYSADMIN_PASSWORD") || fail "platform admin login with the new password"
[ "$(post /api/auth/login "{\"username\":\"$SYSADMIN_EMAIL\",\"password\":\"$SYSADMIN_FIRST\"}")" = "401" ] \
  || fail "generated password still works after the change"
pass "platform admin must change the generated password at first sign-in (12+ characters)"

# ---- Phase 4: secure defaults (password policy, retention) ----
curl -fsS -H "X-Authorization: Bearer $systoken" "$BASE/api/admin/securitySettings" \
  | json 'p=d["passwordPolicy"]; assert p["minimumLength"] >= 12, p; assert d["maxFailedLoginAttempts"] == 10, d' \
  || fail "password policy"
profile_id=$(curl -fsS -H "X-Authorization: Bearer $systoken" "$BASE/api/tenantProfileInfo/default" | json 'print(d["id"]["id"])')
curl -fsS -H "X-Authorization: Bearer $systoken" "$BASE/api/tenantProfile/$profile_id" \
  | json 'c=d["profileData"]["configuration"]; assert (c["defaultStorageTtlDays"], c["alarmsTtlDays"], c["rpcTtlDays"]) == (365, 365, 30), c' \
  || fail "retention defaults"
pass "secure defaults: 12-character passwords, lockout after 10 failures, retention 365/365/30 days"

# ---- Phase 4: two-factor sign-in (authenticator app) ----
totp() {
  "$PY" -c "import sys,hmac,hashlib,struct,time,base64; s=sys.argv[1]; k=base64.b32decode(s+'='*(-len(s)%8)); h=hmac.new(k,struct.pack('>Q',int(time.time())//30),hashlib.sha1).digest(); o=h[-1]&15; print('%06d'%((struct.unpack('>I',h[o:o+4])[0]&0x7fffffff)%1000000))" "$1"
}
curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/2fa/providers" | json 'assert "TOTP" in str(d), d' \
  || fail "authenticator app not offered"
curl -fsS -X POST -H "X-Authorization: Bearer $token" "$BASE/api/2fa/account/config/generate?providerType=TOTP" > smoke-test-totp.json \
  || fail "authenticator setup"
secret=$("$PY" -c "import json,urllib.parse; d=json.load(open('smoke-test-totp.json')); print(urllib.parse.parse_qs(urllib.parse.urlparse(d['authUrl']).query)['secret'][0])")
curl -fsS -H "X-Authorization: Bearer $token" -H 'Content-Type: application/json' -d @smoke-test-totp.json \
  "$BASE/api/2fa/account/config?verificationCode=$(totp "$secret")" > /dev/null || fail "enable authenticator"
pre=$(curl -fsS -H 'Content-Type: application/json' -d "{\"username\":\"admin@example.com\",\"password\":\"$PASSWORD\"}" \
  "$BASE/api/auth/login" | json 'assert d.get("scope") == "PRE_VERIFICATION_TOKEN", d; print(d["token"])') \
  || fail "password alone still gives full access"
sleep 31  # next 30-second code, so the same code is never used twice
token=$(curl -fsS -X POST -H "X-Authorization: Bearer $pre" \
  "$BASE/api/auth/2fa/verification/check?providerType=TOTP&verificationCode=$(totp "$secret")" | json 'print(d["token"])') \
  || fail "sign-in with the authenticator code"
rm -f smoke-test-totp.json
pass "two-factor sign-in: password, then authenticator code"

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

# ---- Phase 4: privacy requests (export, erase) ----
customer_id=$(curl -fsS -H "X-Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"title":"Client test"}' "$BASE/api/customer" | json 'print(d["id"]["id"])') || fail "create customer"
person_id=$(curl -fsS -H "X-Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d "{\"email\":\"person@example.com\",\"firstName\":\"Pat\",\"authority\":\"CUSTOMER_USER\",\"customerId\":{\"id\":\"$customer_id\",\"entityType\":\"CUSTOMER\"}}" \
  "$BASE/api/user?sendActivationMail=false" | json 'print(d["id"]["id"])') || fail "create customer user"
sleep 2
curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/orbitos/privacy/users/$person_id/export" \
  | json 'assert d["account"]["email"] == "person@example.com"; assert d["customer"]["title"] == "Client test"; assert len(d["actionsAboutThisAccount"]) >= 1, d.keys()' \
  || fail "privacy export"
pass "privacy export: account, customer and audit entries in one JSON file"

sysadmin_id=$(curl -fsS -H "X-Authorization: Bearer $systoken" "$BASE/api/auth/user" | json 'print(d["id"]["id"])')
code=$(curl -s -o /dev/null -w '%{http_code}' -H "X-Authorization: Bearer $token" "$BASE/api/orbitos/privacy/users/$sysadmin_id/export")
[ "$code" = "404" ] || fail "organization admin can export the platform admin ($code)"
me_id=$(curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/auth/user" | json 'print(d["id"]["id"])')
code=$(curl -s -o /dev/null -w '%{http_code}' -X POST -H "X-Authorization: Bearer $token" "$BASE/api/orbitos/privacy/users/$me_id/erase")
[ "$code" = "403" ] || fail "an admin can erase their own account ($code)"
pass "privacy tools limited to the admin's own organization; no self-erasure"

curl -fsS -X POST -H "X-Authorization: Bearer $token" "$BASE/api/orbitos/privacy/users/$person_id/erase" \
  | json 'assert d["pseudonym"].startswith("erased-user-"), d' || fail "privacy erase"
code=$(curl -s -o /dev/null -w '%{http_code}' -H "X-Authorization: Bearer $token" "$BASE/api/user/$person_id")
[ "$code" = "404" ] || fail "erased account still exists ($code)"
sleep 3
left=$($COMPOSE exec -T postgres psql -U orbitos -d orbitos -tA \
  -c "SELECT count(*) FROM audit_log WHERE user_name = 'person@example.com' OR entity_name = 'person@example.com'")
[ "$left" = "0" ] || fail "email still in $left audit entries (names)"
pass "privacy erasure: account deleted, email replaced by a pseudonym in the audit log"

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
pre=$(login admin@example.com "$PASSWORD") || fail "login after restore"
token=$(curl -fsS -X POST -H "X-Authorization: Bearer $pre" \
  "$BASE/api/auth/2fa/verification/check?providerType=TOTP&verificationCode=$(totp "$secret")" | json 'print(d["token"])') \
  || fail "two-factor login after restore"
curl -fsS -H "X-Authorization: Bearer $token" \
  "$BASE/api/plugins/telemetry/DEVICE/$device_id/values/timeseries?keys=temperature" \
  | json 'assert d["temperature"][0]["value"] in ("21.5", 21.5), d' || fail "restore did not bring back the backed-up value"
pass "restore brings back the data as it was at backup time"

# ---- Phase 5: real protocol end to end (MQTT), both directions ----
"$PY" orbitos/scripts/mqtt_device.py --port "$ORBITOS_MQTT_PORT" --token "$device_token" --once \
  --values '{"temperature":19.25,"via":"mqtt"}' > /dev/null || fail "MQTT publish"
sleep 2
curl -fsS -H "X-Authorization: Bearer $token" \
  "$BASE/api/plugins/telemetry/DEVICE/$device_id/values/timeseries?keys=temperature,via" \
  | json 'assert d["via"][0]["value"] == "mqtt" and d["temperature"][0]["value"] in ("19.25", 19.25), d' \
  || fail "MQTT value not readable through the REST API"
pass "MQTT device -> platform -> REST API"

"$PY" orbitos/scripts/mqtt_device.py --port "$ORBITOS_MQTT_PORT" --token "$device_token" --interval 0 --duration 15 > /dev/null &
device_pid=$!
sleep 3
curl -fsS -H "X-Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"method":"getStatus","params":{"probe":42},"timeout":10000}' "$BASE/api/plugins/rpc/twoway/$device_id" \
  | json 'assert d["ok"] is True and d["received"] == {"probe": 42}, d' || fail "command to the MQTT device"
wait "$device_pid" || true
code=$(curl -s -o /dev/null -w '%{http_code}' -X POST -d '{}' "$BASE/api/v1/not-a-device-token/telemetry")
[ "$code" = "401" ] || [ "$code" = "404" ] || fail "unknown device token accepted ($code)"
pass "REST API -> platform -> MQTT device -> answer back (two-way command); unknown token refused"

"$PY" orbitos/scripts/coap_device.py --port "$ORBITOS_COAP_PORT" --token "$device_token" \
  --values '{"temperature":20.75,"via":"coap"}' > /dev/null || fail "CoAP publish"
"$PY" orbitos/scripts/coap_device.py --port "$ORBITOS_COAP_PORT" --token not-a-device-token > /dev/null 2>&1 \
  && fail "CoAP accepted an unknown device token"
sleep 1
curl -fsS -H "X-Authorization: Bearer $token" \
  "$BASE/api/plugins/telemetry/DEVICE/$device_id/values/timeseries?keys=temperature,via" \
  | json 'assert d["via"][0]["value"] == "coap" and d["temperature"][0]["value"] in ("20.75", 20.75), d' \
  || fail "CoAP value not readable through the REST API"
pass "CoAP device (UDP) -> platform -> REST API; unknown token refused"

curl -fsS "$BASE/v3/api-docs/api" > smoke-test-api.json || fail "API reference"
"$PY" -c "import json,re; s=open('smoke-test-api.json',encoding='utf-8').read(); d=json.loads(s); assert d['info']['title']=='OrbitOS REST API', d['info']; assert len(d['paths'])>300; n=len(re.findall('ThingsBoard',s)); assert n==0, n" \
  || fail "API reference not rebranded"
rm -f smoke-test-api.json
pass "API reference: $(curl -fsS "$BASE/v3/api-docs/api" | json 'print(len(d["paths"]))') endpoints, rebranded"

# ---- Phase 6: encrypted MQTT (TLS, port 8883) ----
$COMPOSE cp orbitos:/var/lib/orbitos/tls/mqtt-server.crt smoke-test-mqtt.crt > /dev/null 2>&1 || fail "MQTT TLS certificate not created"
"$PY" orbitos/scripts/mqtt_device.py --port "$ORBITOS_MQTTS_PORT" --tls --cafile smoke-test-mqtt.crt --token "$device_token" \
  --once --values '{"tlsprobe":7}' > /dev/null || fail "MQTT over TLS"
if "$PY" orbitos/scripts/mqtt_device.py --port "$ORBITOS_MQTTS_PORT" --tls --token "$device_token" --once > /dev/null 2>&1; then
  fail "MQTT over TLS accepted without trusting the certificate"
fi
sleep 2
curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/plugins/telemetry/DEVICE/$device_id/values/timeseries?keys=tlsprobe" \
  | json 'assert d["tlsprobe"][0]["value"] in ("7", 7), d' || fail "TLS value not stored"
pass "encrypted MQTT (TLS 8883): certificate created offline, value stored, untrusted certificate refused"

# ---- Phase 6: IoT Gateway add-on with the simulated Modbus meter ----
docker image inspect localhost/orbitos-gateway:3.8.5 > /dev/null 2>&1 || $COMPOSE build gateway > /dev/null
MSYS_NO_PATHCONV=1 docker run --rm --entrypoint grep localhost/orbitos-gateway:3.8.5 -q "OrbitOS: no calls home" \
  /thingsboard_gateway/tb_utility/tb_updater.py || fail "gateway update check not disabled"
gateway_id=$(curl -fsS -H "X-Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"name":"Gateway 1","additionalInfo":{"gateway":true}}' "$BASE/api/device" | json 'print(d["id"]["id"])') || fail "create gateway"
ORBITOS_GATEWAY_TOKEN=$(curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/device/$gateway_id/credentials" \
  | json 'print(d["credentialsId"])')
export ORBITOS_GATEWAY_TOKEN
rm -rf .smoke-gateway && mkdir -p .smoke-gateway && cp orbitos/gateway/demo-config/* .smoke-gateway/
$COMPOSE up -d --no-build --no-deps gateway modbus-simulator > smoke-test-up.log 2>&1 || { cat smoke-test-up.log; fail "gateway start"; }
meter=""
for _ in $(seq 1 30); do
  meter=$(curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/tenant/devices?deviceName=Modbus%20demo%20meter" 2>/dev/null \
    | json 'print(d["id"]["id"])' 2>/dev/null) && break
  sleep 3
done
[ -n "$meter" ] || fail "Modbus device not created by the gateway"
sleep 5
curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/plugins/telemetry/DEVICE/$meter/values/timeseries?keys=temperature,voltage" \
  | json 'assert 15 <= float(d["temperature"][0]["value"]) <= 30 and 220 <= float(d["voltage"][0]["value"]) <= 240, d' \
  || fail "Modbus values not received"
if $COMPOSE logs gateway 2>&1 | grep -qiE 'pip install|Installing collected|updates\.thingsboard'; then
  fail "gateway downloaded something or checked for updates"
fi
pass "IoT Gateway add-on: Modbus meter -> gateway -> platform; no downloads, no update checks"

# ---- optional: OPC UA through the gateway (ORBITOS_TEST_OPCUA=1). Needs internet once: the OPC UA
# library (asyncua, LGPL) is not bundled, so the simulator and the gateway connector download it. ----
if [ "${ORBITOS_TEST_OPCUA:-}" = "1" ]; then
  net=$(docker inspect -f '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}' "$($COMPOSE ps -q orbitos)")
  docker rm -f orbitos-smoke-opcua > /dev/null 2>&1 || true
  MSYS_NO_PATHCONV=1 docker create --name orbitos-smoke-opcua --network "$net" --network-alias opcua-simulator \
    --entrypoint sh localhost/orbitos-gateway:3.8.5 -c 'pip install -q asyncua==1.1.5 && python /tmp/opcua_simulator.py' > /dev/null
  MSYS_NO_PATHCONV=1 docker cp orbitos/gateway/opcua_simulator.py orbitos-smoke-opcua:/tmp/opcua_simulator.py > /dev/null
  docker start orbitos-smoke-opcua > /dev/null
  "$PY" - <<'EOF'
import json
path = ".smoke-gateway/"
gw = json.load(open(path + "tb_gateway.json", encoding="utf-8"))
gw["connectors"].append({"type": "opcua", "name": "OPC UA demo", "configuration": "opcua.json"})
json.dump(gw, open(path + "tb_gateway.json", "w", encoding="utf-8"), indent=2)
conf = {
  "server": {"url": "opc.tcp://opcua-simulator:4840/freeopcua/server/", "timeoutInMillis": 5000,
             "scanPeriodInMillis": 3600000, "pollPeriodInMillis": 2000, "enableSubscriptions": False,
             "sessionTimeoutInMillis": 120000, "showMap": False, "security": "None",
             "identity": {"type": "anonymous"}},
  "mapping": [{
    "deviceNodeSource": "path", "deviceNodePattern": "Root\\.Objects\\.MyObject",
    "deviceInfo": {"deviceNameExpression": "OPC UA demo machine", "deviceNameExpressionSource": "constant",
                   "deviceProfileExpression": "default", "deviceProfileExpressionSource": "constant"},
    "attributes": [], "timeseries": [
      {"key": "temperature", "type": "path", "value": "${Temperature}"},
      {"key": "humidity", "type": "path", "value": "${Humidity}"}],
    "rpc_methods": [], "attributes_updates": []}]
}
json.dump(conf, open(path + "opcua.json", "w", encoding="utf-8"), indent=2)
EOF
  $COMPOSE restart gateway > /dev/null 2>&1
  machine=""
  for _ in $(seq 1 60); do
    machine=$(curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/tenant/devices?deviceName=OPC%20UA%20demo%20machine" 2>/dev/null \
      | json 'print(d["id"]["id"])' 2>/dev/null) && break
    sleep 3
  done
  [ -n "$machine" ] || fail "OPC UA device not created by the gateway (see: docker logs orbitos-smoke-opcua)"
  sleep 6
  curl -fsS -H "X-Authorization: Bearer $token" "$BASE/api/plugins/telemetry/DEVICE/$machine/values/timeseries?keys=temperature,humidity" \
    | json 'assert 17 <= float(d["temperature"][0]["value"]) <= 25 and 39 <= float(d["humidity"][0]["value"]) <= 51, d' \
    || fail "OPC UA values not received"
  docker rm -f orbitos-smoke-opcua > /dev/null 2>&1 || true
  pass "IoT Gateway add-on: OPC UA machine (simulated) -> gateway -> platform"
fi

# ---- Phase 4: tamper-evident audit log (last: it damages the log on purpose) ----
psql() { $COMPOSE exec -T postgres psql -U orbitos -d orbitos -tA -c "$1"; }
systoken=$(login "$SYSADMIN_EMAIL" "$SYSADMIN_PASSWORD") || fail "platform admin login after restore"
sleep 2
curl -fsS -H "X-Authorization: Bearer $systoken" "$BASE/api/orbitos/audit/verify" \
  | json 'assert d["status"] == "intact" and d["checked"] > 0, d' || fail "audit chain not intact (API)"
$COMPOSE run --rm orbitos verify-audit > smoke-test-audit.log 2>&1 || { cat smoke-test-audit.log; fail "audit chain not intact (command)"; }
pass "audit log intact: $(grep -oE '"checked" : [0-9]+' smoke-test-audit.log) entries"

victim=$(psql "SELECT audit_log_id FROM orbitos_audit_chain ORDER BY seq LIMIT 1 OFFSET 3")
psql "UPDATE audit_log SET action_status = 'FAILURE' WHERE id = '$victim'" > /dev/null
set +e
$COMPOSE run --rm orbitos verify-audit > smoke-test-audit.log 2>&1
status=$?
set -e
[ "$status" = "3" ] && grep -q '"changed"' smoke-test-audit.log && grep -q "$victim" smoke-test-audit.log \
  || { cat smoke-test-audit.log; fail "changed audit entry not detected"; }
psql "DELETE FROM audit_log WHERE id = (SELECT audit_log_id FROM orbitos_audit_chain ORDER BY seq LIMIT 1 OFFSET 5)" > /dev/null
curl -fsS -H "X-Authorization: Bearer $systoken" "$BASE/api/orbitos/audit/verify" \
  | json 'kinds={p["kind"] for p in d["problems"]}; assert d["status"] == "broken" and {"changed", "deleted"} <= kinds, d' \
  || fail "deleted audit entry not detected"
pass "audit log tampering detected (changed entry, deleted entry)"

rm -f smoke-test-body.json smoke-test-audit.log
echo "All smoke tests passed."
