# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
#
# Builds the OrbitOS single-node image from source.
# Run from the repository root:
#   docker compose build orbitos      (or: docker build -f orbitos/docker/Dockerfile -t localhost/orbitos:dev .)

# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
RUN apt-get update \
 && apt-get install -y --no-install-recommends python3 python3-yaml \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /src
COPY . .
# Apply orbitos/branding/branding.yml (colors, built-in logo, names) before compiling.
RUN python3 orbitos/scripts/rebrand.py
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/src/ui-ngx/node_modules \
    --mount=type=cache,target=/usr/local/share/.cache/yarn \
    MAVEN_OPTS="-Xmx2g" NODE_OPTIONS="--max_old_space_size=6144" \
    mvn -B -T 1C install -DskipTests -Dlicense.skip=true -Dmaven.gitcommitid.skip=true \
        -Dpkg.skip.deb=true -Dpkg.skip.rpm=true -Dpkg.skip.zip=true \
        -pl application -am
RUN mkdir -p /out/data \
 && cp application/target/thingsboard-*-boot.jar /out/orbitos.jar \
 && cp -r application/src/main/data/. /out/data/ \
 && cp -r dao/src/main/resources/sql dao/src/main/resources/cassandra /out/data/

# ---------- test stage: docker build --target test -f orbitos/docker/Dockerfile . ----------
# Runs the OrbitOS unit tests (the build stage already compiled them).
FROM build AS test
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl application surefire:test -Dmaven.gitcommitid.skip=true \
        -Dtest='org.thingsboard.server.orbitos.**.*Test' -Dsurefire.failIfNoSpecifiedTests=false

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre-noble
RUN apt-get update \
 && apt-get install -y --no-install-recommends libharfbuzz0b fontconfig fonts-dejavu-core \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --uid 799 --home-dir /var/lib/orbitos --shell /usr/sbin/nologin orbitos \
 && mkdir -p /usr/share/orbitos/bin /usr/share/orbitos/conf /var/log/orbitos /var/lib/orbitos /etc/orbitos/branding \
 && chown -R orbitos:orbitos /var/log/orbitos /var/lib/orbitos
COPY --from=build /out/orbitos.jar /usr/share/orbitos/bin/orbitos.jar
COPY --from=build /out/data /usr/share/orbitos/data
COPY orbitos/docker/logback.xml /usr/share/orbitos/conf/logback.xml
COPY orbitos/branding/ /etc/orbitos/branding/
COPY orbitos/docs/en/ /usr/share/orbitos/docs/en/
COPY orbitos/docs/fr/ /usr/share/orbitos/docs/fr/
COPY --chmod=755 orbitos/docker/entrypoint.sh /usr/bin/orbitos-entrypoint
USER orbitos
# Local state (calculated-field state store). Mount a volume here to keep it across restarts.
VOLUME /var/lib/orbitos
# 8080 web UI and REST API, 1883 MQTT, 5683/udp CoAP
EXPOSE 8080 1883 5683/udp
ENTRYPOINT ["/usr/bin/orbitos-entrypoint"]
