#!/bin/sh
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
#
# Starts OrbitOS.
#   (no argument)  install the database on first start if it is empty, then run the server
#   upgrade        upgrade the database after a new version (take a backup first), then exit
#   install        install the database only, then exit
#   verify-audit   check that the audit log was not changed (exit code 3 if it was)
#
# The database password is read from ORBITOS_DB_PASSWORD_FILE when SPRING_DATASOURCE_PASSWORD is not set.
set -e

JAR=/usr/share/orbitos/bin/orbitos.jar
DATA_DIR=/usr/share/orbitos/data
LOG_CONFIG=${LOG_CONFIG:-/usr/share/orbitos/conf/logback.xml}
JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx1g -XX:+UseG1GC}"

if [ -z "$SPRING_DATASOURCE_PASSWORD" ] && [ -n "$ORBITOS_DB_PASSWORD_FILE" ]; then
    SPRING_DATASOURCE_PASSWORD="$(cat "$ORBITOS_DB_PASSWORD_FILE")"
    export SPRING_DATASOURCE_PASSWORD
fi

# Installer and upgrader (no audit-log sink there: they write no audit entries).
java_main() {
    main_class="$1"; shift
    # shellcheck disable=SC2086
    java -cp "$JAR" $JAVA_OPTS \
        -Dloader.main="$main_class" \
        -Daudit-log.sink.type=none \
        -Dinstall.data_dir="$DATA_DIR" \
        -Dlogging.config="$LOG_CONFIG" \
        "$@" \
        org.springframework.boot.loader.launch.PropertiesLauncher
}

# Encrypted MQTT (port 8883) with MQTT_SSL_ENABLED=true: unless you configured your own certificate
# (MQTT_SSL_CREDENTIALS_TYPE), a certificate for ORBITOS_DOMAIN is created once, offline, and kept in
# /var/lib/orbitos/tls. Devices trust mqtt-server.crt from that folder.
prepare_mqtt_tls() {
    if [ "$MQTT_SSL_ENABLED" != "true" ] || [ -n "$MQTT_SSL_CREDENTIALS_TYPE" ]; then
        return
    fi
    tls=/var/lib/orbitos/tls
    domain="${ORBITOS_DOMAIN:-localhost}"
    mkdir -p "$tls"
    if [ ! -s "$tls/mqtt.p12" ]; then
        head -c 48 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 32 > "$tls/mqtt.password"
        chmod 600 "$tls/mqtt.password"
        case "$domain" in
            *[!0-9.]*) san="dns:$domain" ;;
            *) san="ip:$domain" ;;
        esac
        keytool -genkeypair -alias mqtt -keyalg EC -groupname secp256r1 -sigalg SHA256withECDSA -validity 825 \
            -dname "CN=$domain" -ext "SAN=$san,dns:localhost,dns:orbitos,ip:127.0.0.1" \
            -storetype PKCS12 -keystore "$tls/mqtt.p12" \
            -storepass "$(cat "$tls/mqtt.password")" -keypass "$(cat "$tls/mqtt.password")" > /dev/null 2>&1
        keytool -exportcert -rfc -alias mqtt -keystore "$tls/mqtt.p12" \
            -storepass "$(cat "$tls/mqtt.password")" -file "$tls/mqtt-server.crt" > /dev/null 2>&1
        echo "OrbitOS: MQTT TLS certificate created for $domain (devices trust $tls/mqtt-server.crt)"
    fi
    MQTT_SSL_CREDENTIALS_TYPE=KEYSTORE
    MQTT_SSL_KEY_STORE_TYPE=PKCS12
    MQTT_SSL_KEY_STORE="$tls/mqtt.p12"
    MQTT_SSL_KEY_ALIAS=mqtt
    MQTT_SSL_KEY_STORE_PASSWORD="$(cat "$tls/mqtt.password")"
    MQTT_SSL_KEY_PASSWORD="$MQTT_SSL_KEY_STORE_PASSWORD"
    export MQTT_SSL_CREDENTIALS_TYPE MQTT_SSL_KEY_STORE_TYPE MQTT_SSL_KEY_STORE MQTT_SSL_KEY_ALIAS \
        MQTT_SSL_KEY_STORE_PASSWORD MQTT_SSL_KEY_PASSWORD
}

install_database() {
    echo "OrbitOS: installing the database (first start, about 1-2 minutes) ..."
    java_main org.thingsboard.server.ThingsboardInstallApplication \
        -Dinstall.load_demo=false -Dinstall.upgrade=false
}

case "${1:-run}" in
    install)
        install_database
        ;;
    upgrade)
        echo "OrbitOS: upgrading the database ..."
        java_main org.thingsboard.server.ThingsboardInstallApplication -Dinstall.upgrade=true \
            ${FROM_VERSION:+-Dinstall.upgrade.from_version="$FROM_VERSION"}
        ;;
    verify-audit)
        # Integrity check of the audit log: prints JSON; exit code 0 = intact, 3 = broken.
        exec java -cp "$JAR" -Dloader.main=org.thingsboard.server.orbitos.audit.AuditChainCheck \
            org.springframework.boot.loader.launch.PropertiesLauncher
        ;;
    run)
        set +e
        java -cp "$JAR" -Dloader.main=org.thingsboard.server.orbitos.install.DatabaseState \
            org.springframework.boot.loader.launch.PropertiesLauncher
        state=$?
        set -e
        if [ "$state" = "10" ]; then
            install_database
        elif [ "$state" != "0" ]; then
            echo "OrbitOS: database problem (see the message above), stopping." >&2
            exit 1
        fi
        prepare_mqtt_tls
        echo "OrbitOS: starting the server ..."
        # shellcheck disable=SC2086
        exec java -cp "$JAR" $JAVA_OPTS \
            -Dloader.main=org.thingsboard.server.ThingsboardServerApplication \
            -Dinstall.data_dir="$DATA_DIR" \
            -Dlogging.config="$LOG_CONFIG" \
            org.springframework.boot.loader.launch.PropertiesLauncher
        ;;
    *)
        echo "Usage: orbitos-entrypoint [run|install|upgrade|verify-audit]" >&2
        exit 2
        ;;
esac
