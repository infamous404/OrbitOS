# Connect devices

*[Version française](../fr/connect-devices.md)*

Each device has an **access token** (device page > *Copy access token*). The device sends it with
every message; that is how OrbitOS knows which device is talking. Keep it secret.

Replace `HOST` with your server's address and `TOKEN` with the device token. From another
computer, set `ORBITOS_BIND=0.0.0.0` in `.env` first (see the [install guide](install.md)).

## MQTT (recommended for most devices)

| Setting | Value |
|---|---|
| Server | `HOST`, port `1883` |
| User name | the device token (no password) |
| Send values | topic `v1/devices/me/telemetry`, JSON such as `{"temperature": 21.5}` |
| Send device properties | topic `v1/devices/me/attributes`, JSON such as `{"firmware": "1.2"}` |
| Receive commands | subscribe to `v1/devices/me/rpc/request/+` |
| Answer a command | topic `v1/devices/me/rpc/response/<id>` (the `<id>` from the request topic) |

Values with their own time: `{"ts": 1767225600000, "values": {"temperature": 21.5}}` (milliseconds since 1970, UTC).

Try it without writing code, with the included simulator (Python 3, nothing to install):

```bash
python orbitos/scripts/mqtt_device.py --host HOST --token TOKEN
```

It sends a temperature and a humidity every 5 seconds and answers commands. Read it as an
example for your own device code: it is about 200 lines. Other ways:

```bash
mosquitto_pub -h HOST -p 1883 -u TOKEN -t v1/devices/me/telemetry -m '{"temperature": 21.5}'
```

On microcontrollers (ESP32, Arduino), any MQTT library works (for example PubSubClient): connect
with the token as user name and publish to the topic above.

## HTTP

```bash
curl -X POST http://HOST:8080/api/v1/TOKEN/telemetry -H "Content-Type: application/json" -d '{"temperature": 21.5}'
```

Properties: `/api/v1/TOKEN/attributes`. Good for devices that wake up, send and sleep, and for
other services that push data (an "incoming webhook": point them at this address).

## CoAP (small battery devices)

`coap://HOST:5683/api/v1/TOKEN/telemetry`, same JSON. Example with libcoap:
`coap-client -m post coap://HOST/api/v1/TOKEN/telemetry -e '{"temperature": 21.5}'`

No CoAP tool? This repository has a small one (Python 3, nothing to install):
`python orbitos/scripts/coap_device.py --host HOST --token TOKEN --values '{"temperature": 21.5}'`

## Encrypted MQTT (TLS)

Turn it on in `.env`, then `docker compose up -d`:

```
ORBITOS_MQTT_TLS=true
ORBITOS_DOMAIN=iot.example.ca      # the name or IP address devices use to reach the server
```

On first start, OrbitOS creates a certificate for that name (offline, valid about 2 years) and
listens on port **8883** (plain MQTT on 1883 keeps working). Devices must trust that certificate:

```bash
docker compose cp orbitos:/var/lib/orbitos/tls/mqtt-server.crt .
python orbitos/scripts/mqtt_device.py --host iot.example.ca --tls --cafile mqtt-server.crt --token TOKEN
```

With `mosquitto_pub`: add `-p 8883 --cafile mqtt-server.crt`. To use a certificate from a public
authority instead, set `MQTT_SSL_CREDENTIALS_TYPE=PEM`, `MQTT_SSL_PEM_CERT` and `MQTT_SSL_PEM_KEY`
(paths inside the container) in a `compose.override.yaml` that mounts your files.

CoAP is not encrypted. HTTP can go through the HTTPS option in the install guide.

## LoRaWAN (long-range battery sensors)

LoRaWAN sensors talk by radio to a LoRaWAN gateway (antenna). A LoRaWAN network server turns those
radio messages into data. **ChirpStack** (free, MIT licence) is the usual choice; it runs next to
OrbitOS and forwards decoded measurements to OrbitOS's HTTP device API.

1. Install ChirpStack with its own Docker Compose files (see chirpstack.io) and register your
   gateway and sensors there. Add a *codec* (decoder) to the device profile so the sensor's bytes
   become named values (temperature, battery...).
2. In OrbitOS, create one device per sensor and copy its token.
3. In ChirpStack, on each sensor, add the access-token *variable* expected by ChirpStack's
   integration for this platform family (ChirpStack lists it under the upstream project's name;
   see `NOTICE`), with the OrbitOS token as value.
4. In ChirpStack, *Application > Integrations*, add that integration with the server URL
   `http://HOST:8080`.

The decoded values then appear on the device like any other telemetry.
Status: documented, **not tested** with real LoRaWAN hardware in this project.

<!-- #gateway -->
## Modbus, OPC UA, BACnet and other industrial protocols

Machines, energy meters and building systems often speak Modbus, OPC UA or BACnet. OrbitOS has an
optional **IoT Gateway** add-on (Apache-2.0, from the upstream project, see `NOTICE`) that reads
them and sends the values to OrbitOS. The OrbitOS version of the gateway makes no calls home and
needs no internet connection for Modbus and BACnet (their libraries are included). The OPC UA library
(asyncua, LGPL-3.0) is not bundled: the OPC UA connector downloads it once, on first use.

**Try it with a simulated meter (no hardware):**

1. In OrbitOS, create a device named "Gateway 1", turn on **Is gateway**, and copy its token.
2. In `.env`: `ORBITOS_GATEWAY_TOKEN=<the token>`.
3. Copy the demo configuration, then start:
   ```bash
   mkdir -p gateway-config
   cp orbitos/gateway/demo-config/* gateway-config/
   docker compose --profile gateway --profile gateway-demo up -d
   ```
4. Within a minute, a device "Modbus demo meter" appears with temperature, humidity, voltage and power.

**With real machines:** start with `docker compose --profile gateway up -d` (no demo). Edit the
files in `gateway-config/`: list your connectors in `tb_gateway.json` (`"connectors"`), and describe
each machine in the connector file (for Modbus, `modbus.json`: address, polling period, which
registers become which values). Restart with `docker compose restart gateway`. Each machine
appears in OrbitOS as its own device. Serial Modbus (RS-485) needs the serial port passed to the
container (`devices:` in a `compose.override.yaml`).

The gateway can also run on a small computer next to the machines (it connects to OrbitOS by
MQTT, port 1883 or 8883): use the same image and settings (`host`, `port`, `accessToken`).

**OPC UA example:** `orbitos/gateway/opcua_simulator.py` is a simulated OPC UA machine, and the
optional OPC UA check of the test script (`ORBITOS_TEST_OPCUA=1 bash orbitos/scripts/smoke-test.sh`)
shows a complete working connector file (`opcua.json`). OPC UA server address format:
`opc.tcp://MACHINE:4840/...`.
