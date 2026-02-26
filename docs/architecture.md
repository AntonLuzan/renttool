# System Architecture

## High Level Overview

```
Client
   ↓
Nginx (reverse proxy)
   ↓
Spring Boot Application
   ↓
PostgreSQL Database
```

---

## Containers

### Application Container

* Spring Boot application
* Built via Gradle inside Docker
* Runs as executable JAR

### Database Container

* PostgreSQL
* Separate service for isolation
* Connected via Docker network

---

## Deployment Flow

1. Source code is copied into Docker image
2. Gradle builds executable bootJar
3. Containers start via docker-compose
4. Application connects to database via service name
5. System becomes fully operational

---

## Engineering Decisions

### Why Docker?

* Consistent environment
* Easy deployment
* No local dependency issues

### Why Separate Database Container?

* Production-like architecture
* Service isolation
* Independent scaling possibility
