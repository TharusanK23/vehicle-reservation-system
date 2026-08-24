(async () => {
    const user = await requireAuth();
    if (!user) return;

    try {
        const summary = await Api.get("/reports/dashboard");
        document.getElementById("kpiTotalVehicles").textContent = summary.totalVehicles;
        document.getElementById("kpiAvailableVehicles").textContent = summary.availableVehicles;
        document.getElementById("kpiActiveReservations").textContent = summary.activeReservations;
        document.getElementById("kpiTodaysPickups").textContent = summary.todaysPickups;
        document.getElementById("kpiTotalCustomers").textContent = summary.totalCustomers;
        document.getElementById("kpiTotalRevenue").textContent = formatCurrency(summary.totalRevenue);
        document.getElementById("kpiUnpaidAmount").textContent = formatCurrency(summary.unpaidAmount);
    } catch (err) {
        showAlert("alertBox", "Could not load dashboard summary. Make sure database/schema.sql has been imported (it provides the stored procedure/view the dashboard depends on).");
    }

    try {
        const reservations = await Api.get("/reservations");
        const recent = reservations.slice(-5).reverse();
        const body = document.getElementById("recentReservationsBody");
        if (recent.length === 0) {
            body.innerHTML = `<tr><td colspan="4" class="text-muted">No reservations yet.</td></tr>`;
        } else {
            body.innerHTML = recent.map(r => `
                <tr>
                    <td><a href="reservation-detail.html?number=${encodeURIComponent(r.reservationNumber)}">${r.reservationNumber}</a></td>
                    <td>${r.customer.fullName}</td>
                    <td>${r.vehicle.make} ${r.vehicle.model}</td>
                    <td><span class="badge bg-secondary status-badge">${r.status}</span></td>
                </tr>`).join("");
        }
    } catch (err) {
        /* dashboard KPIs already reported the connectivity problem, if any */
    }
})();
