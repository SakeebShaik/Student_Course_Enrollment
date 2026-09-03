/**
 * STUDENT COURSE MANAGEMENT SYSTEM
 * Common Utilities, Theme Manager & Authentication Service
 */

// Storage Keys
const TOKEN_KEY = "scm_auth_token";
const THEME_KEY = "scm_app_theme";

/**
 * ==========================================================================
 * THEME MANAGER (DARK / LIGHT MODE)
 * ==========================================================================
 */

function initTheme() {
    const savedTheme = localStorage.getItem(THEME_KEY) || "light";
    setTheme(savedTheme);
}

function setTheme(theme) {
    if (theme === "dark") {
        document.documentElement.setAttribute("data-theme", "dark");
        localStorage.setItem(THEME_KEY, "dark");
    } else {
        document.documentElement.removeAttribute("data-theme");
        localStorage.setItem(THEME_KEY, "light");
    }
    updateThemeToggleIcons(theme);
}

function toggleTheme() {
    const currentTheme = localStorage.getItem(THEME_KEY) === "dark" ? "light" : "dark";
    setTheme(currentTheme);
}

function updateThemeToggleIcons(theme) {
    document.querySelectorAll(".theme-toggle-btn").forEach(btn => {
        btn.innerHTML = theme === "dark" ? "☀️" : "🌙";
        btn.setAttribute("title", theme === "dark" ? "Switch to Light Mode" : "Switch to Dark Mode");
    });
}

// Automatically initialize theme as early as possible
initTheme();

/**
 * ==========================================================================
 * AUTHENTICATION & TOKEN MANAGEMENT
 * ==========================================================================
 */

function getToken() {
    return localStorage.getItem(TOKEN_KEY) || localStorage.getItem("token");
}

function saveToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem("token", token);
}

function removeToken() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem("token");
}

function parseJwt(token) {
    if (!token) return null;
    try {
        const base64Url = token.split('.')[1];
        if (!base64Url) return null;
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error("Error parsing JWT:", e);
        return null;
    }
}

function getCurrentUser() {
    const token = getToken();
    if (!token) return null;

    const payload = parseJwt(token);
    if (!payload) return null;

    // Expiry check
    if (payload.exp && Date.now() >= payload.exp * 1000) {
        logout(true);
        return null;
    }

    let roles = [];
    if (Array.isArray(payload.role)) {
        roles = payload.role.map(r => typeof r === 'object' && r.authority ? r.authority : String(r));
    } else if (typeof payload.role === 'string') {
        roles = [payload.role];
    }

    const isAdmin = roles.some(r => r.toUpperCase() === "ROLE_ADMIN" || r.toUpperCase() === "ADMIN");
    const isStudent = roles.some(r => r.toUpperCase() === "ROLE_STUDENT" || r.toUpperCase() === "STUDENT");

    return {
        username: payload.sub || "User",
        roles: roles,
        isAdmin: isAdmin,
        isStudent: isStudent,
        roleName: isAdmin ? "ADMIN" : "STUDENT"
    };
}

function checkLogin(requiredRole = null) {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = "login.html";
        return null;
    }

    if (requiredRole) {
        const req = requiredRole.toUpperCase();
        if (req === "ADMIN" && !user.isAdmin) {
            showToast("Access Denied", "Administrator privilege required.", "error");
            window.location.href = "dashboard.html";
            return null;
        }
    }

    return user;
}

function logout(isExpired = false) {
    removeToken();
    window.location.href = isExpired ? "login.html?expired=1" : "login.html?logged_out=1";
}

function getAuthHeaders() {
    const token = getToken();
    return {
        "Authorization": token ? "Bearer " + token : "",
        "Content-Type": "application/json",
        "Accept": "application/json"
    };
}

async function apiFetch(url, options = {}) {
    const headers = {
        ...getAuthHeaders(),
        ...(options.headers || {})
    };

    try {
        const response = await fetch(url, { ...options, headers });

        if (response.status === 401) {
            showToast("Session Expired", "Please sign in again.", "warning");
            setTimeout(() => logout(true), 1000);
            throw new Error("Unauthorized - Session Expired");
        }

        if (response.status === 204) {
            return { ok: true, data: null };
        }

        let data = null;
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            data = await response.json();
        } else {
            const text = await response.text();
            data = text ? { message: text } : null;
        }

        if (!response.ok) {
            const msg = (data && data.message) ? data.message : `Server returned status ${response.status}`;
            const error = new Error(msg);
            error.status = response.status;
            error.data = data;
            throw error;
        }

        return { ok: true, data: data };
    } catch (err) {
        console.error("API Error:", err);
        throw err;
    }
}

/**
 * ==========================================================================
 * TOAST NOTIFICATIONS & MODALS
 * ==========================================================================
 */

function showToast(title, message, type = "info", duration = 3500) {
    let container = document.getElementById("toast-container");
    if (!container) {
        container = document.createElement("div");
        container.id = "toast-container";
        document.body.appendChild(container);
    }

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;

    let icon = "ℹ️";
    if (type === "success") icon = "✓";
    if (type === "error") icon = "✕";
    if (type === "warning") icon = "⚠";

    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-content">
            <div class="toast-title">${escapeHtml(title)}</div>
            <div class="toast-message">${escapeHtml(message)}</div>
        </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = "0";
        setTimeout(() => toast.remove(), 250);
    }, duration);
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add("active");
        document.body.style.overflow = "hidden";
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove("active");
        document.body.style.overflow = "";
    }
}

document.addEventListener("click", (e) => {
    if (e.target.classList.contains("modal-overlay")) {
        e.target.classList.remove("active");
        document.body.style.overflow = "";
    }
});

document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
        document.querySelectorAll(".modal-overlay.active").forEach(m => m.classList.remove("active"));
        document.body.style.overflow = "";
    }
});

function escapeHtml(str) {
    if (!str) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}