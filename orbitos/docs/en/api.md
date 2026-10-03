# APIs: REST, WebSocket, webhooks

*[Version française](../fr/api.md)*

For applications that read data or control devices. To *send* device data, see
[Connect devices](connect-devices.md).

## Reference

Every REST call is described, with a "Try it out" button, at **http://HOST:8080/swagger-ui.html**
(served by your own server, works offline). Click *Authorize* and paste `Bearer <token>`.

## Sign in

```bash
curl -X POST http://HOST:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username": "you@example.com", "password": "your password"}'
```

The answer contains `token` (valid 2.5 hours) and `refreshToken`. Send the token with every call:

```
X-Authorization: Bearer <token>
```

Use a dedicated user for each application, with the smallest role that works (a customer user if
it only reads that customer's devices). If two-factor sign-in is on for that user, the first answer
is a pre-verification token: send the code to `POST /api/auth/2fa/verification/check?providerType=TOTP&verificationCode=123456`.

## Common calls

| What | Call |
|---|---|
| List your devices | `GET /api/tenant/devices?pageSize=20&page=0` |
| Latest values | `GET /api/plugins/telemetry/DEVICE/{deviceId}/values/timeseries?keys=temperature,humidity` |
| History | same, plus `&startTs=<ms>&endTs=<ms>&limit=1000` |
| Device properties | `GET /api/plugins/telemetry/DEVICE/{deviceId}/values/attributes` |
| Send a command and wait for the answer | `POST /api/plugins/rpc/twoway/{deviceId}` with `{"method": "getStatus", "params": {}}` |
| Send a command without waiting | `POST /api/plugins/rpc/oneway/{deviceId}` |
| Alarms | `GET /api/alarm/DEVICE/{deviceId}?pageSize=20&page=0` |

## Live updates (WebSocket)

Connect to `ws://HOST:8080/api/ws` (or `wss://` with HTTPS), then send:

```json
{"authCmd": {"cmdId": 0, "token": "<token>"},
 "cmds": [{"type": "TIMESERIES", "entityType": "DEVICE", "entityId": "<deviceId>",
           "scope": "LATEST_TELEMETRY", "cmdId": 10}]}
```

You receive the current values, then every new value as it arrives:
`{"subscriptionId": 10, "data": {"temperature": [[1767225600000, "21.5"]]}}`.

In a browser:

```js
const ws = new WebSocket('ws://HOST:8080/api/ws');
ws.onopen = () => ws.send(JSON.stringify({authCmd: {cmdId: 0, token}, cmds: [
  {type: 'TIMESERIES', entityType: 'DEVICE', entityId: deviceId, scope: 'LATEST_TELEMETRY', cmdId: 10}]}));
ws.onmessage = event => console.log(JSON.parse(event.data).data);
```

## Webhooks

**Incoming** (another service sends data to OrbitOS): give it the device HTTP address
`http://HOST:8080/api/v1/<device token>/telemetry`. See [Connect devices](connect-devices.md).

**Outgoing** (OrbitOS calls your service when data arrives):

1. *Rule chains* > open the root rule chain.
2. Add a **REST API call** node. URL: your service, for example `https://example.ca/hooks/iot`;
   method `POST`. The body is the device message (JSON). Headers can use message details, for
   example `X-Device: ${deviceName}`.
3. Connect the *Post telemetry* output of the *Message Type Switch* node to it, and save.

Nothing is sent anywhere until you add such a node: it is an outbound connection you choose.
Status: documented, **not tested** end to end in this project.
