(async () => {
    const user = await requireAuth();
    if (!user) return;

    const params = new URLSearchParams(location.search);
    const number = params.get("number");
    if (!number) {
        showAlert("alertBox", "No reservation number was supplied.");
        return;
    }

    if (params.get("created") === "1") {
        showAlert("alertBox", "Reservation " + number + " was created successfully.", "success");
    }

    try {
        const r = await Api.get("/reservations/" + encodeURIComponent(number));
        renderReservation(r);
    } catch (err) {
        if (err.status === 404) {
            showAlert("alertBox", "No reservation found with number " + number + ".");
        } else {
            showAlert("alertBox", "Could not load the reservation.");
        }
        return;
    }

    function renderReservation(r) {
        document.getElementById("detailCard").style.display = "";
        document.getElementById("resNumber").textContent = r.reservationNumber;
        document.getElementById("resStatus").textContent = r.status;

        document.getElementById("custName").textContent = r.customer.fullName;
        document.getElementById("custAddress").textContent = r.customer.address;
        document.getElementById("custContact").textContent = r.customer.contactNumber;
        document.getElementById("custEmail").textContent = r.customer.email || "-";

        document.getElementById("vehReg").textContent = r.vehicle.registrationNumber;
        document.getElementById("vehMakeModel").textContent = `${r.vehicle.make} ${r.vehicle.model} (${r.vehicle.manufactureYear})`;
        document.getElementById("vehCategory").textContent = r.vehicle.category.categoryName;
        document.getElementById("vehRate").textContent = formatCurrency(r.vehicle.category.dailyRate) + " / day";

        document.getElementById("pickupInfo").textContent = `${formatDate(r.pickupDate)} at ${r.pickupTime.substring(0,5)}`;
        document.getElementById("returnInfo").textContent = `${formatDate(r.returnDate)} at ${r.returnTime.substring(0,5)}`;
        document.getElementById("notesInfo").textContent = r.notes || "-";

        document.getElementById("createdBy").textContent = r.createdByUsername;
        document.getElementById("createdAt").textContent = formatDateTime(r.createdAt);

        document.getElementById("generateBillBtn").href = "billing.html?number=" + encodeURIComponent(r.reservationNumber);

        const cancelBtn = document.getElementById("cancelBtn");
        if (r.status === "CANCELLED" || r.status === "COMPLETED") {
            cancelBtn.disabled = true;
        }
        cancelBtn.addEventListener("click", async () => {
            if (!confirm("Cancel reservation " + r.reservationNumber + "? This will free up the vehicle.")) return;
            try {
                await Api.post("/reservations/" + encodeURIComponent(r.reservationNumber) + "/cancel");
                window.location.reload();
            } catch (err) {
                showAlert("alertBox", "Could not cancel the reservation.");
            }
        });
    }
})();
