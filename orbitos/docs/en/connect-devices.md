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

## Encryption

MQTT and CoAP above are not encrypted. Use them on a network you trust, or through a VPN.
MQTT over TLS (port 8883) is not set up yet. HTTP can go through the HTTPS option in the install guide.

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

## Modbus, OPC UA, BACnet and other industrial protocols

Machines, energy meters and building systems often speak Modbus (or OPC UA, BACnet...). The free
**IoT Gateway** (Apache-2.0, from the upstream project, see `NOTICE`) runs on a small computer near
the machines, reads them, and sends the values to OrbitOS by MQTT.

1. In OrbitOS, create a device for the gateway and turn on **Is gateway**. Copy its token.
2. Install the IoT Gateway (Python package or Docker image; see its documentation) and set, in its
   main configuration: host `HOST`, port `1883`, the gateway token.
3. Turn on the Modbus connector and describe each machine: address, polling period, and which
   registers become which values. Each machine appears in OrbitOS as its own device, created automatically.

Status: documented, **not tested** with real Modbus hardware in this project.
