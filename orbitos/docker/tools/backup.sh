#!/bin/sh
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
# Writes a full database backup to the backups folder. Run with: docker compose run --rm backup
set -e
PGPASSWORD="$(cat /run/orbitos-secrets/db_password)"
export PGPASSWORD
file="orbitos-$(date +%Y%m%d-%H%M%S).dump"
pg_dump -h postgres -U orbitos -d orbitos --format=custom --file="/backups/$file"
echo "Backup written: backups/$file ($(du -h "/backups/$file" | cut -f1))"
echo "Sauvegarde écrite : backups/$file"
