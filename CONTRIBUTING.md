# Contributing

## Branching

- Branch from `main`.
- Name feature branches by what they build, not who's building it:
  `feature/<functionality-slug>`.
  - Good: `feature/vacancy-crud`, `feature/interview-scheduling`,
    `feature/cv-upload`, `feature/assessment-creation`
  - Not: `feature/member1`, `feature/john`, `feature/task3`
- Small fixes/chores use `fix/<slug>` or `chore/<slug>`.

## Commit messages

We use [Conventional Commits](https://www.conventionalcommits.org/), scoped
by module:

```
<type>(<module>): <short summary>

[optional body]
```

- `type`: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`
- `module`: matches the package/module you touched — `auth`, `vacancy`,
  `application`, `resume`, `interview`, `assessment`, `notification`,
  `frontend`, etc.

Examples:

```
feat(vacancy): add vacancy creation endpoint
fix(auth): reject expired refresh tokens
test(application): cover application status transitions
docs(readme): document local MySQL setup
```

## Staging changes — never `git add .`

Always stage files by name (or with a narrow glob), and review `git status`
before committing:

```bash
git add backend/src/main/java/com/recruitsystem/vacancy/VacancyController.java
git status
git commit -m "feat(vacancy): add vacancy creation endpoint"
```

`git add .` / `git add -A` is not allowed on this project — it's how build
output, `.env` files, and IDE folders sneak into commits. If you need to
stage many files, list them explicitly or use a targeted path.

## Before opening a PR

- `./mvnw test` passes (backend)
- `npm test` passes (frontend)
- No changes to files under `application-local.yml`, `.env`, or anything
  matched by `.gitignore`
- Migrations are additive (`V<N+1>__...sql`), never edits to an existing
  migration file that's already merged

## Architecture reminders

- Backend: strict `Controller -> Service -> Repository -> Database`. Put
  entities/DTOs/controllers/services/repositories for your module under the
  matching `<module>` package (e.g. `entity.vacancy`, `dto.vacancy`,
  `controller.vacancy`, `service.vacancy`, `repository.vacancy`).
- Don't put business logic in controllers or entities — controllers call
  services, services call repositories.
- Shared code goes in the cross-cutting packages (`exception`, `config`,
  `security`, `notification`, `util`) — only add to these when something is
  genuinely shared by more than one module.
- Frontend: put module-specific components/pages under
  `components/<module>` and `pages/<module>`; shared UI goes in
  `components/common`.
- If your module adds a route that must be reachable by every role but
  still needs portal chrome (nav/header) — e.g. a shared profile or detail
  page — import `PortalLayoutForRole` from `frontend/src/routes/` and wrap
  your route with it, the same way `ProtectedRoute` is used. **Don't**
  redefine your own role-picking layout helper in `AppRoutes.jsx`; two
  modules independently doing that is exactly the kind of diff that
  conflicts when both land on the same branch.
