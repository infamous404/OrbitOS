# Upstream single-container images (not used by OrbitOS)

This module comes from the upstream project (see [NOTICE](../../NOTICE)) and builds its own
single-container images. OrbitOS does not publish or support these images.

The OrbitOS image is built from [orbitos/docker/Dockerfile](../../orbitos/docker/Dockerfile):

```bash
docker compose build orbitos
docker compose up -d
```

See [orbitos/docs/en/install.md](../../orbitos/docs/en/install.md) and the
[developer guide](../../orbitos/docs/en/developer-guide.md).

The module stays in the tree only so upstream patches merge cleanly.
