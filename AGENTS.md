# AGENTS.md

## Cursor Cloud specific instructions

### Overview

This is a **Spring Boot 4.0.3** data pump microservice (`read_opendata_apis`) that fetches Open Data from Open Finance Brasil (OFB) participant institutions and stores it in PostgreSQL. There are no REST controllers — the core logic is a `@Scheduled` task in `DataPumpScheduler`.

### Prerequisites

- **Java 25** — installed via SDKMAN (`sdk use java 25.0.2-open`)
- **Maven** — the project uses the Maven wrapper (`./mvnw`); the `.mvn/` directory must exist (if missing, run `mvn -N wrapper:wrapper`)
- **Docker** — required to run PostgreSQL 16 via `docker compose`

### Running services

1. **PostgreSQL**: `sudo docker compose up -d postgres` (port 5432, user/pass: `postgres`/`postgres`)
2. **Application**: `./mvnw spring-boot:run` (starts on port **9091**, NOT 8081 — `application.yml` overrides `application.yaml`)

### Non-obvious gotchas

- There are **two config files**: `application.yaml` (sets port 8081, datasource) and `application.yml` (sets port 9091, actuator). Spring Boot merges them but `application.yml` wins for overlapping keys like `server.port`, so the actual port is **9091**.
- The app connects to database `postgres` (the default PostgreSQL database), not `opendata` (which is the DB created by `docker-compose.yaml` `POSTGRES_DB`). This is intentional — Hibernate auto-creates the needed tables in the `postgres` DB.
- The `DataPumpScheduler` starts immediately on boot and runs every ~55 hours (`fixedRate = 200000000` ms). During tests (`@SpringBootTest`), it also fires, which causes the test JVM to stay alive briefly while fetching external APIs.
- The Surefire "kill self fork JVM" warning during `./mvnw test` is expected — the scheduler's HTTP calls keep threads alive after the test completes.
- **No REST endpoints** exist — the service is a background data pump only. Verify it works by querying the `"opendata-new"` table in PostgreSQL.
- SDKMAN must be sourced in each new shell session: `source "$HOME/.sdkman/bin/sdkman-init.sh"`

### Standard commands

- **Build**: `./mvnw clean compile`
- **Test**: `./mvnw test`
- **Run**: `./mvnw spring-boot:run`
- **Package**: `./mvnw clean package`
