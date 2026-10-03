# Upstream black-box tests (not used by OrbitOS)

This module comes from the upstream project (see [NOTICE](../../NOTICE)). It tests the upstream
microservice images, which OrbitOS does not build or support.

OrbitOS tests:

- Unit tests of OrbitOS code: `docker build -f orbitos/docker/Dockerfile --target test .`
- End-to-end test of a fresh install: `bash orbitos/scripts/smoke-test.sh`

See the [developer guide](../../orbitos/docs/en/developer-guide.md).

The module stays in the tree only so upstream patches merge cleanly.
