# Job Agent Backend

A modular, production-ready Spring Boot backend for a personal job application assistant. It manages user profiles, resume parsing and versioning, deterministic job deduplication, two-stage rule-based job matching, and a full application tracking workflow backed by an append-only audit log.

---

## Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x (Web, Data JPA, Security, Validation)
* **Database & Migrations:** MySQL 8 (InnoDB), Flyway (`ddl-auto: validate`)
* **Authentication:** Stateless JWT (Access & Refresh token flow)
* **Document Parsing:** Apache PDFBox (PDF), Apache POI `XWPFWordExtractor` (DOCX)
* **ORM Mapping:** Hibernate 6 with native JSON column mapping (`@JdbcTypeCode(SqlTypes.JSON)`)
* **Build Tool:** Maven

---

## Architecture

The project follows a **Modular Monolith** structure organised by business domain under `com.example.jobagent`:

```text
com.example.jobagent
├── auth          # User registration, login, JWT generation & security filter
├── profile       # User profile, declared skills, and job search preferences
├── resume        # Multipart file storage, SHA-256 deduplication, PDF/DOCX parsing
├── job           # Job sources, manual import, deterministic deduplication & search
├── matching      # Two-stage matching engine (hard filters + weighted skill scoring)
├── application   # Application lifecycle, form answers, state machine & audit trail
└── common.config # Global exception handling & shared security/web configuration
```

---

## Key Engineering Highlights

* **Deterministic Deduplication & Change Detection:** Jobs are deduplicated using a SHA-256 `dedupeKey` (normalized company, title, and URL) and tracked for description updates via `contentHash`. Resumes use SHA-256 file hashing to prevent duplicate uploads.
* **Secure File Handling:** Resume uploads validate file types and enforce path traversal protection before writing to local storage.
* **Two-Stage Matching Engine:**
  1. **Hard Rule Filtering:** Evaluates excluded title keywords, experience limits, preferred locations, and employment types.
  2. **Weighted Skill Scoring:** Scores required skills (weight `2`) and optional skills (weight `1`) against both declared user skills and extracted resume text, caching results per `(resume_version_id, job_id)` and `content_hash`.
* **Strict Status State Machine:** Application status transitions are enforced directly in the `ApplicationStatus` domain enum to prevent invalid jumps (e.g., `DISCOVERED` directly to `INTERVIEW_SCHEDULED`).
* **Concurrency Control:** Uses JPA `@Version` optimistic locking on `applications`, returning `409 Conflict` via `GlobalExceptionHandler` on concurrent write collisions.
* **Append-Only Audit Log:** Every application creation, status change, and answer edit is logged to `application_events` within the same `@Transactional` boundary. Immutability is enforced at both the JPA layer (`@Immutable`) and the database layer (MySQL `BEFORE UPDATE` and `BEFORE DELETE` triggers).

---

## API Overview

All endpoints except `/api/auth/**` require an `Authorization: Bearer <accessToken>` header.

| Module | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/auth/register` | Register a new user account |
| | `POST` | `/api/auth/login` | Authenticate and receive JWT tokens |
| **Profile** | `GET / PUT` | `/api/profile` | View or update user profile |
| | `GET / POST / DELETE` | `/api/profile/skills` | Manage declared user skills |
| | `GET / PUT` | `/api/profile/preferences` | Manage job search preferences & hard filters |
| **Resumes** | `POST` | `/api/resumes` | Upload a PDF/DOCX resume (`multipart/form-data`) |
| | `GET` | `/api/resumes` | List user resumes and versions |
| **Jobs** | `POST` | `/api/jobs/import` | Import a job with skills (auto-deduplicated) |
| | `GET` | `/api/jobs` | Paginated list of active jobs |
| | `GET` | `/api/jobs/{jobId}` | Fetch job details and required skills |
| **Matching** | `POST` | `/api/matches/evaluate` | Evaluate and cache match score for a job & resume version |
| | `GET` | `/api/matches/resume-versions/{id}` | List scored job matches ordered by score |
| **Applications** | `POST` | `/api/applications` | Create a new job application |
| | `GET` | `/api/applications` | Paginated list of user applications (optional `status` filter) |
| | `PATCH` | `/api/applications/{id}/status` | Transition application status (state-machine validated) |
| | `PUT / GET` | `/api/applications/{id}/answers` | Upsert or fetch application form answers |
| | `GET` | `/api/applications/{id}/events` | Fetch chronological append-only audit log |

---

## Local Setup

### Prerequisites
* Java 21
* Maven 3.9+
* MySQL 8.0+

### 1. Create the Database
```sql
CREATE DATABASE job_agent;
```

### 2. Configure Environment / `application.yml`
Update `src/main/resources/application.yml` (or set environment variables) with your local MySQL credentials, JWT secret, and resume upload directory:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/job_agent
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:your_password}

app:
  jwt:
    secret: ${JWT_SECRET:replace-with-a-secure-256-bit-base64-secret}
  storage:
    resume-dir: ${RESUME_STORAGE_DIR:./uploads/resumes}
```

### 3. Run the Application
Flyway will automatically apply all schema migrations on startup, and Hibernate will validate the entities against the schema.

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Roadmap

- [x] Authentication & JWT Security
- [x] Profile, Skills & Preferences Management
- [x] Resume Upload, Deduplication & Text Extraction
- [x] Job Ingestion & Deterministic Deduplication
- [x] Two-Stage Matching & Scoring Engine
- [x] Application Workflow, State Machine, Optimistic Locking & Audit Log
- [ ] Dashboard Summary Endpoint & Swagger/OpenAPI Documentation
- [ ] Unit Tests (JUnit 5 + Mockito)
- [ ] Docker & Docker Compose Setup
