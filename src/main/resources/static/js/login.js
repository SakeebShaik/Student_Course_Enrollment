/**
 * LOGIN CONTROLLER
 */

document.addEventListener("DOMContentLoaded", () => {
    initTheme();

    const user = getCurrentUser();
    if (user) {
        window.location.href = "dashboard.html";
        return;
    }

    const loginForm = document.getElementById("loginForm");
    const loginBtn = document.getElementById("loginBtn");
    const message = document.getElementById("message");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get("logged_out")) {
        showToast("Signed Out", "You have been logged out.", "info");
    }
    if (urlParams.get("expired")) {
        showToast("Session Expired", "Your session timed out. Please sign in again.", "warning");
    }
    if (urlParams.get("registered")) {
        showToast("Account Created", "Registration successful. Please sign in.", "success");
    }

    window.togglePassword = function() {
        const type = passwordInput.type === "password" ? "text" : "password";
        passwordInput.type = type;
        const icon = document.getElementById("togglePasswordIcon");
        if (icon) icon.textContent = type === "password" ? "👁️" : "🙈";
    };

    loginForm.addEventListener("submit", async event => {
        event.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value;

        if (!username || !password) {
            showToast("Validation Error", "Please enter both username and password.", "error");
            return;
        }

        loginBtn.disabled = true;
        const originalText = loginBtn.innerHTML;
        loginBtn.innerHTML = `<span class="spinner"></span> Signing in...`;
        message.textContent = "";

        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify({ username, password })
            });

            const data = await response.json();

            if (response.ok && data.token) {
                saveToken(data.token);
                const loggedUser = getCurrentUser();
                const roleLabel = loggedUser && loggedUser.isAdmin ? "Administrator" : "Student";
                showToast("Welcome", `Signed in as ${roleLabel}.`, "success");

                setTimeout(() => {
                    window.location.href = "dashboard.html";
                }, 300);
            } else {
                const errorMsg = data.message || "Invalid username or password.";
                message.innerHTML = `<div class="badge badge-danger" style="width:100%;padding:.5rem;justify-content:center;">${escapeHtml(errorMsg)}</div>`;
                showToast("Login Failed", errorMsg, "error");
                loginBtn.disabled = false;
                loginBtn.innerHTML = originalText;
            }
        } catch (error) {
            console.error("Login request failed:", error);
            const netError = "Unable to connect to server. Please check the backend.";
            message.innerHTML = `<div class="badge badge-danger" style="width:100%;padding:.5rem;justify-content:center;">${escapeHtml(netError)}</div>`;
            showToast("Connection Error", netError, "error");
            loginBtn.disabled = false;
            loginBtn.innerHTML = originalText;
        }
    });
});
