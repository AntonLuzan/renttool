# Complex Engineering Tasks

This document describes the most complex engineering tasks solved in the project.

---

## 🚀 Task 1 — Full Containerized Deployment

### Problem

The application could not be reliably started across different environments (OS differences, local setup issues).

### Solution

* Containerized Spring Boot application using Docker
* Added PostgreSQL as a separate container
* Configured Docker network between services
* Unified startup using docker-compose

### Artifacts

* Dockerfile
* docker-compose.yml
* .dockerignore

### Result

The entire system can be started with a single command:

```bash
docker compose up --build
```

The environment is reproducible and isolated.

---

## 🚀 Task 2 — Database Connectivity Inside Containers

### Problem

Database connection failed because `localhost` does not work inside Docker containers.

### Solution

* Switched datasource host to Docker service name
* Configured container dependency order
* Unified environment configuration

### Artifacts

* application.properties
* docker-compose.yml

### Result

Stable application startup with automatic DB connection.

---

## 🚀 Task 3 — Automated Build Process via Gradle

### Problem

Manual application build created inconsistent results between environments.

### Solution

* Gradle build executed inside Docker
* Automatic bootJar generation
* Unified Java runtime environment

### Artifacts

* Dockerfile
* Gradle Wrapper (gradlew)

### Result

Build process became fully reproducible and independent from host system.
