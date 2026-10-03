# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""
Simulated OPC UA machine for testing the gateway's OPC UA connector (test use only).

Needs the asyncua library (LGPL-3.0), which OrbitOS does not bundle: the optional OPC UA test
installs it in a throw-away container. Serves opc.tcp://0.0.0.0:4840/freeopcua/server/ with an
object "MyObject" holding Temperature, Humidity, Relay, Frequency and Power.
"""
import asyncio
import math
import time

from asyncua import Server


async def main():
    server = Server()
    await server.init()
    server.set_endpoint("opc.tcp://0.0.0.0:4840/freeopcua/server/")
    server.set_server_name("OrbitOS OPC UA simulator")
    idx = await server.register_namespace("urn:orbitos:opcua-simulator")
    machine = await server.nodes.objects.add_object(idx, "MyObject")
    temperature = await machine.add_variable(idx, "Temperature", 21.0)
    humidity = await machine.add_variable(idx, "Humidity", 45.0)
    relay = await machine.add_variable(idx, "Relay", False)
    await relay.set_writable()
    await machine.add_variable(idx, "Frequency", 50.0)
    await machine.add_variable(idx, "Power", 1200.0)
    print("OPC UA simulator ready on port 4840", flush=True)
    async with server:
        while True:
            t = time.time()
            await temperature.write_value(round(21.0 + 3.0 * math.sin(t / 30), 2))
            await humidity.write_value(round(45.0 + 5.0 * math.cos(t / 40), 2))
            await asyncio.sleep(1)


if __name__ == "__main__":
    asyncio.run(main())
