(async () => {
    const user = await requireAuth();
    if (!user) return;

    const params = new URLSearchParams(location.search);
    const number = params.get("number");
    if (!number) {
        showAlert("alertBox", "No reservation number was supplied.");
        return;
    }

    let bill;
    try {
        bill = await Api.get("/bills/reservation/" + encodeURIComponent(number));
        render(bill);
    } catch (err) {
        showAlert("alertBox", err.body && err.body.message ? err.body.message : "Could not calculate the bill for this reservation.");
        return;
    }

    document.getElementById("settleBtn").addEventListener("click", async () => {
        if (bill.paymentStatus === "PAID") {
            alert("This bill is already marked as paid.");
            return;
        }
        const method = prompt("Payment method (e.g. Cash, Card, Bank Transfer):", "Cash");
        if (!method) return;
        try {
            bill = await Api.post("/bills/" + encodeURIComponent(bill.billNumber) + "/settle", { paymentMethod: method });
            render(bill);
        } catch (err) {
            alert("Could not record the payment.");
        }
    });

    function render(bill) {
        document.getElementById("receiptBox").style.display = "";
        document.getElementById("billNumber").textContent = bill.billNumber;
        document.getElementById("resNumber").textContent = bill.reservation.reservationNumber;
        document.getElementById("generatedAt").textContent = formatDateTime(bill.generatedAt);

        const statusEl = document.getElementById("paymentStatus");
        statusEl.textContent = bill.paymentStatus;
        statusEl.className = "badge status-badge " + (bill.paymentStatus === "PAID" ? "bg-success" : "bg-warning text-dark");
        document.getElementById("pricingStrategy").textContent = bill.pricingStrategy.replace(/_/g, " ");

        const c = bill.reservation.customer;
        document.getElementById("customerBlock").innerHTML = `${c.fullName}<br>${c.address}<br>${c.contactNumber}`;

        const v = bill.reservation.vehicle;
        document.getElementById("vehicleBlock").innerHTML = `${v.registrationNumber}<br>${v.make} ${v.model}<br>${v.category.categoryName}`;

        document.getElementById("numDays").textContent = bill.numberOfDays;
        document.getElementById("dailyRate").textContent = formatCurrency(bill.dailyRate);
        document.getElementById("subtotal").textContent = formatCurrency(bill.subtotal);
        document.getElementById("surcharge").textContent = bill.surchargeAmount > 0 ? formatCurrency(bill.surchargeAmount) : "-";
        document.getElementById("discount").textContent = bill.discountAmount > 0 ? "- " + formatCurrency(bill.discountAmount) : "-";
        document.getElementById("tax").textContent = formatCurrency(bill.taxAmount);
        document.getElementById("total").textContent = formatCurrency(bill.totalAmount);

        document.getElementById("settleBtn").textContent = bill.paymentStatus === "PAID"
            ? `Paid via ${bill.paymentMethod || "N/A"}` : "Mark as Paid";
    }
})();
