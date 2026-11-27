# Job Agent Backend

A production-ready, containerized **Modular Monolith** backend built with **Java 21**, **Spring Boot**, **MySQL 8**, and **Docker** to manage job discovery, resume parsing, rule-based and skill-weighted matching, scheduled background automation, and end-to-end application lifecycle tracking.

---

## Why This Project Stands Out (Engineering Impact)

Most portfolio backends stop at basic CRUD. This project was engineered to solve real-world backend challenges:

- **Strict State Machine Enforcement (FSM):** Job application statuses follow a deterministic Finite State Machine (`ApplicationStatus`), blocking illegal state transitions at the domain level before they ever touch the database.
- **Concurrency Control (Optimistic Locking):** Prevents lost updates during concurrent modifications by using JPA optimistic locking paired with a global `409 Conflict` exception handler.
- **Append-Only Audit Trail:** Every status transition is immutably logged in an append-only audit table for complete traceability.
- **Rule-Based & Skill-Weighted Matching Engine:** Evaluates jobs against user hard-preference rules and weighted required/optional skill overlap, caching results by job content hash (`contentHash`) to skip unchanged jobs during re-evaluation.
- **Fault-Isolated Background Scheduling:** Runs periodic batch evaluations across registered users via Spring `@Scheduled` with configurable cron expressions, a dedicated scheduler thread pool, and per-user exception handling so one user's failure never aborts the batch.
- **Optimized Read Queries:** Dashboard analytics (`GET /api/applications/summary`) use **Spring Data JPA Interface Projections** to aggregate counts directly at the database level without loading unnecessary entity graphs.
- **Automated CI & Zero-Friction Deployment:** GitHub Actions runs Maven tests and Docker builds on pushes/PRs, while a multi-stage `Dockerfile` (Maven builder + lightweight Alpine JRE running as non-root `appuser`) pairs with MySQL 8 and persistent Docker volumes.

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language & Framework** | Java 21, Spring Boot, Spring Web, Spring Security |
| **Database & Migrations** | MySQL 8.0, Spring Data JPA, Hibernate, Flyway |
| **Scheduling & Automation** | Spring `@Scheduled` (`TaskScheduler`), GitHub Actions |
| **Authentication** | Stateless JWT (`jjwt`), BCrypt Password Encoding |
| **Document Processing** | Apache PDFBox, Apache POI (Resume Storage & Parsing) |
| **API Documentation** | OpenAPI 3 / Swagger UI (`springdoc-openapi`) |
| **Testing** | JUnit 5, Mockito |
| **DevOps & Monitoring** | Docker, Docker Compose, GitHub Actions, Spring Boot Actuator |

---

## Core Modules

1. **Auth & Security:** Stateless JWT registration/login flow with role-based security filter chain.
2. **Profile & Skills:** Candidate profile management, preferences, and structured skill indexing.
3. **Resume Management:** Secure file upload, text extraction, and persistent storage mapped to Docker volumes (`resume_uploads`).
4. **Jobs & Sources:** Job ingestion and structured job listing management with content hashing.
5. **Matching Engine:** Hard-preference filtering, weighted skill scoring, recommendations, and content-hash caching.
6. **Applications & FSM:** Full lifecycle tracking with state transition validation, optimistic locking, and append-only audit logs.
7. **Dashboard Summary:** Aggregated application metrics via JPA projections.
8. **Background Scheduler (`scheduler` & `config`):** `@EnableScheduling` configuration and `JobMatchingScheduler` batch runner (`evaluateAllJobsForUser`) across users.

---

## Background Scheduling (`@Scheduled`)

`JobMatchingScheduler` periodically iterates over registered users and evaluates job-resume pairs while isolating errors per user.

Configure scheduling in `src/main/resources/application.properties` (or via environment variables):

```properties
jobagent.scheduler.enabled=true
jobagent.scheduler.matching-cron=0 0 */6 * * *
spring.task.scheduling.pool.size=4
spring.task.scheduling.thread-name-prefix=job-agent-scheduler-
Default schedule (0 0 */6 * * *): Runs every 6 hours (00:00, 06:00, 12:00, 18:00).

Local smoke verification (0 * * * * *): Triggers every minute on the :00 second mark—switch back to 0 0 */6 * * * after testing.

Disabling during tests: Set jobagent.scheduler.enabled=false when you do not want scheduled tasks to trigger during test runs.

Quick Start (Docker Compose)
Run the entire stack (MySQL 8 + Spring Boot Backend) with Docker Compose.

Prerequisites
Docker Desktop (with WSL 2 on Windows)

1. Clone the repository
Bash
git clone [https://github.com/Md-Altaf1/AI-Agent.git](https://github.com/Md-Altaf1/AI-Agent.git)
cd AI-Agent/backend
2. Start the application stack
Bash
docker compose up --build
(Use docker compose up --build -d to run in detached background mode.)

3. Verify the running services
Swagger UI (Interactive API Docs): http://localhost:8080/swagger-ui.html

Actuator Health Check: http://localhost:8080/actuator/health

4. Stop the containers
Bash
docker compose down
(Database data and uploaded resumes persist safely in Docker named volumes mysql_data and resume_uploads.)

Port 8080 note: Do not run BackendApplication directly from IntelliJ IDEA while the job-agent-backend Docker container is already occupying port 8080. Run docker compose down before starting locally in IntelliJ, or run docker compose up --build after code changes when running in Docker.

Running Automated Tests & CI
Unit tests cover Finite State Machine transition rules, ApplicationService business logic, and JobMatchingScheduler using JUnit 5 and Mockito:

Bash
./mvnw test
(On Windows PowerShell, you can also run .\mvnw.cmd test or mvn test.)

GitHub Actions (.github/workflows/ci.yml) automatically runs mvn -B test and builds the Docker image on pushes and pull requests.

Key Engineering Learnings
Domain-Driven Guardrails: Encapsulating state transition rules inside the domain (ApplicationStatus) keeps service classes clean and eliminates invalid data states.

Schema Evolution with Flyway: Managing database changes through versioned SQL migrations ensures identical schemas across local development and Docker containers.

Container Security & Permissions: Running containers as a non-root user (appuser) and properly configuring Linux directory ownership (chown) for mounted volumes prevents production AccessDeniedException failures.

Multi-Stage Docker Optimization: Separating Maven dependency resolution and compilation from the final eclipse-temurin:21-jre-alpine runtime drastically reduces image size and attack surface.

Upcoming Roadmap
External Job Board Connectors: Automated ingestion jobs pulling fresh listings from external job APIs before scheduled matching runs.

AI Layer (LLM Integration): Automated resume-to-JD gap analysis and tailored cover letter generation.

Chrome Extension Companion: 1-click form auto-filler connected to this backend via authenticated REST APIs.


### Next Steps Before Pushing
1. Save this content into your repository `README.md` file.
2. Stage, commit, and push your `README.md` along with your scheduler, tests, workflow, and Docker files:
   ```powershell
   git add README.md
   git commit -m "docs: update README with matching engine, scheduler, and CI details"
   git push
Would you like to add a quick API Endpoints / Swagger Flow table to this README.md before pushing it to GitHub?
