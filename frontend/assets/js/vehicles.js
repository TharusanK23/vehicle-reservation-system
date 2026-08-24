(async () => {
    const user = await requireAuth();
    if (!user) return;

    await loadCategories();
    await loadVehicles();

    const vehicleForm = document.getElementById("vehicleForm");
    if (vehicleForm) vehicleForm.addEventListener("submit", addVehicle);

    const categoryForm = document.getElementById("categoryForm");
    if (categoryForm) categoryForm.addEventListener("submit", addCategory);

    async function loadCategories() {
        try {
            const categories = await Api.get("/vehicle-categories");
            const body = document.getElementById("categoriesBody");
            body.innerHTML = categories.map(c => `
                <tr><td>${c.categoryName}</td><td>${formatCurrency(c.dailyRate)}</td><td>${c.description || "-"}</td></tr>`).join("")
                || `<tr><td colspan="3" class="text-muted">No categories yet.</td></tr>`;

            const select = document.getElementById("categoryId");
            if (select) select.innerHTML = categories.map(c => `<option value="${c.id}">${c.categoryName}</option>`).join("");
        } catch (err) {
            showAlert("alertBox", "Could not load vehicle categories.");
        }
    }

    async function loadVehicles() {
        try {
            const vehicles = await Api.get("/vehicles");
            const body = document.getElementById("vehiclesBody");
            body.innerHTML = vehicles.map(v => `
                <tr>
                    <td>${v.registrationNumber}</td>
                    <td>${v.make} ${v.model}</td>
                    <td>${v.manufactureYear}</td>
                    <td>${v.category.categoryName}</td>
                    <td><span class="badge status-badge ${statusBadgeClass(v.status)}">${v.status}</span></td>
                    <td data-role-admin-only><button class="btn btn-sm btn-outline-danger" data-id="${v.id}">Delete</button></td>
                </tr>`).join("") || `<tr><td colspan="6" class="text-muted">No vehicles yet.</td></tr>`;

            body.querySelectorAll("button[data-id]").forEach(btn => {
                btn.addEventListener("click", () => deleteVehicle(btn.dataset.id));
            });

            if (user.role !== "ADMIN") {
                document.querySelectorAll("[data-role-admin-only]").forEach(el => el.style.display = "none");
            }
        } catch (err) {
            showAlert("alertBox", "Could not load vehicles.");
        }
    }

    function statusBadgeClass(status) {
        switch (status) {
            case "AVAILABLE": return "bg-success";
            case "RESERVED": return "bg-warning text-dark";
            case "MAINTENANCE": return "bg-secondary";
            default: return "bg-dark";
        }
    }

    async function deleteVehicle(id) {
        if (!confirm("Delete this vehicle? This cannot be undone.")) return;
        try {
            await Api.del("/vehicles/" + id);
            await loadVehicles();
        } catch (err) {
            alert("Could not delete this vehicle (it may have existing reservations).");
        }
    }

    async function addVehicle(e) {
        e.preventDefault();
        clearFieldErrors();
        clearAlert("vehicleFormAlert");
        const payload = {
            registrationNumber: document.getElementById("registrationNumber").value.trim(),
            make: document.getElementById("make").value.trim(),
            model: document.getElementById("model").value.trim(),
            manufactureYear: Number(document.getElementById("manufactureYear").value),
            categoryId: Number(document.getElementById("categoryId").value)
        };
        try {
            await Api.post("/vehicles", payload);
            document.getElementById("vehicleForm").reset();
            await loadVehicles();
        } catch (err) {
            if (err.status === 400 && err.body.validationErrors) applyFieldErrors(err.body.validationErrors);
            else showAlert("vehicleFormAlert", (err.body && err.body.message) || "Could not add the vehicle.");
        }
    }

    async function addCategory(e) {
        e.preventDefault();
        clearFieldErrors();
        clearAlert("categoryFormAlert");
        const payload = {
            categoryName: document.getElementById("categoryName").value.trim(),
            dailyRate: Number(document.getElementById("dailyRate").value),
            description: document.getElementById("description").value.trim() || null
        };
        try {
            await Api.post("/vehicle-categories", payload);
            document.getElementById("categoryForm").reset();
            await loadCategories();
        } catch (err) {
            if (err.status === 400 && err.body.validationErrors) applyFieldErrors(err.body.validationErrors);
            else showAlert("categoryFormAlert", (err.body && err.body.message) || "Could not add the category.");
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
})();
