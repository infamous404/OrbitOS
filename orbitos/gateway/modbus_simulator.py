#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""A small Modbus TCP device (an energy and climate meter) for trying the IoT Gateway without hardware.

Python standard library only. Holding/input registers (values x10):
  0 temperature (C)   1 humidity (%)   2 voltage (V)   3 power (W)
Coils: 0 relay. Supports function codes 1, 2, 3, 4, 5, 6.

    python modbus_simulator.py --port 5020
"""
import argparse
import random
import socketserver
import struct
import threading
import time

registers = [215, 450, 2300, 1200]
coils = [False]
lock = threading.Lock()


def drift():
    while True:
        time.sleep(1)
        with lock:
            registers[0] = max(150, min(300, registers[0] + random.randint(-2, 2)))
            registers[1] = max(200, min(800, registers[1] + random.randint(-5, 5)))
            registers[2] = max(2250, min(2350, registers[2] + random.randint(-3, 3)))
            registers[3] = max(0, min(5000, registers[3] + random.randint(-40, 40)))


class Handler(socketserver.BaseRequestHandler):
    def handle(self):
        while True:
            header = self._exact(7)
            if not header:
                return
            transaction, protocol, length, unit = struct.unpack(">HHHB", header)
            pdu = self._exact(length - 1)
            if not pdu:
                return
            self.request.sendall(self._mbap(transaction, unit, self._answer(pdu)))

    def _exact(self, n):
        data = b""
        while len(data) < n:
            chunk = self.request.recv(n - len(data))
            if not chunk:
                return None
            data += chunk
        return data

    @staticmethod
    def _mbap(transaction, unit, pdu):
        return struct.pack(">HHHB", transaction, 0, len(pdu) + 1, unit) + pdu

    @staticmethod
    def _answer(pdu):
        function = pdu[0]
        with lock:
            if function in (3, 4):
                start, count = struct.unpack(">HH", pdu[1:5])
                values = [registers[i] if i < len(registers) else 0 for i in range(start, start + count)]
                return bytes([function, 2 * count]) + struct.pack(">%dH" % count, *values)
            if function in (1, 2):
                start, count = struct.unpack(">HH", pdu[1:5])
                bits = [coils[i] if i < len(coils) else False for i in range(start, start + count)]
                packed = bytearray((count + 7) // 8)
                for i, bit in enumerate(bits):
                    if bit:
                        packed[i // 8] |= 1 << (i % 8)
                return bytes([function, len(packed)]) + bytes(packed)
            if function == 5:
                address, value = struct.unpack(">HH", pdu[1:5])
                if address < len(coils):
                    coils[address] = value == 0xFF00
                return pdu[:5]
            if function == 6:
                address, value = struct.unpack(">HH", pdu[1:5])
                if address < len(registers):
                    registers[address] = value
                return pdu[:5]
        return bytes([function | 0x80, 1])  # illegal function


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--port", type=int, default=5020)
    args = parser.parse_args()
    threading.Thread(target=drift, daemon=True).start()
    socketserver.ThreadingTCPServer.allow_reuse_address = True
    with socketserver.ThreadingTCPServer(("0.0.0.0", args.port), Handler) as server:
        print("Modbus simulator listening on port %d" % args.port, flush=True)
        server.serve_forever()


if __name__ == "__main__":
    main()
