#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""A tiny MQTT device for OrbitOS, with no dependencies (Python 3.8+ only).

Use it to try the platform, or as a starting point for your own device code. It:
  - sends telemetry (temperature, humidity) to   v1/devices/me/telemetry
  - answers commands from the platform (RPC) on   v1/devices/me/rpc/request/+

Examples:
  python orbitos/scripts/mqtt_device.py --token YOUR_DEVICE_TOKEN
  python orbitos/scripts/mqtt_device.py --token T --once --values '{"temperature": 21.5}'
  python orbitos/scripts/mqtt_device.py --host 192.168.1.20 --token T --interval 10 --duration 60
  python orbitos/scripts/mqtt_device.py --token T --tls --cafile mqtt-server.crt

The device token is on the device's page (Copy access token) or on the last setup step.
MQTT 3.1.1, QoS 0. Plain on port 1883, or encrypted with --tls on port 8883.
"""
import argparse
import json
import random
import socket
import ssl
import struct
import sys
import time

TELEMETRY_TOPIC = "v1/devices/me/telemetry"
RPC_REQUESTS = "v1/devices/me/rpc/request/+"
RPC_REQUEST_PREFIX = "v1/devices/me/rpc/request/"
RPC_RESPONSE_PREFIX = "v1/devices/me/rpc/response/"


def _length(n):
    out = bytearray()
    while True:
        digit, n = n % 128, n // 128
        out.append(digit | (0x80 if n else 0))
        if not n:
            return bytes(out)


def _string(text):
    data = text.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def _packet(first_byte, body):
    return bytes([first_byte]) + _length(len(body)) + body


class MqttDevice:
    def __init__(self, host, port, token, client_id=None, keepalive=60, tls_context=None):
        self.sock = socket.create_connection((host, port), timeout=10)
        if tls_context is not None:
            self.sock = tls_context.wrap_socket(self.sock, server_hostname=host)
        self.keepalive = keepalive
        self.last_sent = time.time()
        client_id = client_id or "orbitos-demo-%06d" % random.randint(0, 999999)
        body = _string("MQTT") + bytes([4, 0x82]) + struct.pack(">H", keepalive) + _string(client_id) + _string(token)
        self._send(_packet(0x10, body))
        kind, payload = self._read()
        if kind != 0x20 or len(payload) < 2 or payload[1] != 0:
            code = payload[1] if len(payload) > 1 else "?"
            raise ConnectionError("connection refused (code %s): check the host, port and device token" % code)

    def _send(self, data):
        self.sock.sendall(data)
        self.last_sent = time.time()

    def _exact(self, n):
        data = b""
        while len(data) < n:
            chunk = self.sock.recv(n - len(data))
            if not chunk:
                raise ConnectionError("connection closed by the server")
            data += chunk
        return data

    def _read(self):
        first = self._exact(1)[0]
        length, shift = 0, 0
        while True:
            byte = self._exact(1)[0]
            length += (byte & 0x7F) << shift
            shift += 7
            if not byte & 0x80:
                break
        return first & 0xF0, self._exact(length)

    def publish(self, topic, message):
        self._send(_packet(0x30, _string(topic) + json.dumps(message).encode("utf-8")))

    def subscribe(self, topic, packet_id=1):
        self._send(_packet(0x82, struct.pack(">H", packet_id) + _string(topic) + b"\x00"))
        kind, _ = self._read()
        if kind != 0x90:
            raise ConnectionError("subscription refused")

    def poll(self, timeout):
        """Returns (topic, message) of the next published message, or None after `timeout` seconds."""
        self.sock.settimeout(timeout)
        try:
            kind, payload = self._read()
        except socket.timeout:
            return None
        finally:
            self.sock.settimeout(10)
        if kind != 0x30:
            return None
        topic_length = struct.unpack(">H", payload[:2])[0]
        topic = payload[2:2 + topic_length].decode("utf-8")
        body = payload[2 + topic_length:]
        try:
            return topic, json.loads(body.decode("utf-8") or "null")
        except ValueError:
            return topic, body.decode("utf-8", "replace")

    def ping_if_needed(self):
        if time.time() - self.last_sent > self.keepalive / 2:
            self._send(b"\xc0\x00")

    def close(self):
        try:
            self._send(b"\xe0\x00")
        finally:
            self.sock.close()


def next_values(previous):
    temperature = previous.get("temperature", 21.0) + random.uniform(-0.3, 0.3)
    humidity = previous.get("humidity", 45.0) + random.uniform(-1.0, 1.0)
    return {"temperature": round(temperature, 2), "humidity": round(min(100.0, max(0.0, humidity)), 1)}


def answer(request_id, request, device):
    """Default command handler: echoes the command. Replace it with your own actions."""
    method = request.get("method") if isinstance(request, dict) else None
    params = request.get("params") if isinstance(request, dict) else None
    result = {"ok": True, "method": method, "received": params}
    device.publish(RPC_RESPONSE_PREFIX + request_id, result)
    print("command %r answered" % method, flush=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, help="1883, or 8883 with --tls")
    parser.add_argument("--tls", action="store_true", help="encrypted MQTT (server started with ORBITOS_MQTT_TLS=true)")
    parser.add_argument("--cafile", help="certificate to trust with --tls (for example mqtt-server.crt from the server)")
    parser.add_argument("--token", required=True, help="device access token")
    parser.add_argument("--interval", type=float, default=5.0, help="seconds between values (0 = send none)")
    parser.add_argument("--duration", type=float, default=0, help="stop after this many seconds (0 = run until Ctrl+C)")
    parser.add_argument("--once", action="store_true", help="send one value and stop")
    parser.add_argument("--values", help='JSON to send instead of simulated values, e.g. \'{"temperature": 21.5}\'')
    args = parser.parse_args()

    fixed = json.loads(args.values) if args.values else None
    port = args.port or (8883 if args.tls else 1883)
    tls_context = ssl.create_default_context(cafile=args.cafile) if args.tls else None
    try:
        device = MqttDevice(args.host, port, args.token, tls_context=tls_context)
    except (OSError, ConnectionError) as e:
        print("Cannot connect to %s:%s: %s" % (args.host, port, e), file=sys.stderr)
        return 1
    print("Connected to %s:%s%s" % (args.host, port, " (TLS)" if args.tls else ""), flush=True)
    try:
        if args.once:
            device.publish(TELEMETRY_TOPIC, fixed or next_values({}))
            print("1 value sent", flush=True)
            return 0
        device.subscribe(RPC_REQUESTS)
        values, next_send = {}, time.time()
        end = time.time() + args.duration if args.duration else None
        while end is None or time.time() < end:
            if args.interval > 0 and time.time() >= next_send:
                values = fixed or next_values(values)
                device.publish(TELEMETRY_TOPIC, values)
                print("sent %s" % json.dumps(values), flush=True)
                next_send = time.time() + args.interval
            message = device.poll(0.5)
            if message and message[0].startswith(RPC_REQUEST_PREFIX):
                answer(message[0][len(RPC_REQUEST_PREFIX):], message[1], device)
            device.ping_if_needed()
        return 0
    except KeyboardInterrupt:
        return 0
    finally:
        device.close()


if __name__ == "__main__":
    sys.exit(main())
