(async () => {
    const user = await requireAuth();
    if (!user) return;

    await loadCustomers();

    document.getElementById("searchForm").addEventListener("submit", (e) => {
        e.preventDefault();
        loadCustomers(document.getElementById("searchName").value.trim());
    });
    document.getElementById("resetSearch").addEventListener("click", () => {
        document.getElementById("searchName").value = "";
        loadCustomers();
    });

    async function loadCustomers(search) {
        try {
            const customers = await Api.get("/customers", search ? { search } : undefined);
            const body = document.getElementById("customersBody");
            body.innerHTML = customers.map(c => `
                <tr>
                    <td>${c.fullName}</td>
                    <td>${c.address}</td>
                    <td>${c.contactNumber}</td>
                    <td>${c.email || "-"}</td>
                    <td>${c.licenseNumber || "-"}</td>
                </tr>`).join("") || `<tr><td colspan="5" class="text-muted">No customers found.</td></tr>`;
        } catch (err) {
            showAlert("alertBox", "Could not load customers.");
        }
    }
})();
