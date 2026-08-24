(async () => {
    const user = await requireAuth();
    if (!user) return;

    const newCard = document.getElementById("newReservationCard");
    const params = new URLSearchParams(location.search);

    document.getElementById("toggleFormBtn").addEventListener("click", () => newCard.classList.toggle("d-none"));
    document.getElementById("cancelFormBtn").addEventListener("click", () => newCard.classList.add("d-none"));
    if (params.get("new") === "1") newCard.classList.remove("d-none");

    document.querySelectorAll('input[name="customerMode"]').forEach(radio => {
        radio.addEventListener("change", () => {
            const existing = document.getElementById("modeExisting").checked;
            document.getElementById("existingCustomerBlock").style.display = existing ? "" : "none";
            document.getElementById("newCustomerBlock").style.display = existing ? "none" : "";
        });
    });

    await loadCategories();
    await loadCustomers();
    await loadReservations();

    document.getElementById("searchForm").addEventListener("submit", (e) => {
        e.preventDefault();
        const number = document.getElementById("searchNumber").value.trim();
        if (number) window.location.href = "reservation-detail.html?number=" + encodeURIComponent(number);
    });

    document.getElementById("checkAvailabilityBtn").addEventListener("click", checkAvailability);
    document.getElementById("reservationForm").addEventListener("submit", submitReservation);

    async function loadCategories() {
        try {
            const categories = await Api.get("/vehicle-categories");
            const select = document.getElementById("categoryFilter");
            select.innerHTML = `<option value="">Any category</option>` +
                categories.map(c => `<option value="${c.id}">${c.categoryName} (${formatCurrency(c.dailyRate)}/day)</option>`).join("");
        } catch (err) {
            showAlert("alertBox", "Could not load vehicle categories.");
        }
    }

    async function loadCustomers() {
        try {
            const customers = await Api.get("/customers");
            const select = document.getElementById("customerSelect");
            select.innerHTML = customers.map(c => `<option value="${c.id}">${c.fullName} - ${c.contactNumber}</option>`).join("");
        } catch (err) { /* non-fatal */ }
    }

    async function checkAvailability() {
        clearFieldErrors();
        const pickupDate = document.getElementById("pickupDate").value;
        const returnDate = document.getElementById("returnDate").value;
        const categoryId = document.getElementById("categoryFilter").value;
        const vehicleSelect = document.getElementById("vehicleSelect");

        if (!pickupDate || !returnDate) {
            showAlert("formAlert", "Please choose both a pickup date and a return date before checking availability.");
            return;
        }
        if (returnDate < pickupDate) {
            showAlert("formAlert", "Return date cannot be before the pickup date.");
            return;
        }
        clearAlert("formAlert");

        try {
            const vehicles = await Api.get("/vehicles/available", { pickupDate, returnDate, categoryId: categoryId || undefined });
            if (vehicles.length === 0) {
                vehicleSelect.innerHTML = `<option value="">No vehicles available for these dates</option>`;
            } else {
                vehicleSelect.innerHTML = `<option value="">-- Select a vehicle --</option>` +
                    vehicles.map(v => `<option value="${v.id}">${v.registrationNumber} - ${v.make} ${v.model} (${v.category.categoryName}, ${formatCurrency(v.category.dailyRate)}/day)</option>`).join("");
            }
        } catch (err) {
            showAlert("formAlert", "Could not check vehicle availability.");
        }
    }

    async function submitReservation(e) {
        e.preventDefault();
        clearFieldErrors();
        clearAlert("formAlert");

        const isExisting = document.getElementById("modeExisting").checked;
        const payload = {
            customerId: isExisting ? Number(document.getElementById("customerSelect").value) : null,
            customerFullName: isExisting ? null : document.getElementById("customerFullName").value.trim(),
            customerAddress: isExisting ? null : document.getElementById("customerAddress").value.trim(),
            customerContactNumber: isExisting ? null : document.getElementById("customerContactNumber").value.trim(),
            customerEmail: isExisting ? null : (document.getElementById("customerEmail").value.trim() || null),
            customerLicenseNumber: isExisting ? null : (document.getElementById("customerLicenseNumber").value.trim() || null),
            vehicleId: Number(document.getElementById("vehicleSelect").value) || null,
            pickupDate: document.getElementById("pickupDate").value,
            pickupTime: document.getElementById("pickupTime").value ? document.getElementById("pickupTime").value + ":00" : null,
            returnDate: document.getElementById("returnDate").value,
            returnTime: document.getElementById("returnTime").value ? document.getElementById("returnTime").value + ":00" : null,
            notes: document.getElementById("notes").value.trim() || null
        };

        const btn = document.getElementById("submitReservationBtn");
        btn.disabled = true;
        btn.textContent = "Saving...";

        try {
            const reservation = await Api.post("/reservations", payload);
            window.location.href = "reservation-detail.html?number=" + encodeURIComponent(reservation.reservationNumber) + "&created=1";
        } catch (err) {
            if (err.status === 400 && err.body && err.body.validationErrors) {
                applyFieldErrors(err.body.validationErrors);
            } else if (err.status === 409) {
                showAlert("formAlert", err.body.message || "This vehicle is already booked for the selected dates.");
            } else if (err.status === 404) {
                showAlert("formAlert", err.body.message || "The selected vehicle or customer could not be found.");
            } else {
                showAlert("formAlert", "Could not save the reservation. Please check the form and try again.");
            }
        } finally {
            btn.disabled = false;
            btn.textContent = "Save Reservation";
        }
    }

    function applyFieldErrors(errors) {
        Object.entries(errors).forEach(([field, message]) => {
            const el = document.getElementById("err-" + field);
            if (el) el.textContent = message;
        });
    }

    function clearFieldErrors() {
        document.querySelectorAll(".field-error").forEach(el => el.textContent = "");
    }

    async function loadReservations() {
        try {
            const reservations = await Api.get("/reservations");
            const body = document.getElementById("reservationsBody");
            if (reservations.length === 0) {
                body.innerHTML = `<tr><td colspan="7" class="text-muted">No reservations yet. Click "New Reservation" to create one.</td></tr>`;
                return;
            }
            body.innerHTML = reservations.slice().reverse().map(r => `
                <tr>
                    <td><a href="reservation-detail.html?number=${encodeURIComponent(r.reservationNumber)}">${r.reservationNumber}</a></td>
                    <td>${r.customer.fullName}</td>
                    <td>${r.vehicle.registrationNumber} - ${r.vehicle.make} ${r.vehicle.model}</td>
                    <td>${formatDate(r.pickupDate)} ${r.pickupTime ? r.pickupTime.substring(0,5) : ""}</td>
                    <td>${formatDate(r.returnDate)} ${r.returnTime ? r.returnTime.substring(0,5) : ""}</td>
                    <td><span class="badge bg-secondary status-badge">${r.status}</span></td>
                    <td><a class="btn btn-sm btn-outline-brand" href="reservation-detail.html?number=${encodeURIComponent(r.reservationNumber)}">View</a></td>
                </tr>`).join("");
        } catch (err) {
            showAlert("alertBox", "Could not load reservations.");
        }
    }
})();
