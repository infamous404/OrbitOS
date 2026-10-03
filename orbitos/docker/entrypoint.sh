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
