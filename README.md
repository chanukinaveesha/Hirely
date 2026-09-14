# Hirely — Web-Based Recruitment Company System

A recruitment company platform connecting job seekers, recruiters, HR executives,
interview panel members, and system administrators.

This repository currently contains the **Phase 1 foundation**: shared entities,
authentication, security wiring, and app scaffolding that six feature branches
will build on top of. No feature business logic (vacancy CRUD, application
search, CV upload, interview scheduling, assessment creation, etc.) lives here
yet — each team member implements their functionality on their own
`feature/<functionality-slug>` branch.

## Tech stack

| Layer    | Technology |
|----------|------------|
| Backend  | Java 21, Spring Boot 3.5.x, Maven, Spring Data JPA + Hibernate, Flyway, Spring Security (stateless JWT), springdoc-openapi |
| Database | MySQL 8.x |
| Frontend | React 18 + Vite, React Router v6, Zustand, Axios, Tailwind CSS |
| Testing  | JUnit 5 + Mockito + MockMvc (backend), Vitest + React Testing Library (frontend) |

## Prerequisites

- Java 21 (JDK). **Important:** if you have a newer JDK (e.g. 24) installed
  as your system default, Lombok's annotation processing does not yet work
  reliably with it — point `JAVA_HOME` at a JDK 21 installation before running
  Maven. Example (bash):
  ```bash
  export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot"
  ```
- Node.js 20 LTS and npm
- MySQL 8.x running locally (or accessible remotely)
- No local Maven install required — this repo uses the Maven Wrapper (`mvnw`)

## Running MySQL locally

1. Install MySQL 8.x (e.g. via the official installer, Docker, or a package
   manager).
2. Create a database and a user with privileges on it, for example:
   ```sql
   CREATE DATABASE recruitment_system CHARACTER SET utf8mb4;
   CREATE USER 'recruit_app'@'localhost' IDENTIFIED BY 'a-strong-local-password';
   GRANT ALL PRIVILEGES ON recruitment_system.* TO 'recruit_app'@'localhost';
   FLUSH PRIVILEGES;
   ```
   (The connection string in `application-local.yml.example` also sets
   `createDatabaseIfNotExist=true`, so step above is optional if your MySQL
   user has permission to create databases.)

## Backend setup

```bash
cd backend

# 1. Copy the local config template and fill in your DB credentials + a JWT secret
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# edit src/main/resources/application-local.yml

# 2. Run the app (applies Flyway migrations automatically on startup)
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. Swagger UI is at
`http://localhost:8080/swagger-ui.html`.

Run the backend test suite:

```bash
./mvnw test
```

### How migrations work

- Flyway migrations live in `backend/src/main/resources/db/migration`, named
  `V<version>__description.sql` (e.g. `V1__init_schema.sql`).
- Hibernate's `ddl-auto` is set to `validate` — **it never creates or alters
  schema**. All schema changes must go through a new Flyway migration file.
- Never edit a migration that has already been applied/committed; add a new
  `V<N+1>__...sql` file instead.
- Each feature module owns its own entities/migrations (e.g. the vacancy
  module adds `V2__create_vacancies.sql`, and can then add the
  `applications.vacancy_id` foreign key that this foundation intentionally
  left unconstrained).

## Frontend setup

```bash
cd frontend
npm install
npm run dev
```

The app starts on `http://localhost:5173` and talks to the backend at
`http://localhost:8080/api` by default (override with a `.env` file setting
`VITE_API_BASE_URL`).

Run the frontend test suite:

```bash
npm test
```

## Project structure

```
backend/    Spring Boot API (controller -> service -> repository -> DB)
frontend/   React + Vite SPA
docs/       planning notes, API docs, architecture notes
database/   ER diagram exports, seed data scripts
```

See `CONTRIBUTING.md` for branching, commit conventions, and staging rules.
