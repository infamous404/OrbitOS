#!/bin/sh
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
# Writes a full database backup to the backups folder. Run with: docker compose run --rm backup
set -e
PGPASSWORD="$(cat /run/orbitos-secrets/db_password)"
export PGPASSWORD
file="orbitos-$(date +%Y%m%d-%H%M%S).dump"
pg_dump -h postgres -U orbitos -d orbitos --format=custom --file="/backups/$file"
# On Linux the container runs as root: give the file to the owner of the backups folder (you),
# readable by that owner only. Harmless on Windows and macOS.
chown "$(stat -c '%u:%g' /backups)" "/backups/$file" 2>/dev/null || true
chmod 600 "/backups/$file"
echo "Backup written: backups/$file ($(du -h "/backups/$file" | cut -f1))"
echo "Sauvegarde écrite : backups/$file"
