# Contributing

Thanks for your interest in improving saas-file-manager. This project is an early-stage OSS service, so the most valuable contributions are reproducible bug reports, small focused fixes, tests, documentation, and security hardening around file upload, sharing, tenant isolation, and object-storage workflows.

## Ways to Contribute

- Report bugs with clear reproduction steps, logs, request examples, and expected behavior.
- Propose features that fit the scope of a reusable SaaS file-management microservice.
- Improve documentation for local setup, deployment, API usage, and integration examples.
- Add tests for upload flows, parsing state transitions, tenant isolation, and permission checks.
- Review dependencies and help address security or compatibility issues.

## Development Setup

Prerequisites:

- Java 11+
- Maven 3.6+
- PostgreSQL
- Redis
- MinIO

Run locally:

```bash
git clone https://github.com/LjavaU/saas-file-manager.git
cd saas-file-manager
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Before running the service, update `src/main/resources/application-local.yml` with local PostgreSQL, Redis, and MinIO settings.

## Pull Request Guidelines

- Keep changes focused and explain the motivation clearly.
- Include tests or manual verification notes when behavior changes.
- Update README or API documentation when endpoints, configuration, or workflows change.
- Avoid committing secrets, credentials, local paths, or generated build outputs.
- For security-sensitive changes, describe the threat model and the checks added.

## Code Style

- Follow the existing Spring Boot package layout.
- Keep controller logic thin and put business logic in manager/service layers.
- Prefer explicit DTOs for API inputs and outputs.
- Preserve tenant-aware behavior for data access and service calls.
- Keep external-service integrations behind Feign clients or clearly scoped adapters.

## Security Reports

Do not disclose vulnerabilities in public issues. Follow the process in [SECURITY.md](SECURITY.md).
