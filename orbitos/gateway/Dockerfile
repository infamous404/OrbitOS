# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
#
# OrbitOS IoT Gateway add-on: the upstream IoT Gateway 3.8.5 (Apache-2.0), made offline and private.
#   - no update checks (the upstream image sends version, OS and an instance ID to the vendor every hour)
#   - no public DNS server added to /etc/resolv.conf at start
#   - Modbus and BACnet libraries installed now, so those connectors never download anything at run time
#   - a small Modbus simulator to try it without hardware
# Built by:  docker compose --profile gateway build gateway
FROM thingsboard/tb-gateway:3.8.5@sha256:547ba1c0f7613f7eca1c939f182c3d85a9465d939cf4962a4d6085a87e32f25f

# Permissive licences only: pymodbus (BSD-3-Clause), pyserial and pyserial-asyncio (BSD-3-Clause), bacpypes3 (MIT).
# The OPC UA library (asyncua) is LGPL-3.0, so it is not bundled: the OPC UA connector installs it on first use.
RUN pip install --no-cache-dir "pymodbus==3.9.2" pyserial pyserial-asyncio bacpypes3

COPY orbitos/gateway/patch_gateway.py /tmp/patch_gateway.py
RUN python /tmp/patch_gateway.py && rm /tmp/patch_gateway.py

COPY --chmod=755 orbitos/gateway/start.sh /start-gateway.sh
COPY orbitos/gateway/modbus_simulator.py /orbitos/modbus_simulator.py

LABEL org.opencontainers.image.title="OrbitOS IoT Gateway" \
      org.opencontainers.image.licenses="Apache-2.0" \
      org.opencontainers.image.base.name="thingsboard/tb-gateway:3.8.5"
