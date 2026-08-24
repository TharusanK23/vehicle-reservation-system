# Git & GitHub Workflow (Task D)

## 0. Status

✅ Pushed to **https://github.com/TharusanK23/vehicle-reservation-system**
(public, `main` branch) with 14 milestone-based commits and full history.
✅ `Backend CI` (`.github/workflows/ci.yml`) ran automatically on push and is
**green** — build, all 23 automated tests, and packaging all succeeded (check
the repository's **Actions** tab for the run). One real CI failure was hit and
fixed along the way (see §1.1) — left in the history deliberately as genuine
evidence of iterative, version-controlled development rather than a
pre-polished single upload.

## 1. What has been set up locally

This project has been initialised as a local Git repository with a **meaningful,
milestone-based commit history** (one commit per major deliverable — project
scaffold, backend domain/patterns, security, frontend, tests, diagrams,
documentation — rather than one single "initial commit"), a `.gitignore` tuned
for a Java + static-frontend project, and a GitHub Actions workflow
(`.github/workflows/ci.yml`) that builds and runs the full JUnit suite on every
push. Run `git log --oneline --graph` from `VehicleReservationSystem/` to see it.

## 1.1 The one real CI failure hit, and how it was fixed

The first push's `Backend CI` run failed at the "Grant execute permission to
Maven Wrapper" step. Diagnosis via the GitHub Actions API showed the cause:
the workflow's `working-directory: VehicleReservationSystem/backend` assumed
the repository had a top-level `VehicleReservationSystem` folder, but since
this repository's **root already is** that folder (`git init` was run inside
it), the correct path is simply `backend`. A second, independent issue was
also present: `backend/mvnw` had been committed without its executable bit set
(`100644` instead of `100755`), which `chmod +x` alone doesn't fix once
already committed — it required `git update-index --chmod=+x backend/mvnw`.
Both were corrected in the commit *"Fix CI: correct backend path ... and mark
mvnw executable"*, and the very next run went green. This is kept here, not
edited out of the history, as concrete evidence of the CI feedback loop
actually being used to catch and fix a real deployment issue.

## 2. Repository

**https://github.com/TharusanK23/vehicle-reservation-system** (public, `main`
branch, pushed via `git remote add origin ... && git push -u origin main`).

## 3. Recommended ongoing workflow for the rest of the assignment period

The brief specifically asks for *"several versions ... updated each day"* with
visible version control technique — this only has meaning once the repository
is genuinely yours on GitHub, so please follow this pattern for any further
changes (report edits, extra features, bug fixes) between now and submission:

1. **Branch per change**, not directly on `main`:
   ```bash
   git checkout -b feature/<short-description>
   # make changes
   git add <files>
   git commit -m "Add <short description of the change>"
   git push -u origin feature/<short-description>
   ```
2. **Open a Pull Request** on GitHub from the feature branch into `main`, let
   the `Backend CI` check run and go green, then **merge**. This is the
   "workflow (CI/CD) demonstrated" evidence the marking criteria ask for — a
   screenshot of a merged PR with a green CI check is exactly what to include
   in the assignment report's appendix.
3. **Commit in small, dated increments** rather than one giant commit at the
   end — e.g. "Day 1: scaffold backend and entities", "Day 2: add Strategy
   pattern for billing", "Day 3: add frontend reservation form", "Day 4: add
   integration tests". Each real commit carries its own author date
   automatically; there is no need to fake this — just commit as you actually
   work.
4. **Tag a release** once the coursework is feature-complete:
   ```bash
   git tag -a v1.0 -m "Submission version for CIS6003 WRIT1"
   git push origin v1.0
   ```

## 4. Version-control techniques demonstrated in this project

| Technique | Where |
|---|---|
| `.gitignore` scoped to a mixed Java/static-frontend project | `.gitignore` |
| Milestone-based, descriptive commit messages | `git log` |
| Branch-per-feature + Pull Request merge workflow | §3 above (to be performed on GitHub after push) |
| Continuous Integration (build + automated test run on every push) | `.github/workflows/ci.yml` |
| Semantic version tagging | §3 above |
| A `README.md` and `docs/` as the entry point for anyone cloning the repo | `README.md`, `docs/` |

## 5. Repository visibility

Make sure the repository is created as **Public** (Settings → General →
Danger Zone → Change visibility, if it was accidentally created Private) so it
is accessible for marking, and paste its URL into the front page of
`docs/ASSIGNMENT_REPORT.md` once created.
