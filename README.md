# saas-file-manager

Enterprise multi-tenant SaaS file management microservice built with Spring Boot and MinIO.

This project provides reusable backend infrastructure for SaaS and knowledge-base systems that need secure file upload, object storage, parsing workflows, indexing integration, and tenant-aware data isolation.

![Java](https://img.shields.io/badge/Java-8%2B-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen?logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.8-brightgreen?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue?logo=postgresql)
![MinIO](https://img.shields.io/badge/MinIO-8.0.3-red?logo=minio)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)
[![CI](https://github.com/LjavaU/saas-file-manager/actions/workflows/ci.yml/badge.svg)](https://github.com/LjavaU/saas-file-manager/actions/workflows/ci.yml)

## Project Status

The repository is public and maintained by the primary maintainer. It is currently an early-stage OSS infrastructure project, with the core file-management service, local setup instructions, Docker/Docker Compose/Kubernetes deployment assets, CI, unit tests, and database migration structure already available.

The next maintenance priorities are tracked in [ROADMAP.md](ROADMAP.md): test coverage, API examples, release automation, security hardening, and clearer extension points for LLM-based document classification and entity extraction.

## Features

- File upload: standard backend upload, MinIO presigned direct upload for files up to 200MB, and multipart concurrent upload for larger files.
- File management: folder tree, batch delete, paginated query, file details, ZIP download, share links, and link-based download.
- File parsing: PDF, Word, and Excel parsing workflows with status tracking, re-parse, and re-index support.
- LLM integration: document business-category recognition and automatic entity extraction through service integrations.
- Multi-tenant isolation: tenant-aware data isolation based on `tenant_id`, with tenant context propagated through request headers.
- Microservice integration: OpenFeign clients for LLM, index, DataHub, knowledge, and related internal services.
- Scheduled jobs: parsing-status synchronization with XXL-Job.
- Deployment: Dockerfile, Kubernetes manifest, Flyway migration structure, and local configuration templates.

## Tech Stack

| Category | Technology |
| --- | --- |
| Core framework | Spring Boot 2.7.18, Spring Cloud 2021.0.8 |
| Persistence | MyBatis Plus 3.5.5, PostgreSQL, Flyway |
| Object storage | MinIO 8.0.3 |
| Cache | Spring Data Redis |
| Registry and config | Nacos |
| Service communication | Spring Cloud OpenFeign |
| Scheduled jobs | XXL-Job 2.4.1 |
| File processing | EasyExcel 3.3.4, Apache Commons IO |
| DTO mapping | MapStruct 1.5.5 |
| API documentation | Knife4j 3.0.3 / Swagger |
| Authentication | JWT via jjwt 0.9.1 |
| Utilities | Hutool 5.8.31, Lombok |

## Project Structure

```text
src/main/java/com/example/saasfile/
  openapi/          REST API controllers
  manager/          Business logic and upload strategies
  service/          Service interfaces
  mapper/           MyBatis Plus mappers
  entity/           Database entities
  dto/              Request and response DTOs
  convert/          MapStruct converters
  feign/            OpenFeign external service clients
  integration/      MQ, Excel, and WebSocket integrations
  job/              XXL-Job handlers
  common/           Configuration, enums, exceptions, utilities
  support/          Auth, tenant context, logging, Redis, web support
```

## Quick Start

### Prerequisites

- Java 8+
- Maven 3.6+
- PostgreSQL
- Redis
- MinIO

### Run Locally

1. Clone the repository.

```bash
git clone https://github.com/LjavaU/saas-file-manager.git
cd saas-file-manager
```

2. Configure local services.

Edit `src/main/resources/application-local.yml` and configure database, Redis, and MinIO connection settings.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/saas-file
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 26379

minio:
  endpoint: http://localhost:9000
  access-key: your-access-key
  secret-key: your-secret-key
  bucket: your-bucket
```

3. Start the application.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

4. Open API documentation.

```text
http://localhost:8088/doc.html
```

### Run Dependencies with Docker Compose

Start PostgreSQL, Redis, and MinIO for local development:

```bash
docker compose up -d postgres redis minio minio-init
```

To build and run the application container as well:

```bash
docker compose --profile app up --build
```

The MinIO console is available at:

```text
http://localhost:9001
```

Default local credentials are for development only and should be changed for any shared environment.

### Run Tests

```bash
mvn test
```

## Port Information

| Service | Default Port |
| --- | --- |
| Application | 8088 |
| PostgreSQL | 5432 |
| Redis | 26379 |
| MinIO | 9000 |

## File Upload Strategy

### Presigned Direct Upload

For files up to 200MB, the backend creates a presigned MinIO URL and the frontend uploads the object directly to storage. This reduces backend bandwidth usage and keeps the application service focused on authorization, metadata, and workflow state.

### Multipart Upload

For files larger than 200MB, files are split into 10MB chunks and uploaded concurrently. The backend coordinates upload initialization, part signing, completion, and metadata updates.

## API Endpoints

Base URL: `/open-api/file`

| Method | Path | Description |
| --- | --- | --- |
| POST | `/upload` | Upload file |
| POST | `/batchUpload` | Batch upload |
| DELETE | `/delete/{id}` | Delete file |
| DELETE | `/batchDelete` | Batch delete |
| POST | `/page` | Paginated query |
| POST | `/detail` | Get file details |
| POST | `/update` | Update file information |
| POST | `/createFolder` | Create folder |
| GET | `/tree` | Get file tree |
| GET | `/browse` | Browse files |
| GET | `/statistics` | File statistics |
| GET | `/downloadFilesAsZip` | Download files as ZIP |
| POST | `/share-link` | Create share link |
| GET | `/link-download` | Download via share link |
| GET | `/getFileStatus/{fileId}` | Get parsing status |
| GET | `/reParse/{fileId}` | Re-parse file |
| GET | `/reIndexParse/{fileId}` | Re-index file |

## Deployment

### Docker

```bash
docker build -t saas-file-manager:latest .
docker run -p 8088:8088 saas-file-manager:latest
```

### Kubernetes

```bash
kubectl apply -f k8s/deploy.yml
```

## Database Migration

Database versions are managed with Flyway.

Migration scripts are stored in:

```text
src/main/resources/db/migration/
```

Enable Flyway in configuration:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
```

## Security

This project handles file uploads, temporary download links, tenant isolation, and object-storage access. Security review priorities are documented in [SECURITY.md](SECURITY.md), including upload validation, link expiration, tenant-bound authorization checks, dependency review, and secret handling.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening issues or pull requests.

## License

Licensed under the [Apache License 2.0](LICENSE).
