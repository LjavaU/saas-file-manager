

# saas-file-manager

**Enterprise Multi-Tenant SaaS File Management Microservice**

Built with Spring Boot + MinIO, supporting large file multipart upload,
file parsing, knowledge base integration, and multi-tenant isolation.

![Java](https://img.shields.io/badge/Java-11-orange?logo=openjdk)
![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen?logo=springboot)
![Spring
Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.8-brightgreen?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue?logo=postgresql)
![MinIO](https://img.shields.io/badge/MinIO-8.0.3-red?logo=minio)

------------------------------------------------------------------------

## Features

-   **File Upload** --- Supports standard upload, MinIO presigned direct
    upload (≤200MB), and multipart concurrent upload (\>200MB)
-   **File Management** --- Folder tree structure, batch deletion, ZIP
    download, and share links
-   **File Parse** --- Multi-format parsing (PDF/Word/Excel), state tracking, support for reparsing and re-indexing, 
     and use LLMs for document business category recognition and automatic entity extraction
-   **Multi-Tenant Isolation** --- Data isolation based on `tenant_id`,
    tenant context automatically propagated via HTTP headers
-   **Microservice Integration** --- Integrates with LLM, Index,
    DataHub, Knowledge and other internal services via OpenFeign
-   **Scheduled Jobs** --- File parsing status synchronization based on
    XXL-Job scheduling

------------------------------------------------------------------------

## Tech Stack

Category                     Technology

---------------------------- --------------------------------------------

Core Framework               Spring Boot 2.7.18 / Spring Cloud 2021.0.8
Persistence Layer            MyBatis Plus 3.5.5 / PostgreSQL / Flyway
Object Storage               MinIO 8.0.3
Cache                        Spring Data Redis
Service Registry & Config    Nacos
Microservice Communication   Spring Cloud OpenFeign
Scheduled Jobs               XXL-Job 2.4.1
File Processing              EasyExcel 3.3.4 / Apache Commons IO
DTO Mapping                  MapStruct 1.5.5
API Documentation            Knife4j 3.0.3 (Swagger)
Authentication               JWT (jjwt 0.9.1)
Utility Libraries            Hutool 5.8.31 / Lombok

------------------------------------------------------------------------

## Project Structure

    src/main/java/com/example/saasfile/
    ├── openapi/          # REST API controllers (FileController, FileUploadController)
    ├── manager/          # Business logic layer (file operations, direct upload strategies)
    ├── service/          # Service interface layer
    ├── mapper/           # MyBatis Plus Mapper
    ├── entity/           # Database entities (FileObject, FileRecommendation)
    ├── dto/              # Request/Response DTOs
    ├── convert/          # MapStruct converters
    ├── feign/            # OpenFeign external service clients
    ├── integration/
    │   ├── mq/           # Message queue integration
    │   ├── excel/        # EasyExcel listeners
    │   └── ws/           # WebSocket support
    ├── job/              # XXL-Job scheduled task handlers
    ├── common/
    │   ├── config/       # Configurations (thread pools, etc.)
    │   ├── enums/        # Business enums (FileStatus, FileCategory, etc.)
    │   ├── exception/    # Custom exceptions
    │   └── utils/        # Utility classes (MinIO, JWT, file utilities, etc.)
    └── support/          # Cross-cutting concerns (auth, multi-tenant, logging, Redis, WebSocket, etc.)

------------------------------------------------------------------------

## Quick Start

### Prerequisites

-   Java 11+
-   Maven 3.6+
-   PostgreSQL
-   Redis
-   MinIO

### Run Locally

**1. Clone the repository**

``` bash
git clone https://github.com/your-org/saas-file-manager.git
cd saas-file-manager
```

**2. Modify local configuration**

Edit `src/main/resources/application-local.yml` and configure database,
Redis, and MinIO connection settings:

``` yaml
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

**3. Start the application**

``` bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**4. Access API documentation**

    http://localhost:8088/doc.html

------------------------------------------------------------------------

### Port Information

Service       Default Port

------------- --------------

Application   8088
PostgreSQL    5432
Redis         26379
MinIO         9000

------------------------------------------------------------------------

## File Upload Strategy

### Presigned Direct Upload (Files ≤ 200MB)

The frontend uploads files directly to MinIO via a presigned URL,
bypassing the backend to reduce bandwidth pressure.

### Multipart Upload (Files \> 200MB)

Files are split into 10MB chunks and uploaded concurrently. The backend
merges them after all chunks are uploaded.

------------------------------------------------------------------------

## API Endpoints

Base URL: `/open-api/file`

### File Management

Method   Path                      Description

-------- ------------------------- -------------------------

POST     /upload                   Upload file
POST     /batchUpload              Batch upload
DELETE   /delete/{id}              Delete file
DELETE   /batchDelete              Batch delete
POST     /page                     Paginated query
POST     /detail                   Get file details
POST     /update                   Update file information
POST     /createFolder             Create folder
GET      /tree                     Get file tree
GET      /browse                   Browse files
GET      /statistics               File statistics
GET      /downloadFilesAsZip       Download files as ZIP
POST     /share-link               Create share link
GET      /link-download            Download via share link
GET      /getFileStatus/{fileId}   Get parsing status
GET      /reParse/{fileId}         Re-parse file
GET      /reIndexParse/{fileId}    Re-index

------------------------------------------------------------------------

## Deployment

### Docker

``` bash
docker build -f Dockerfile.x64 -t saas-file-manager:latest .
docker run -p 8088:8088 saas-file-manager:latest
```

### Kubernetes

``` bash
kubectl apply -f k8s/deploy.yml
```

------------------------------------------------------------------------

## Database Migration

Database versions are managed using Flyway.

Migration scripts are located in:

    src/main/resources/db/migration/

Enable Flyway:

``` yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
```

