# Job Agent Backend

A production-ready, containerized **Modular Monolith** backend built with **Java 21**, **Spring Boot**, **MySQL 8**, and **Docker** to manage job discovery, resume parsing, skill matching, and end-to-end application lifecycle tracking.

---

## Why This Project Stands Out (Engineering Impact)

Most portfolio backends stop at basic CRUD. This project was engineered to solve real-world backend challenges:

* **Strict State Machine Enforcement (FSM):** Job application statuses follow a deterministic Finite State Machine (`ApplicationStatus`), blocking illegal state transitions at the domain level before they ever touch the database.
* **Concurrency Control (Optimistic Locking):** Prevents lost updates during concurrent modifications by using JPA optimistic locking paired with a global `409 Conflict` exception handler.
* **Append-Only Audit Trail:** Every status transition is immutably logged in an append-only audit table for complete traceability.
* **Optimized Read Queries:** Dashboard analytics (`GET /api/applications/summary`) use **Spring Data JPA Interface Projections** to aggregate counts directly at the database level without loading unnecessary entity graphs.
* **Zero-Friction Deployment:** Multi-stage Docker build (Maven builder + lightweight Alpine JRE running as a non-root `appuser`) orchestrated with MySQL 8 and persistent Docker volumes.

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language & Framework** | Java 21, Spring Boot, Spring Web, Spring Security |
| **Database & Migrations** | MySQL 8.0, Spring Data JPA, Hibernate, Flyway |
| **Authentication** | Stateless JWT (`jjwt`), BCrypt Password Encoding |
| **Document Processing** | Apache PDFBox, Apache POI (Resume Storage & Parsing) |
| **API Documentation** | OpenAPI 3 / Swagger UI (`springdoc-openapi`) |
| **Testing** | JUnit 5, Mockito |
| **DevOps & Monitoring** | Docker, Docker Compose, Spring Boot Actuator |

---

## Core Modules

1. **Auth & Security:** Stateless JWT registration/login flow with role-based security filter chain.
2. **Profile & Skills:** Candidate profile management and structured skill indexing.
3. **Resume Management:** Secure file upload and persistent storage mapped to Docker volumes (`resume_uploads`).
4. **Jobs & Sources:** Job ingestion and structured job listing management.
5. **Matching Engine:** Automated scoring between candidate skills/resume and job requirements.
6. **Applications & FSM:** Full lifecycle tracking with state transition validation, optimistic locking, and append-only audit logs.
7. **Dashboard Summary:** Aggregated application metrics via JPA projections.

---

## Quick Start (Docker Compose)

Run the entire stack (MySQL 8 + Spring Boot Backend) with a single command.

### Prerequisites
* Docker Desktop (with WSL 2 on Windows)

### 1. Clone the repository
```bash
git clone https://github.com/Md-Altaf1/AI-Agent.git
cd AI-Agent/backend
```

### 2. Start the application stack
```bash
docker compose up --build -d
```

### 3. Verify the running services
* **Swagger UI (Interactive API Docs):** `http://localhost:8080/swagger-ui.html`
* **Actuator Health Check:** `http://localhost:8080/actuator/health`

### 4. Stop the containers
```bash
docker compose down
```
*(Database data and uploaded resumes persist safely in Docker named volumes `mysql_data` and `resume_uploads`.)*

---

## Running Automated Tests

Unit tests cover Finite State Machine transition rules and core `ApplicationService` business logic using JUnit 5 and Mockito:

```bash
./mvnw test
```

---

## Key Engineering Learnings

* **Domain-Driven Guardrails:** Encapsulating state transition rules inside the domain (`ApplicationStatus`) keeps service classes clean and eliminates invalid data states.
* **Schema Evolution with Flyway:** Managing database changes through versioned SQL migrations ensures identical schemas across local development and Docker containers.
* **Container Security & Permissions:** Running containers as a non-root user (`appuser`) and properly configuring Linux directory ownership (`chown`) for mounted volumes prevents production `AccessDeniedException` failures.
* **Multi-Stage Docker Optimization:** Separating Maven dependency resolution and compilation from the final `eclipse-temurin:21-jre-alpine` runtime drastically reduces image size and attack surface.

---

## Upcoming Roadmap

* **Automated Job Fetcher:** Background `@Scheduled` jobs integrating external Job APIs to pull fresh listings automatically.
* **AI Layer (LLM Integration):** Automated resume-to-JD gap analysis and tailored cover letter generation.
* **Chrome Extension Companion:** 1-click form auto-filler connected to this backend via authenticated REST APIs.