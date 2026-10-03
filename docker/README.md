# Upstream microservice files (not used by OrbitOS)

This folder comes unchanged from the upstream project (see [NOTICE](../NOTICE)). It holds scripts
and Compose files for a multi-container "microservices" deployment (Kafka, Valkey, Cassandra,
separate transport services).

**OrbitOS does not use or support this setup.** OrbitOS runs as one server plus PostgreSQL,
started from the repository root:

```bash
docker compose up -d
```

- Install and first start: [orbitos/docs/en/install.md](../orbitos/docs/en/install.md)
- Building and testing from source: [orbitos/docs/en/developer-guide.md](../orbitos/docs/en/developer-guide.md)

These files are kept only so upstream security patches merge cleanly. Do not use them as they are:
they create the upstream default administrator account and password.
