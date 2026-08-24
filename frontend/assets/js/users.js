(async () => {
    const user = await requireAuth();
    if (!user) return;

    if (user.role !== "ADMIN") {
        document.getElementById("accessDenied").classList.remove("d-none");
        return;
    }
    document.getElementById("adminContent").classList.remove("d-none");

    await loadUsers();
    document.getElementById("userForm").addEventListener("submit", createUser);

    async function loadUsers() {
        try {
            const users = await Api.get("/users");
            const body = document.getElementById("usersBody");
            body.innerHTML = users.map(u => `
                <tr>
                    <td>${u.username}</td>
                    <td>${u.fullName}</td>
                    <td>${u.email}</td>
                    <td><span class="badge bg-secondary status-badge">${u.role}</span></td>
                    <td><span class="badge status-badge ${u.enabled !== false ? "bg-success" : "bg-secondary"}">${u.enabled !== false ? "Active" : "Disabled"}</span></td>
                    <td><button class="btn btn-sm btn-outline-danger" data-id="${u.id}">Deactivate</button></td>
                </tr>`).join("");

            body.querySelectorAll("button[data-id]").forEach(btn => {
                btn.addEventListener("click", async () => {
                    if (!confirm("Deactivate this staff account?")) return;
                    try {
                        await Api.patch("/users/" + btn.dataset.id + "/deactivate");
                        await loadUsers();
                    } catch (err) {
                        alert("Could not deactivate this account.");
                    }
                });
            });
        } catch (err) {
            showAlert("alertBox", "Could not load staff accounts.");
        }
    }

    async function createUser(e) {
        e.preventDefault();
        document.querySelectorAll(".field-error").forEach(el => el.textContent = "");
        clearAlert("formAlert");

        const payload = {
            username: document.getElementById("username").value.trim(),
            password: document.getElementById("password").value,
            fullName: document.getElementById("fullName").value.trim(),
            email: document.getElementById("email").value.trim(),
            role: document.getElementById("role").value
        };

        try {
            await Api.post("/users", payload);
            document.getElementById("userForm").reset();
            await loadUsers();
        } catch (err) {
            if (err.status === 400 && err.body.validationErrors) {
                Object.entries(err.body.validationErrors).forEach(([field, message]) => {
                    const el = document.getElementById("err-" + field);
                    if (el) el.textContent = message;
                });
            } else {
                showAlert("formAlert", (err.body && err.body.message) || "Could not create the staff account.");
            }
        }
    }
})();
