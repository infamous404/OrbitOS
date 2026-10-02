#!/bin/sh
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
#
# Starts OrbitOS. Modes, chosen by environment variables:
#   ORBITOS_INSTALL=true                 create the database schema and system data, then exit
#   ORBITOS_UPGRADE=true FROM_VERSION=x  upgrade the database schema from version x, then exit
#   (neither)                            run the server
set -e

JAR=/usr/share/orbitos/bin/orbitos.jar
DATA_DIR=/usr/share/orbitos/data
LOG_CONFIG=${LOG_CONFIG:-/usr/share/orbitos/conf/logback.xml}
JAVA_OPTS="${JAVA_OPTS:--XX:+UseG1GC -XX:MaxRAMPercentage=75}"

launch() {
    main_class="$1"; shift
    # shellcheck disable=SC2086
    exec java -cp "$JAR" $JAVA_OPTS \
        -Dloader.main="$main_class" \
        -Dinstall.data_dir="$DATA_DIR" \
        -Dlogging.config="$LOG_CONFIG" \
        "$@" \
        org.springframework.boot.loader.launch.PropertiesLauncher
}

if [ "$ORBITOS_INSTALL" = "true" ]; then
    echo "OrbitOS: installing database schema and system data ..."
    launch org.thingsboard.server.ThingsboardInstallApplication \
        -Dinstall.load_demo=false -Dinstall.upgrade=false
elif [ "$ORBITOS_UPGRADE" = "true" ]; then
    echo "OrbitOS: upgrading database from version ${FROM_VERSION:?FROM_VERSION is required} ..."
    launch org.thingsboard.server.ThingsboardInstallApplication \
        -Dinstall.upgrade=true -Dinstall.upgrade.from_version="$FROM_VERSION"
else
    echo "OrbitOS: starting server ..."
    launch org.thingsboard.server.ThingsboardServerApplication
fi
