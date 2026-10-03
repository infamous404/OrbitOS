#!/bin/sh
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
# Replaces the whole database with a backup. Stop the server first:
#   docker compose stop orbitos
#   docker compose run --rm restore orbitos-20261002-101500.dump
#   docker compose start orbitos
set -e
if [ -z "$1" ]; then
    echo "Usage: docker compose run --rm restore <file name from the backups folder>" >&2
    ls /backups 2>/dev/null | grep '\.dump$' >&2 || true
    exit 2
fi
file="/backups/$(basename "$1")"
[ -f "$file" ] || { echo "Not found / introuvable : $file" >&2; exit 1; }

PGPASSWORD="$(cat /run/orbitos-secrets/db_password)"
export PGPASSWORD
psql_admin() { psql -h postgres -U orbitos -d postgres -v ON_ERROR_STOP=1 -tA -c "$1"; }

others=$(psql_admin "SELECT count(*) FROM pg_stat_activity WHERE datname = 'orbitos' AND pid <> pg_backend_pid()")
if [ "$others" != "0" ]; then
    echo "The server is still using the database. Run 'docker compose stop orbitos' first." >&2
    echo "Le serveur utilise encore la base de données. Lancez d'abord 'docker compose stop orbitos'." >&2
    exit 1
fi

echo "Restoring $file ..."
psql_admin "DROP DATABASE IF EXISTS orbitos"
psql_admin "CREATE DATABASE orbitos OWNER orbitos"
pg_restore -h postgres -U orbitos -d orbitos --no-owner --exit-on-error "$file"
echo "Restore finished. Start the server: docker compose start orbitos"
echo "Restauration terminée. Démarrez le serveur : docker compose start orbitos"
