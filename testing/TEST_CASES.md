# Test Case Matrix — Online Vehicle Reservation System

Legend for **Type**: `POS` positive · `NEG` negative · `BND` boundary ·
`VAL` validation · `API` API contract · `DB` database-level · `INT` integration

All statuses below are **PASS**, captured against the automated suite (see
`testing/evidence/*.txt`, `Tests run: 23, Failures: 0, Errors: 0`) plus a manual
pass against the real MySQL/XAMPP instance per `docs/SETUP.md` §7.

## Authentication

| ID | Type | Description | Steps | Expected Result | Automated In | Status |
|---|---|---|---|---|---|---|
| AUTH-01 | POS | Valid staff login succeeds | POST `/api/auth/login` with `staff1`/`staff123` | `200 OK`, user JSON + `vrs_token` HttpOnly cookie set | `ReservationFlowIntegrationTest.fullReservationAndBillingFlow` (via `login()` helper) | PASS |
| AUTH-02 | NEG | Wrong password rejected | POST `/api/auth/login` with `staff1`/`wrong-password` | `401 Unauthorized` | `ReservationFlowIntegrationTest.loginWithWrongPasswordIsRejected` | PASS |
| AUTH-03 | NEG | Unknown username rejected | POST `/api/auth/login` with a non-existent username | `401 Unauthorized` (no user enumeration - same message as AUTH-02) | Manual (Postman "Login - Unknown User") | PASS |
| AUTH-04 | VAL | Blank username/password rejected | POST `/api/auth/login` with `{"username":"","password":""}` | `400 Bad Request` with `validationErrors.username`/`.password` | Manual (Postman) | PASS |
| AUTH-05 | NEG | Protected endpoint rejects an anonymous request | GET `/api/reservations` with no cookie | `401 Unauthorized` | `ReservationFlowIntegrationTest.protectedEndpointRejectsAnonymousRequest` | PASS |
| AUTH-06 | POS | Logout clears the session | POST `/api/auth/logout`, then GET `/api/auth/me` | `204` then `401` on the follow-up call | Manual (Postman sequence) | PASS |
| AUTH-07 | API | `ADMIN`-only endpoint rejects `STAFF` | Logged in as `staff1`, POST `/api/users` | `403 Forbidden` | Manual (Postman, role check) | PASS |

## Register New Reservation

| ID | Type | Description | Steps | Expected Result | Automated In | Status |
|---|---|---|---|---|---|---|
| RES-01 | POS | Register a reservation for a new customer | POST `/api/reservations` with full new-customer + valid vehicle/date payload | `201 Created`, `reservationNumber` starts with `RES-`, vehicle status becomes `RESERVED` | `ReservationServiceImplTest.registersReservationSuccessfully`, `ReservationFlowIntegrationTest.fullReservationAndBillingFlow` | PASS |
| RES-02 | NEG | Reject booking a vehicle under maintenance | Register with a `vehicleId` whose status is `MAINTENANCE` | `409 Conflict`, message names the vehicle | `ReservationServiceImplTest.rejectsVehicleUnderMaintenance` | PASS |
| RES-03 | NEG | Reject an overlapping (double) booking | Register two reservations for the same vehicle with overlapping date ranges | Second request: `409 Conflict` | `ReservationServiceImplTest.rejectsOverlappingBooking`, `ReservationFlowIntegrationTest.doubleBookingIsRejectedByApi` | PASS |
| RES-04 | NEG | Reject a request with neither an existing nor a new customer | Omit `customerId` **and** `customerFullName`/`address`/`contact` | `400`/`409` with a clear "customer details required" message | `ReservationServiceImplTest.rejectsRequestMissingCustomerDetails` | PASS |
| RES-05 | VAL | Reject a return date before the pickup date | Build a reservation with `returnDate < pickupDate` | `IllegalStateException` → `400 Bad Request` at the API boundary | `ReservationBuilderTest.rejectsReturnDateBeforePickupDate` | PASS |
| RES-06 | BND | Same-day rental requires return time strictly after pickup time | `pickupDate == returnDate`, `returnTime <= pickupTime` | Rejected | `ReservationBuilderTest.rejectsSameDayReturnTimeNotAfterPickupTime` | PASS |
| RES-07 | VAL | Reject missing mandatory fields | Omit `vehicle` from the builder | `IllegalStateException` | `ReservationBuilderTest.rejectsMissingMandatoryFields` | PASS |
| RES-08 | VAL | Reject an invalid contact number format | POST with `customerContactNumber: "abc"` | `400 Bad Request`, `validationErrors.customerContactNumber` | Manual (Postman) | PASS |
| RES-09 | VAL | Reject a pickup date in the past | POST with `pickupDate` = yesterday | `400 Bad Request` (`@FutureOrPresent`) | Manual (Postman) | PASS |
| RES-10 | POS | Register a reservation for an *existing* customer | POST with `customerId` set, no new-customer fields | `201 Created`, response's `customer` matches the existing record | Manual (Postman "Register - Existing Customer") | PASS |
| RES-11 | API | Successful registration produces a unique, correctly-formatted number | Inspect `reservationNumber` in the `201` response | Matches `RES-\d{4}-\d{6}` and is unique per call | `ReservationBuilderTest.buildsValidReservation` | PASS |

## Search / Display Reservation

| ID | Type | Description | Steps | Expected Result | Automated In | Status |
|---|---|---|---|---|---|---|
| SRCH-01 | POS | Find a reservation by its number | GET `/api/reservations/{validNumber}` | `200 OK` with full customer + vehicle + date details | `ReservationFlowIntegrationTest.fullReservationAndBillingFlow` | PASS |
| SRCH-02 | NEG | Unknown reservation number | GET `/api/reservations/RES-2026-999999` | `404 Not Found` | `ReservationServiceImplTest.findByReservationNumberNotFound` | PASS |

## Cancel Reservation

| ID | Type | Description | Steps | Expected Result | Automated In | Status |
|---|---|---|---|---|---|---|
| CANC-01 | POS | Cancelling frees the vehicle | POST `/api/reservations/{number}/cancel` on a `CONFIRMED` reservation | Reservation → `CANCELLED`, vehicle → `AVAILABLE` | `ReservationServiceImplTest.cancelReleasesVehicle` | PASS |
| CANC-02 | NEG | Cannot cancel a completed reservation | POST cancel on a `COMPLETED` reservation | `409 Conflict` | `ReservationServiceImplTest.cannotCancelCompletedReservation` | PASS |

## Calculate and Print Bill

| ID | Type | Description | Steps | Expected Result | Automated In | Status |
|---|---|---|---|---|---|---|
| BILL-01 | POS | Standard weekday pricing | 2-day rental, Tuesday pickup, Rs.5,000/day | subtotal 10,000; tax 800 (8%); total 10,800; `strategyName = STANDARD` | `PricingContextTest$StandardPricing.calculatesStandardPriceForWeekday` | PASS |
| BILL-02 | BND | 1-day rental billed as one full day, not zero | `pickupDate == returnDate` conceptually 0 days | `numberOfDays = 1`, priced accordingly | `PricingContextTest$StandardPricing.oneDayRentalIsBilledAsOneDay`, `BillFactoryTest.sameDayRentalIsBilledAsOneDayMinimum` | PASS |
| BILL-03 | POS | Weekend surcharge applied on Saturday pickup | Saturday pickup, 2 days | 10% surcharge on subtotal, `strategyName = WEEKEND_SURCHARGE` | `PricingContextTest$WeekendPricing.appliesWeekendSurchargeOnSaturdayPickup` | PASS |
| BILL-04 | POS | Weekend surcharge also applies on Sunday pickup | Sunday pickup, 1 day | `strategyName = WEEKEND_SURCHARGE` | `PricingContextTest$WeekendPricing.appliesWeekendSurchargeOnSundayPickup` | PASS |
| BILL-05 | POS | Long-term discount (7+ days) applied | 7-day rental | 15% discount on subtotal, `strategyName = LONG_TERM_DISCOUNT` | `PricingContextTest$LongTermPricing.appliesLongTermDiscountAndOverridesWeekendSurcharge` | PASS |
| BILL-06 | BND | 7-day discount takes priority over a weekend surcharge | 7-day rental *with* a Saturday pickup | Discount applied, **no** surcharge (mutually exclusive by design) | `PricingContextTest$LongTermPricing.appliesLongTermDiscountAndOverridesWeekendSurcharge` | PASS |
| BILL-07 | BND | 6-day rental does not yet qualify for the long-term discount | 6-day rental, weekday pickup | `strategyName != LONG_TERM_DISCOUNT` | `PricingContextTest$LongTermPricing.sixDaysDoesNotQualifyForDiscount` | PASS |
| BILL-08 | POS | Bill number is correctly derived from the reservation number | `create(reservation)` with `reservationNumber = RES-2026-000001` | `billNumber = INV-2026-000001` | `BillFactoryTest.createsBillFromReservation` | PASS |
| BILL-09 | POS | Fetching an already-generated bill returns the same figures | Call GET `/api/bills/reservation/{number}` twice | Both responses identical (bill persisted, not recomputed) | Manual (Postman, repeat call) | PASS |
| BILL-10 | POS | Settling a bill marks it paid | POST `/api/bills/{billNumber}/settle` with a payment method | `paymentStatus = PAID`, `paymentMethod` recorded | Manual (Postman) | PASS |
| BILL-11 | INT | Full price includes 8% tax end-to-end via the live API | Register + generate bill via `ReservationFlowIntegrationTest` | `totalAmount = subtotal * 1.08` for a standard 2-day booking | `ReservationFlowIntegrationTest.fullReservationAndBillingFlow` | PASS |

## Vehicles & Categories

| ID | Type | Description | Steps | Expected Result | Automated In | Status |
|---|---|---|---|---|---|---|
| VEH-01 | POS | Available-vehicle search excludes overlapping bookings | GET `/api/vehicles/available?pickupDate=...&returnDate=...` after a booking exists for that range | The booked vehicle is excluded from results | Manual (Postman + UI walkthrough) | PASS |
| VEH-02 | VAL | Reject a vehicle year outside 1990-2100 | POST `/api/vehicles` with `manufactureYear: 1800` | `400 Bad Request`, `validationErrors.manufactureYear` | Manual (Postman) | PASS |
| VEH-03 | NEG | Reject a duplicate registration number | POST `/api/vehicles` with a registration number already in use | `409 Conflict` | Manual (Postman) | PASS |

## Database-level tests (run directly against MySQL)

| ID | Type | Description | Steps | Expected Result | Status |
|---|---|---|---|---|---|
| DB-01 | DB | `trg_prevent_double_booking` blocks a raw SQL double-insert | `INSERT INTO reservations` directly with dates overlapping an existing active reservation for the same vehicle | Statement fails with `SIGNAL SQLSTATE '45000'` and the custom message | PASS |
| DB-02 | DB | `trg_sync_vehicle_status` frees the vehicle on completion | `UPDATE reservations SET status='COMPLETED' WHERE ...` | Corresponding `vehicles.status` becomes `AVAILABLE` automatically | PASS |
| DB-03 | DB | `sp_daily_revenue_report` returns correct aggregates | `CALL sp_daily_revenue_report('2026-01-01','2026-12-31')` after several bills exist | One row per date, `total_revenue` = sum of that day's `bills.total_amount` | PASS |
| DB-04 | DB | `fn_calculate_rental_days` enforces the minimum-1-day rule | `SELECT fn_calculate_rental_days('2026-03-01','2026-03-01')` | Returns `1`, not `0` | PASS |
| DB-05 | DB | `vw_vehicle_utilization` counts only non-cancelled reservations | Cancel one reservation for a vehicle that also has a confirmed one | `times_booked` reflects only the confirmed booking | PASS |
| DB-06 | DB | Foreign-key/CHECK constraints reject invalid rows | `INSERT INTO vehicles (..., status) VALUES (..., 'BROKEN')` | Rejected by `chk_vehicles_status` | PASS |

## Cross-cutting API contract tests

| ID | Type | Description | Steps | Expected Result | Automated In | Status |
|---|---|---|---|---|---|---|
| API-01 | API | Validation errors return a consistent, field-mapped shape | Any endpoint with `@Valid` violations | `400` body matches `ApiErrorResponse` with a populated `validationErrors` map | All `@Valid`-annotated endpoints (`GlobalExceptionHandler`) | PASS |
| API-02 | API | Unhandled resource-not-found returns `404`, not `500` | GET any `/api/{resource}/{unknownId}` | `404 Not Found` with a descriptive message | `ReservationServiceImplTest.findByReservationNumberNotFound` | PASS |
| API-03 | API | Health endpoint is unauthenticated | GET `/api/health` with no cookie | `200 OK` | Manual (curl, `docs/SETUP.md` §3) | PASS |

---

**Total automated test methods: 23** (run via `./mvnw test`; see
`testing/evidence/` for the captured passing output and `TEST_PLAN.md` §4 for
how each level of this matrix maps to a tool).
