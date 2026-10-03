#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""
A tiny CoAP device for testing OrbitOS (Python 3 standard library only, no install needed).

Sends one telemetry value over CoAP (UDP) with a device access token:

    python orbitos/scripts/coap_device.py --token YOUR_DEVICE_TOKEN --values '{"temperature":21.5}'

Options: --host (default 127.0.0.1), --port (default 5683).
Exit code 0 when the platform answers "2.01 Created" or "2.04 Changed".
"""
import argparse
import json
import os
import socket
import sys

CON, ACK = 0, 2
POST = 0x02
URI_PATH, CONTENT_FORMAT = 11, 12
JSON_FORMAT = 50


def option(delta, value):
    def nibble(n):
        if n < 13:
            return n, b""
        if n < 269:
            return 13, bytes([n - 13])
        return 14, (n - 269).to_bytes(2, "big")
    d, d_ext = nibble(delta)
    length, l_ext = nibble(len(value))
    return bytes([(d << 4) | length]) + d_ext + l_ext + value


def request(path_parts, payload, message_id, token):
    header = bytes([(1 << 6) | (CON << 4) | len(token), POST]) + message_id.to_bytes(2, "big") + token
    options, last = b"", 0
    for part in path_parts:
        options += option(URI_PATH - last, part.encode("utf-8"))
        last = URI_PATH
    options += option(CONTENT_FORMAT - last, bytes([JSON_FORMAT]))
    return header + options + b"\xff" + payload


def code_text(code):
    return f"{code >> 5}.{code & 0x1f:02d}"


def main():
    parser = argparse.ArgumentParser(description="Send one value to OrbitOS over CoAP.")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=5683)
    parser.add_argument("--token", required=True, help="device access token")
    parser.add_argument("--values", default='{"temperature":21.5}', help="JSON object to send")
    args = parser.parse_args()

    payload = json.dumps(json.loads(args.values)).encode("utf-8")
    message_id = int.from_bytes(os.urandom(2), "big")
    token = os.urandom(4)
    packet = request(["api", "v1", args.token, "telemetry"], payload, message_id, token)

    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
        sock.settimeout(2)
        for _ in range(4):  # CoAP retransmits confirmable messages
            sock.sendto(packet, (args.host, args.port))
            try:
                while True:
                    data, _ = sock.recvfrom(2048)
                    if len(data) >= 4 and int.from_bytes(data[2:4], "big") == message_id:
                        break
                break
            except socket.timeout:
                continue
        else:
            print("no answer from the platform (is CoAP port open?)", file=sys.stderr)
            return 1

        if data[1] == 0:
            # Empty acknowledgement: the answer follows in a separate message with our token.
            sock.settimeout(10)
            while True:
                try:
                    data, _ = sock.recvfrom(2048)
                except socket.timeout:
                    print("no answer after the acknowledgement", file=sys.stderr)
                    return 1
                tkl = data[0] & 0x0f
                if data[4:4 + tkl] == token:
                    if (data[0] >> 4) & 0x03 == CON:
                        sock.sendto(bytes([(1 << 6) | (ACK << 4), 0]) + data[2:4], (args.host, args.port))
                    break

    code = data[1]
    print(f"answer: {code_text(code)}")
    return 0 if code in (0x41, 0x44) else 1


if __name__ == "__main__":
    sys.exit(main())
