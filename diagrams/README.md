# UML & ER Diagrams

This folder contains the full set of design diagrams required by **Task A** of the
CIS6003 assignment brief, plus the Entity-Relationship diagram for the database
(supporting Task B's "proper database" requirement).

| File | Diagram | Notes |
|---|---|---|
| `use-case-diagram.mmd` / `.png` | Use Case Diagram | Actors, use cases, `<<include>>`/`<<extend>>` stereotypes |
| `class-diagram.mmd` / `.png` | Class Diagram | Domain model + service layer + all 5 design patterns |
| `sequence-01-login.mmd` / `.png` | Sequence Diagram 1 | User Authentication (Login) |
| `sequence-02-register-reservation.mmd` / `.png` | Sequence Diagram 2 | Register New Reservation |
| `sequence-03-calculate-print-bill.mmd` / `.png` | Sequence Diagram 3 | Calculate and Print Bill |
| `er-diagram.mmd` / `.png` | Entity Relationship Diagram | Database schema (7 tables) |

All diagrams are authored as [Mermaid](https://mermaid.js.org) source (`.mmd`) and
pre-rendered to `.png` so they can be viewed without any tooling. Mermaid also
renders `.mmd`/fenced-mermaid content natively on GitHub and in Claude
Artifacts, so these files are directly viewable there too, not just as static
images. To regenerate the PNGs after an edit:

```bash
cd diagrams
npx @mermaid-js/mermaid-cli -i class-diagram.mmd -o class-diagram.png -b white -s 2
# repeat per file, or: for f in *.mmd; do npx @mermaid-js/mermaid-cli -i "$f" -o "${f%.mmd}.png" -b white -s 2; done
```

(Requires Node.js; `mmdc` launches a headless Chromium via Puppeteer the first
time it runs, which it downloads automatically.)

**Note on the Class, Sequence and ER diagrams vs. the Use Case diagram:**
Mermaid has first-class native syntax for `classDiagram`, `sequenceDiagram`
and `erDiagram`, so those five diagrams use proper UML/ER constructs
(visibility modifiers, `<<interface>>`/`<<abstract>>` stereotypes,
inheritance/realisation/aggregation/composition arrows, crow's-foot
cardinality). Mermaid has **no** native UML use-case diagram type, so
`use-case-diagram.mmd` is modelled as a flowchart instead - actors as
labelled nodes, use cases as stadium-shaped nodes inside a system-boundary
subgraph, and `<<include>>`/`<<extend>>` as labelled dashed edges - the
closest faithful equivalent the tool supports, while still showing every
actor, use case, and stereotype relationship required by Task A.

## Important note on the assignment brief's scenario

The assignment brief's **title and file name** say "Online Vehicle Reservation
System", but the **scenario text** inside the brief describes a dental clinic
appointment book (patients, dentists, treatment types). This is a known
template-reuse mismatch in the brief document. Per the brief's own instruction -
*"Students are free to make necessary assumptions on system design ... but all
suggestions must be well explained with valid reasons"* - this project implements
the **vehicle reservation** domain named in the title and in the project folder,
carrying over the brief's required functionality one-for-one:

| Brief requirement (dental clinic wording) | This system (vehicle reservation wording) |
|---|---|
| Appointment number | Reservation number (`RES-<year>-<seq>`) |
| Patient (name, address, contact number) | Customer (name, address, contact number, email, licence no.) |
| Dentist name | Vehicle (registration, make, model, year) |
| Treatment type | Vehicle category / rental package (Economy, Sedan, SUV, Van, Luxury) |
| Appointment date & time | Pickup date & time |
| (n/a) | Return date & time - added because a *rental* naturally needs a return, unlike a single-visit appointment |
| Consultation fee -> bill | Category daily rate x rental days -> bill |

## Design decisions & assumptions (Task A)

1. **Two roles, not one.** The brief says "only authorised staff can use the
   system" but does not specify roles. We introduced `ADMIN` and `STAFF` because
   the Excellent-band marking criteria explicitly reward role-based access and a
   more sophisticated data/business model. `ADMIN` manages the fleet, categories
   and staff accounts; `STAFF` performs the day-to-day booking/billing workflow
   described in the brief.
2. **A reservation always has both a pickup and a return date/time.** The brief's
   appointment concept only needs one date/time; a *vehicle rental* is inherently
   a date range, so `returnDate`/`returnTime` were added as a natural, low-risk
   extension explicitly permitted by "Additional functionalities can be included
   as needed."
3. **Customers are first-class, reusable records**, not re-entered on every
   booking - mirroring how the brief's registration data (name/address/contact)
   is described once and then referenced by "appointment number" thereafter.
4. **A bill is generated once per reservation and persisted**, rather than
   recalculated on every view. This avoids a bill silently changing if a vehicle
   category's daily rate is edited later, which is both a more realistic business
   rule and a cleaner demonstration of the Factory pattern (Task B).
5. **Double-booking prevention** is treated as a first-class business rule (not
   stated in the brief) because a real reservation system's core value is exactly
   this guarantee. It is enforced twice, deliberately: once in
   `ReservationServiceImpl` (a clear error message to the user) and once again as
   a MySQL trigger `trg_prevent_double_booking` (a database-level invariant that
   holds even if a future client bypasses the REST API) - see `database/schema.sql`.
6. **"Exit System"** is interpreted, for a web application, as a secure Logout
   that clears the authentication cookie server-side - the closest safe-shutdown
   equivalent for a browser-based client. See `docs/ASSIGNMENT_REPORT.md` for the
   full justification.

## How the diagrams support the design (traceability)

- The **Use Case Diagram** enumerates every functionality from the brief (Login,
  Register New Reservation, Search/Display, Calculate & Print Bill, Help, and
  Logout/Exit) plus the additional Admin-only use cases, and shows the two
  `<<include>>` relationships that are *always* executed (checking availability,
  calculating a base cost) versus the three `<<extend>>` relationships that are
  *conditional* (new-customer registration, weekend surcharge, long-term discount).
- The **Class Diagram** shows exactly which classes implement each of the five
  design patterns used in Task B (Builder, Factory Method, Strategy, Observer,
  Singleton), with correct public/private visibility, and the aggregation/
  composition/multiplicity of the domain model (e.g. a `Reservation` *contains*
  at most one `Bill` - composition - but only *references* a `Customer`/`Vehicle`
  - aggregation).
- The three **Sequence Diagrams** trace the three most important use cases
  end-to-end, from the browser through the controller/service/repository layers
  and down to MySQL, including the specific point at which each design pattern
  is invoked.
- The **ER Diagram** matches `database/schema.sql` exactly (table/column names,
  primary/foreign keys, and the derived `notification_logs` correlation).
