# Setup, Configuration & Testing Guide

Complete instructions for installing, configuring, running, and validating the
Sunrise Vehicle Rentals — Online Vehicle Reservation System entirely on
**localhost**. This system was built and verified end-to-end on Windows using
XAMPP at `F:\Program\xampp`; adjust paths if your XAMPP is installed elsewhere.

---

## 1. Prerequisites

| Requirement | Version used in development | Notes |
|---|---|---|
| Java (JDK) | 17 (LTS) | `java -version` |
| XAMPP | any recent version bundling MariaDB 10.4+ | Only **MySQL/MariaDB** and (optionally) **Apache** are used — PHP is not required by this project |
| A modern browser | Chrome/Edge/Firefox | For the client and for printing bills to PDF |
| Internet access | first run only | Maven Wrapper downloads Maven + all dependencies on the first build |
| Maven | *not required* | The project ships the **Maven Wrapper** (`mvnw` / `mvnw.cmd`), which downloads Maven automatically |

You do **not** need Node.js, PHP, or any global Maven install — the backend is
fully self-bootstrapping via the Maven Wrapper, and the frontend is plain
HTML/CSS/JS with Bootstrap vendored locally (no build step, no CDN dependency).

---

## 2. Database setup (MySQL via XAMPP)

### 2.1 Start MySQL

Open the **XAMPP Control Panel** and click **Start** next to *MySQL* (and
*Apache*, if you intend to serve the frontend through it — see §4). If you
prefer the command line:

```
"F:\Program\xampp\mysql_start.bat"
```

Verify it is listening:

```
"F:\Program\xampp\mysql\bin\mysql.exe" -u root -e "SELECT VERSION();"
```

You should see a MariaDB version string. If this fails, see
**Troubleshooting §8.1** below (this is the single most common setup problem).

### 2.2 Import the schema

The complete schema (7 tables, 2 triggers, 1 stored procedure, 1 function, 2
views, and seed data) lives in **`database/schema.sql`**.

**Option A — phpMyAdmin (GUI):**
1. Open `http://localhost/phpmyadmin`
2. Click **Import** → **Choose File** → select `database/schema.sql` → **Go**

**Option B — command line (recommended, does not require phpMyAdmin/PHP to be working):**

```
"F:\Program\xampp\mysql\bin\mysql.exe" -u root < "database\schema.sql"
```

Both create the `vehicle_reservation_db` database from scratch, so the script
is safe to re-run at any time to reset to a clean state.

### 2.3 Verify

```
"F:\Program\xampp\mysql\bin\mysql.exe" -u root vehicle_reservation_db -e "SHOW TABLES; SELECT COUNT(*) FROM users;"
```

Expected tables: `bills`, `customers`, `notification_logs`, `reservations`,
`users`, `vehicle_categories`, `vehicles`, plus views `vw_reservation_summary`
and `vw_vehicle_utilization`. `users` should contain 2 seeded accounts.

### 2.4 Seed accounts

| Username | Password | Role | Purpose |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Fleet, categories, staff accounts, all reports |
| `staff1` | `staff123` | STAFF | Day-to-day reservation & billing operations |

Passwords are stored as BCrypt hashes (`database/schema.sql`); they are never
stored or transmitted in plain text.

---

## 3. Run the backend (Spring Boot API)

```
cd VehicleReservationSystem\backend
.\mvnw.cmd spring-boot:run
```

The first run downloads Maven itself plus all dependencies (needs internet) and
takes a minute or two; subsequent runs are fast. When ready you will see:

```
Started VehicleReservationApplication in X.XXX seconds
```

The API listens on **`http://localhost:8081`**. Confirm it is up:

```
curl http://localhost:8081/api/health
```

should return `{"status":"UP", ...}`.

Interactive API documentation (Swagger UI) is available at
**`http://localhost:8081/swagger-ui.html`**.

### 3.1 Configuration

All configuration lives in `backend/src/main/resources/application.yml`:

| Property | Default | Purpose |
|---|---|---|
| `server.port` | `8081` | API port |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/vehicle_reservation_db` | XAMPP MySQL default port |
| `spring.datasource.username` / `password` | `root` / *(empty)* | XAMPP MySQL default credentials |
| `app.security.jwt.secret` | (demo key) | HMAC signing key for login tokens — **rotate before any real deployment** |
| `app.business.tax-rate`, `weekend-surcharge-rate`, `long-term-discount-*` | 8% / 10% / 7 days / 15% | Billing rules used by the Strategy pattern |
| `app.cors.allowed-origins` | `http://localhost,http://127.0.0.1,...` | Add your frontend's exact origin here if it differs |

If your MySQL root user has a password, or XAMPP's MySQL runs on a different
port, edit `spring.datasource.url`/`username`/`password` accordingly.

### 3.2 Stopping the backend

Press `Ctrl+C` in the terminal running `mvnw spring-boot:run`.

---

## 4. Run the frontend (client)

The client is pure static HTML/CSS/JS — no build step. Two ways to serve it,
both are "localhost on a suitable port":

**Option A — XAMPP Apache (recommended, matches the brief's "use XAMPP" instruction):**

Copy (or symlink) the `frontend` folder into XAMPP's web root, then browse to it:

```
xcopy /E /I "frontend" "F:\Program\xampp\htdocs\vehicle-reservation-client"
```

Start Apache in the XAMPP Control Panel, then open:

**`http://localhost/vehicle-reservation-client/`**

**Option B — any static file server**, e.g. with Node (already on this machine):

```
npx http-server frontend -p 5500
```

then open `http://localhost:5500/`.

> Whichever port you use, the API's CORS configuration (`app.cors.allowed-origins`
> in `application.yml`) already allows `http://localhost` (port 80, XAMPP's
> default) and `http://localhost:5500`. Add any other port you use to that list
> and restart the backend.

### 4.1 Changing the API URL the frontend calls

`frontend/assets/js/api.js` defaults to `http://localhost:8081/api`. Override it
without editing the file by adding, before the other `<script>` tags on any
page, e.g.:
```html
<script>window.VRS_API_BASE = "http://localhost:8081/api";</script>
```

---

## 5. Accessing the application

| What | URL |
|---|---|
| Client (login page) | `http://localhost/vehicle-reservation-client/` (or your chosen static server URL) |
| REST API base | `http://localhost:8081/api` |
| Swagger / OpenAPI UI | `http://localhost:8081/swagger-ui.html` |
| phpMyAdmin (optional) | `http://localhost/phpmyadmin` |

Log in with `admin`/`admin123` or `staff1`/`staff123` (§2.4).

---

## 6. Testing the API directly

### 6.1 Postman

Import **`testing/postman/VehicleReservation.postman_collection.json`**. It is
pre-configured with a `{{baseUrl}}` variable (`http://localhost:8081/api`) and
a login request that captures the session cookie automatically for the
subsequent requests in the collection (Postman handles cookies per-domain by
default — just run the requests in order, or use "Run collection").

### 6.2 curl

```bash
# Login (saves the session cookie to cookies.txt)
curl -c cookies.txt -H "Content-Type: application/json" \
     -d '{"username":"staff1","password":"staff123"}' \
     http://localhost:8081/api/auth/login

# List vehicles
curl -b cookies.txt http://localhost:8081/api/vehicles

# Register a reservation
curl -b cookies.txt -H "Content-Type: application/json" -d '{
  "customerFullName":"Kasun Fernando","customerAddress":"Colombo 03",
  "customerContactNumber":"0771234567","vehicleId":1,
  "pickupDate":"2026-09-01","pickupTime":"09:00:00",
  "returnDate":"2026-09-04","returnTime":"09:00:00"
}' http://localhost:8081/api/reservations

# Calculate the bill
curl -b cookies.txt http://localhost:8081/api/bills/reservation/RES-2026-000001
```

### 6.3 Swagger UI

Open `http://localhost:8081/swagger-ui.html`, log in via the client first (so
the browser holds the auth cookie), then "Try it out" on any endpoint.

---

## 7. Validating the major features (walkthrough matching the assignment brief)

1. **User Authentication (Login)** — Open the client; try an invalid password
   (expect a clear "Invalid username or password" message and a `401`); then
   log in with `staff1`/`staff123`.
2. **Register New Appointment (Reservation)** — Dashboard → *+ New Reservation*.
   Fill in a new customer, choose a category, click *Check Availability*, pick a
   vehicle and dates, *Save*. Confirm the generated reservation number (`RES-…`)
   and that the vehicle's status becomes `RESERVED` on the Vehicles page.
3. **Display Appointment Details** — Reservations → *Find a Reservation* → enter
   the reservation number → confirm all customer/vehicle/date details display.
4. **Calculate and Print Bill** — From the reservation detail page, click
   *Generate / View Bill*; confirm the total (daily rate × days + tax, and any
   weekend surcharge/long-term discount), then click *Print Receipt* (browser
   print dialog → *Save as PDF* works for evidence capture).
5. **Help Section** — Open *Help* in the navbar; confirm the step-by-step guide
   loads from `GET /api/help`.
6. **Exit System** — Click *Logout*; confirm you are redirected to the login
   page and that navigating back to a protected page (e.g. Dashboard) redirects
   you to Login again (the session cookie was cleared).
7. **Double-booking prevention** — Try registering a second reservation for the
   *same* vehicle with overlapping dates; expect an HTTP `409 Conflict` with a
   clear message, both from the UI and confirmed at the database level by the
   `trg_prevent_double_booking` trigger.
8. **Reports** — Open *Reports*; confirm the daily revenue report (backed by
   the `sp_daily_revenue_report` stored procedure) and vehicle utilisation
   report (backed by the `vw_vehicle_utilization` view) both render.
9. **Admin-only screens** — Log in as `admin`; confirm *Staff Accounts* is
   visible and usable. Log in as `staff1`; confirm it is hidden, and that
   directly calling `POST /api/users` as staff returns `403 Forbidden`.

---

## 8. Running the automated test suite

```
cd VehicleReservationSystem\backend
.\mvnw.cmd test
```

This runs all JUnit 5 + Mockito unit tests and the full Spring Boot integration
test (real Spring context, real Spring Security filter chain, in-memory H2
database) covering login, registration, double-booking rejection, and billing.
A summary is printed at the end (`Tests run: 23, Failures: 0, Errors: 0`), and
per-class reports are written to `backend/target/surefire-reports/`. See
`testing/TEST_PLAN.md` and `testing/TEST_CASES.md` for the full rationale, test
data, and traceability matrix, and `testing/evidence/` for a captured passing
run.

---

## 9. Troubleshooting common errors

### 9.1 `mysqld`/Apache won't start / phpMyAdmin shows connection errors / PHP warnings about missing extensions

**Cause:** if your XAMPP folder was moved or copied from its original install
location (commonly `C:\xampp` or `...\Program Files\xampp`) to a new path
without running XAMPP's path-fixer, several config files still contain the
**old, hard-coded absolute paths** — this was encountered and fixed during
this project's own setup, where XAMPP had been moved to `F:\Program\xampp`
while its configs still referenced `F:\Program Files\xampp\...`. Four files
were affected and fixed the same way:

| File | Symptom if unfixed |
|---|---|
| `mysql\bin\my.ini` | `mysqld` fails to start at all (no error log even created) |
| `apache\conf\httpd.conf` | `httpd.exe: ... ServerRoot must be a valid directory` |
| `apache\conf\extra\httpd-xampp.conf` | Apache fails to load the PHP module (`LoadFile`/`LoadModule` paths) |
| `php\php.ini` | `PHP Fatal error: Unable to start standard module`; phpMyAdmin/Apache-hosted PHP breaks |

**Fix:** in each file, find every absolute path containing the *old* XAMPP
location and replace it with your **actual** install folder (a simple
find-and-replace across the file is sufficient — e.g. `socket=`, `basedir=`,
`datadir=` in `my.ini`; `ServerRoot`/`DocumentRoot` in `httpd.conf`;
`LoadFile`/`LoadModule`/`PHPINIDir` in `httpd-xampp.conf`; `extension_dir=`,
`error_log=`, `session.save_path=` etc. in `php.ini`), then restart the
affected service(s) from the XAMPP Control Panel. Since this project's
backend talks to MySQL directly and the frontend is plain static files, you
only strictly need the `my.ini` fix to run this system — the Apache/PHP fixes
are only needed if you want to serve the frontend through XAMPP's Apache
(§4 Option A) or use phpMyAdmin; Option B (a plain static file server) and
Option B of §2.2 (the `mysql.exe` CLI import) avoid Apache/PHP entirely.

### 9.2 Backend fails to start with a MySQL connection error

- Confirm MySQL is running (§2.1) and reachable on port `3306`.
- Confirm `vehicle_reservation_db` exists (§2.2/§2.3).
- Check `spring.datasource.username`/`password` in `application.yml` match your
  MySQL root credentials (XAMPP's default is `root` with **no password**).

### 9.3 `401 Unauthorized` on every request from the browser client, even right after logging in

- The API and the client must be treated as different **origins** by the
  browser (e.g. `http://localhost:8081` vs `http://localhost`). Confirm the
  client's origin is present in `app.cors.allowed-origins` (§3.1) and restart
  the backend after editing it.
- Confirm cookies are not being blocked — the login flow relies on an HttpOnly
  cookie sent with `credentials: 'include'`; some browser privacy modes/
  extensions block third-party-looking cookies on `localhost`. Test in a normal
  (non-incognito) window first.

### 9.4 `403 Forbidden` instead of expected success

- You are logged in as `STAFF` but calling an `ADMIN`-only endpoint (e.g.
  `POST /api/users`, `DELETE /api/vehicles/{id}`). Log in as `admin` instead.

### 9.5 `409 Conflict` when registering a reservation

- This is the double-booking guard working as intended — the chosen vehicle
  already has an overlapping reservation. Pick a different vehicle or date
  range, or check *Vehicles* for one with `AVAILABLE` status.

### 9.6 Reports page shows an error

- The **Reports** screens depend on the stored procedure and view created by
  `database/schema.sql` (§2.2). If you only let Hibernate auto-create tables
  (`ddl-auto: update`) without ever running `schema.sql`, those objects will be
  missing. Re-run §2.2, Option B.

### 9.7 Port already in use (`8081` or `80`/`5500`)

- Change `server.port` in `application.yml` for the backend, or serve the
  frontend on a different port (§4) — just remember to add the new frontend
  origin to `app.cors.allowed-origins` (§3.1).

### 9.8 `mvnw.cmd` fails on the very first run

- This step needs internet access (it downloads Maven itself). If you are
  behind a proxy, configure it via the `MAVEN_OPTS` environment variable, or
  install Maven normally and run `mvn spring-boot:run` instead of `mvnw.cmd`.

---

## 10. Full stop / restart checklist

1. Backend: `Ctrl+C` in its terminal (or close the window).
2. Apache/MySQL: stop via the XAMPP Control Panel, or `mysql_stop.bat` /
   `apache_stop.bat` in the XAMPP root.
3. To reset all data to the original seed state at any time, simply re-run
   §2.2 — `database/schema.sql` drops and recreates everything.

---

## 11. Regenerating the PDF report

`docs/ASSIGNMENT_REPORT.pdf` is a committed, ready-to-submit export of
`docs/ASSIGNMENT_REPORT.md`, already formatted to the brief's spec (A4,
margins 1.5in/1in, 1.5 line spacing, Times New Roman, 14pt bold headings,
12pt body, page numbers bottom-right) with every diagram and screenshot
embedded. If you edit the report or capture new screenshots, regenerate it:

```
cd docs
npm install        # first time only - installs marked + puppeteer-core
node generate-pdf.js
```

Requires a local Chrome install (default path in `generate-pdf.js`; override
with the `CHROME_PATH` environment variable if yours is elsewhere, e.g. Edge
at `C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe`). This is a
one-off documentation tool, not part of the running application - Node.js is
not otherwise required anywhere in this project.

To capture fresh screenshots first, log into the client at
`http://localhost/vehicle-reservation-client/` (§5) and screenshot each page
listed in `testing/screenshots/`, or automate it with a headless-browser
script following the same pattern as `docs/generate-pdf.js`.
