#!/bin/sh
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
# Starts the IoT Gateway. Unlike the upstream script, it does not add a public DNS server, and it
# never overwrites configuration files that already exist (your edits in gateway-config/ are kept).
set -e
CONF=/thingsboard_gateway/config
mkdir -p "$CONF" /thingsboard_gateway/extensions
cp -rn /default-config/config/. "$CONF"/ 2>/dev/null || true
cp -rn /default-config/extensions/. /thingsboard_gateway/extensions/ 2>/dev/null || true

if [ -z "$accessToken" ]; then
    echo "IoT Gateway: set ORBITOS_GATEWAY_TOKEN in .env (token of a device marked 'Is gateway' in OrbitOS)." >&2
    exit 1
fi
exec python /thingsboard_gateway/tb_gateway.py
