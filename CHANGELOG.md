# Changelog

All notable changes to this project will be documented in this file.

## v0.1.0 - 2026-05-31

Initial public OSS readiness release for saas-file-manager.

### Added

- Apache-2.0 license, security policy, contribution guide, roadmap, and issue templates.
- GitHub Actions CI running Maven unit tests.
- Initial utility test coverage for `BooleanValidator` and `FileUtils`.
- Public Dockerfile and Docker Compose setup for PostgreSQL, Redis, MinIO, and the optional app container.
- README setup, testing, Docker Compose, Docker build, and project status documentation.

### Changed

- Renamed Maven artifact metadata from `sass-file-manager` to `saas-file-manager`.
- Set the Maven project version to `0.1.0`.
- Replaced local hardcoded configuration values with environment-variable placeholders.

### Verification

- `mvn -B test` passes locally: 5 tests, 0 failures.
