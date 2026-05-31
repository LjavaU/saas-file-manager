# Roadmap

This roadmap keeps the public maintenance direction visible for contributors and reviewers. Priorities may change as issues, security reviews, and user feedback come in.

## v0.1 - OSS Readiness

- Publish open-source license and contribution guidelines.
- Document security reporting and review priorities.
- Improve README structure, setup instructions, and API endpoint overview.
- Add issue templates for bug reports and feature requests.
- Add initial CI, unit tests, and Docker Compose local dependencies.

## v0.2 - Reliability and Tests

- Add unit tests for upload initialization, multipart signing, completion, and metadata updates.
- Add service-level tests for folder operations, batch deletion, share links, and tenant-aware queries.
- Add integration-test documentation for PostgreSQL, Redis, and MinIO.
- Expand CI to include Docker image build validation.

## v0.3 - Security Hardening

- Review authorization checks for each file operation.
- Add tests for tenant isolation and unauthorized cross-tenant access attempts.
- Harden share-link expiration, revocation, and download validation.
- Review file-name, object-key, ZIP download, and parser safety behavior.
- Add dependency scanning and regular CVE triage.

## v0.4 - LLM and Knowledge Workflows

- Document LLM classification and entity-extraction extension points.
- Add reproducible sample flows for document parsing and re-indexing.
- Add example payloads for LLM, index, DataHub, and knowledge-service integrations.
- Improve observability for parsing status, retry behavior, and failure analysis.

## v0.5 - Release and Deployment Quality

- Publish tagged releases with release notes.
- Improve Kubernetes manifests and configuration examples.
- Add production deployment checklist.
