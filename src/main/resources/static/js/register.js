/**
 * REGISTRATION CONTROLLER
 */

document.addEventListener("DOMContentLoaded", () => {
    initTheme();

    const user = getCurrentUser();
    if (user) {
        window.location.href = "dashboard.html";
        return;
    }

    const registerForm = document.getElementById("registerForm");
    const registerBtn = document.getElementById("registerBtn");
    const message = document.getElementById("message");
    const usernameInput = document.getElementById("username");
    const nameInput = document.getElementById("name");
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirmPassword");

    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const username = usernameInput.value.trim();
        const name = nameInput.value.trim();
        const email = emailInput.value.trim().toLowerCase();
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        if (!/^[A-Za-z0-9._-]{3,50}$/.test(username)) {
            showToast("Validation Error", "Username must be 3-50 characters and contain only letters, numbers, dots, underscores or hyphens.", "warning");
            return;
        }

        if (!/^[A-Za-z][A-Za-z .'-]{1,99}$/.test(name)) {
            showToast("Validation Error", "Please enter a valid full name.", "warning");
            return;
        }

        if (!emailInput.checkValidity()) {
            showToast("Validation Error", "Please enter a valid email address.", "warning");
            return;
        }

        if (password.length < 6 || password.length > 100) {
            showToast("Validation Error", "Password must be between 6 and 100 characters.", "warning");
            return;
        }

        if (password !== confirmPassword) {
            showToast("Validation Error", "Passwords do not match.", "error");
            return;
        }

        registerBtn.disabled = true;
        const originalBtnText = registerBtn.innerHTML;
        registerBtn.innerHTML = `<span class="spinner"></span> Creating Account...`;
        message.textContent = "";

        try {
            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify({ username, password, name, email })
            });

            const data = await response.json();

            if (response.ok) {
                showToast("Account Created", "Registration successful. Redirecting to login...", "success");
                message.innerHTML = `<div class="badge badge-success" style="width: 100%; padding: 0.5rem; justify-content: center;">Account created successfully! Redirecting...</div>`;
                registerForm.reset();

                setTimeout(() => {
                    window.location.href = "login.html?registered=1";
                }, 1000);
            } else {
                const errorMsg = data.message || "Registration failed.";
                message.innerHTML = `<div class="badge badge-danger" style="width: 100%; padding: 0.5rem; justify-content: center;">${escapeHtml(errorMsg)}</div>`;
                showToast("Registration Failed", errorMsg, "error");
                registerBtn.disabled = false;
                registerBtn.innerHTML = originalBtnText;
            }
        } catch (error) {
            console.error("Registration error:", error);
            const netError = "Unable to connect to server. Please check the backend.";
            message.innerHTML = `<div class="badge badge-danger" style="width: 100%; padding: 0.5rem; justify-content: center;">${escapeHtml(netError)}</div>`;
            showToast("Connection Error", netError, "error");
            registerBtn.disabled = false;
            registerBtn.innerHTML = originalBtnText;
        }
    });
});
