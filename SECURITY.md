# Security Policy

saas-file-manager handles file uploads, object-storage access, download links, parsing workflows, JWT-based authentication, and tenant-aware data. Security issues in these areas can affect confidentiality, integrity, and availability.

## Supported Versions

The project is early-stage and the `main` branch is the supported development line. Security fixes should target `main` unless a release branch is introduced later.

## Reporting a Vulnerability

Please do not open a public issue with exploit details, credentials, private URLs, sample secrets, or sensitive tenant data.

Preferred reporting path:

1. Use GitHub private vulnerability reporting or a GitHub security advisory if available for this repository.
2. If that is not available, open a minimal public issue asking for a private contact channel, without vulnerability details.
3. Include affected component, expected impact, reproduction outline, and any safe proof-of-concept material once a private channel is established.

The maintainer will acknowledge reports as soon as practical, validate the issue, and coordinate a fix or mitigation in the public repository when safe to disclose.

## Security Review Priorities

High-priority areas for review:

- Tenant isolation and authorization checks for every file operation.
- Presigned upload and download URL scope, expiration, and permission checks.
- Multipart upload completion, part validation, and object-key handling.
- Share-link creation, revocation, expiration, and access control.
- File-type validation, parser safety, and handling of untrusted document content.
- ZIP download behavior and path traversal protections.
- JWT validation, secret configuration, and token propagation.
- Dependency CVEs in Spring Boot, MinIO, JWT, file parsers, and utility libraries.
- Logging and error handling that may expose tenant data or credentials.

## Secret Handling

Do not commit real credentials, tokens, API keys, MinIO secrets, database passwords, or production endpoints. Use local configuration overrides, environment variables, or secret-management systems for deployments.
