# File Repository Service

Modern, multi-tenant file storage and search service built with Spring Boot 3, PostgreSQL, and Liquibase. Files are stored on the local filesystem, metadata in Postgres. Includes tenant configs, file upload/download/search, and text-embedding utilities for semantic search.

<p align="

## ✨ Features
- Multi-tenant storage under `./storage/{tenantCode}/{yyyy_MM}/`
- Open APIs (no auth yet) for tenants and files
- File metadata in PostgreSQL; Liquibase manages schema
- Upload (single/zip), download, delete, search (tag/name)
- Embeddings generation (per-page) + semantic search (Java cosine similarity)
- Centralized logging via Log4j2 (console + rolling file to `logs/application.log`)

## 🧱 Tech Stack
- Spring Boot 3 (Java 17)
- Spring Data JPA, Hibernate
- PostgreSQL + Liquibase (XML changelogs)
- Apache Tika, PDFBox
- Log4j2 (RollingFile)

## 📦 Endpoints (high level)
- Health: `GET /health`
- Tenants:
  - `POST /v1/tenants/config` (create)
  - `GET /v1/tenants/{tenantId}/config` (get)
  - `POST /v1/tenants/{tenantId}/config` (update)
  - `DELETE /v1/tenants/{tenantId}` (delete)
  - `GET /v1/tenants` (list)
- Files (`/v1/tenants/{tenantId}`):
  - `POST /upload` (multipart)
  - `GET /files` (list)
  - `GET /files/{fileId}` (details)
  - `DELETE /files/{fileId}` (delete)
  - `GET /download/{fileId}` (download)
  - `POST /files/search` (filter search)
- Embeddings (`/v1/tenants/{tenantId}`):
  - `POST /embeddings/{fileId}` (generate from PDF)
  - `GET /embeddings/{fileId}` (list stored)
  - `POST /embeddings/search/{fileId}` (semantic search)

## 🚀 Getting Started

### Prerequisites
- Java 17
- PostgreSQL running and accessible
- Set environment variables or use defaults

### Environment Variables
```
FILE_REPO_DB_NAME=filerepo_db
FILE_REPO_DB_USERNAME=postgres
FILE_REPO_DB_PASSWORD=yourpassword
FILE_REPO_DB_HOST=localhost
FILE_REPO_DB_PORT=5432
FILE_REPO_STORAGE_BASE=./storage
FILE_REPO_TEMP_BASE=./temp
FILE_REPO_PORT=8080
FILE_REPO_HOST=0.0.0.0
FILE_REPO_LOG_LEVEL=INFO
GEMINI_API_KEY=<optional if embeddings used>
```

### Run
```
# from project root
mvn clean package
java -jar target/file-repository-service-0.0.1-SNAPSHOT.jar
```
App will start on `http://localhost:8080`.

## 🗄️ Database & Migrations
- Liquibase XML changelogs live under `src/main/resources/db/changelog/`
- Master changelog: `db.changelog-master.xml`
- No manual DDL needed; Liquibase applies on startup

## 🧠 Embeddings Strategy
- Embeddings are stored as a JSON string (e.g., `[0.12, 0.34, ...]`) for maximum Hibernate compatibility while preserving semantic search ability.
- Cosine similarity is computed in Java (no DB driver issues).

## 📜 Logging
- Log4j2 config at `src/main/resources/log4j2.xml`
- Console + rolling file appender. Logs written to `logs/application.log`
- Controllers log requests and success; services and utils log INFO/DEBUG/WARN/ERROR appropriately

## 🧪 Quick Test (curl)
```
# Health
curl -s http://localhost:8080/health

# Create tenant
curl -s -X POST http://localhost:8080/v1/tenants/config \
  -H 'Content-Type: application/json' \
  -d '{"maxFileSizeKBytes":2048, "allowedExtensions":[".pdf"]}'
```

## 🙌 Contributing
Issues and PRs welcome! Please open an issue to discuss big changes.

## 📌 GitHub Topics (add in repo settings for discoverability)
Recommended topics: `spring-boot` `file-storage` `postgresql` `liquibase` `java-17` `log4j2` `embeddings` `semantic-search` `pdf` `tika` `pdfbox`

## ⭐ Support
If you find this useful:
- Star the repo (it really helps!)
- Share feedback via issues
- Fork and build your own features
