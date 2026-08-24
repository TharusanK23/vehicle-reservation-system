# Git & GitHub Workflow (Task D)

## 1. What has been set up locally

This project has been initialised as a local Git repository with a **meaningful,
milestone-based commit history** (one commit per major deliverable — project
scaffold, backend domain/patterns, security, frontend, tests, diagrams,
documentation — rather than one single "initial commit"), a `.gitignore` tuned
for a Java + static-frontend project, and a GitHub Actions workflow
(`.github/workflows/ci.yml`) that builds and runs the full JUnit suite on every
push. Run `git log --oneline --graph` from `VehicleReservationSystem/` to see it.

## 2. Turning this into your public GitHub repository

The assistant that generated this project does not have — and should not be
given — your GitHub credentials, so the steps below must be completed by you.
They take about two minutes.

```bash
cd VehicleReservationSystem

# 1. Create a new, empty PUBLIC repository on https://github.com/new
#    (do NOT initialise it with a README/license - this project already has one)

# 2. Point your local repo at it and push everything, including history:
git remote add origin https://github.com/<your-username>/vehicle-reservation-system.git
git branch -M main
git push -u origin main
```

GitHub Actions will run automatically on this first push — check the **Actions**
tab of your repository; you should see the `Backend CI` workflow run and pass.

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
