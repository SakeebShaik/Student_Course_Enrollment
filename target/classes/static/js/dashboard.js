/**
 * Dashboard controller for the student and admin workspaces.
 */

const state = {
    currentUser: null,
    currentStudent: null,
    courses: [],
    students: [],
    enrollments: [],
    studentEnrolledCourses: [],
    activeTab: "mycourses",
    activeAdminView: "overview"
};

document.addEventListener("DOMContentLoaded", async () => {
    initTheme();

    state.currentUser = checkLogin();
    if (!state.currentUser) return;

    initShellUI();

    if (state.currentUser.isAdmin) {
        await initAdminWorkspace();
    } else if (state.currentUser.isStudent) {
        await initStudentWorkspace();
    } else {
        logout();
    }
});

function initShellUI() {
    const user = state.currentUser;
    const initial = user.username ? user.username.charAt(0).toUpperCase() : "U";

    document.querySelectorAll(".dyn-user-initial").forEach(el => el.textContent = initial);
    document.querySelectorAll(".dyn-user-name").forEach(el => el.textContent = user.username || "User");
    document.querySelectorAll(".dyn-user-role").forEach(el => {
        el.textContent = user.isAdmin ? "Administrator" : "Student";
    });

    const roleBadge = document.getElementById("headerRoleBadge");
    if (roleBadge) {
        roleBadge.className = user.isAdmin ? "badge badge-primary" : "badge badge-success";
        roleBadge.textContent = user.isAdmin ? "Administrator" : "Student";
    }

    const mobileToggle = document.getElementById("mobileNavToggle");
    const sidebar = document.getElementById("adminSidebar");
    if (mobileToggle && sidebar) {
        mobileToggle.addEventListener("click", () => {
            sidebar.style.display = sidebar.style.display === "none" ? "flex" : "none";
        });
    }
}

/* --------------------------------------------------------------------------
   STUDENT WORKSPACE
   -------------------------------------------------------------------------- */

async function initStudentWorkspace() {
    const studentArea = document.getElementById("studentWorkspace");
    const adminArea = document.getElementById("adminWorkspace");
    if (studentArea) studentArea.style.display = "block";
    if (adminArea) adminArea.style.display = "none";

    setupStudentTabs();
    await loadStudentData();
    switchStudentTab("mycourses");
}

function setupStudentTabs() {
    document.querySelectorAll(".student-tab-btn").forEach(btn => {
        btn.addEventListener("click", () => switchStudentTab(btn.dataset.tab));
    });

    const search = document.getElementById("catalogSearchInput");
    if (search) {
        search.addEventListener("input", event => {
            renderStudentCatalog(event.target.value.trim().toLowerCase());
        });
    }
}

function switchStudentTab(tabName) {
    state.activeTab = tabName;

    document.querySelectorAll(".student-tab-btn").forEach(btn => {
        btn.classList.toggle("active", btn.dataset.tab === tabName);
    });

    document.querySelectorAll(".student-view-tab").forEach(tab => {
        tab.classList.remove("active");
        tab.style.display = "none";
    });

    const target = document.getElementById(`studentTab_${tabName}`);
    if (target) {
        target.classList.add("active");
        target.style.display = "block";
    }

    if (tabName === "catalog") renderStudentCatalog();
    if (tabName === "profile") renderStudentProfile();
    if (tabName === "mycourses") renderStudentEnrolledCourses();
}

async function loadStudentData() {
    try {
        const [studentRes, coursesRes, enrolledRes] = await Promise.all([
            apiFetch("/api/students/me"),
            apiFetch("/api/courses"),
            apiFetch("/api/students/me/courses")
        ]);

        state.currentStudent = studentRes.data;
        state.courses = coursesRes.data || [];
        state.studentEnrolledCourses = enrolledRes.data || [];

        renderStudentProfile();
        renderStudentStats();
        renderStudentEnrolledCourses();
        renderStudentCatalog();
    } catch (error) {
        console.error("Error loading student data:", error);
        showToast("Load Error", error.message || "Failed to load student data.", "error");
    }
}

function renderStudentStats() {
    const enrolledCount = state.studentEnrolledCourses.length;
    const totalCredits = state.studentEnrolledCourses.reduce((sum, course) => sum + (course.credits || 0), 0);

    const enrolledEl = document.getElementById("studentStatEnrolledCount");
    const creditsEl = document.getElementById("studentStatCredits");
    const catalogEl = document.getElementById("studentStatCatalogCount");

    if (enrolledEl) enrolledEl.textContent = enrolledCount;
    if (creditsEl) creditsEl.textContent = totalCredits;
    if (catalogEl) catalogEl.textContent = state.courses.length;
}

function renderStudentEnrolledCourses() {
    const container = document.getElementById("studentEnrolledCoursesGrid");
    const emptyState = document.getElementById("studentEnrolledEmpty");
    if (!container) return;

    if (state.studentEnrolledCourses.length === 0) {
        container.innerHTML = "";
        if (emptyState) emptyState.style.display = "block";
        return;
    }

    if (emptyState) emptyState.style.display = "none";

    container.innerHTML = state.studentEnrolledCourses.map(course => `
        <div class="course-card">
            <div class="course-card-header">
                <span class="course-code-badge">ID #${course.id}</span>
                <span class="badge badge-success">Enrolled</span>
            </div>
            <h3 class="course-title">${escapeHtml(course.name)}</h3>
            <div class="course-meta-row">
                <div><strong>${course.credits} Credits</strong></div>
                <div>Seats: ${course.enrolledCount}/${course.capacity}</div>
            </div>
            <div class="course-card-actions">
                <button class="btn btn-secondary btn-sm" style="flex: 1;" onclick="openCourseDetailsModal(${course.id})">
                    View Details
                </button>
            </div>
        </div>
    `).join("");
}

function renderStudentCatalog(filterText = "") {
    const container = document.getElementById("studentCatalogGrid");
    if (!container) return;

    const enrolledIds = new Set(state.studentEnrolledCourses.map(course => course.id));
    const filtered = state.courses.filter(course => {
        if (!filterText) return true;
        return (course.name || "").toLowerCase().includes(filterText)
            || String(course.credits).includes(filterText)
            || String(course.id).includes(filterText);
    });

    if (filtered.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-icon">🔎</div>
                <div class="empty-title">No Courses Available</div>
                <p class="empty-desc">No courses match your search.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = filtered.map(course => {
        const isEnrolled = enrolledIds.has(course.id);
        const isFull = course.availableSeats <= 0;
        const status = isFull ? "Full" : `${course.availableSeats} seat${course.availableSeats === 1 ? "" : "s"} available`;
        const statusClass = isFull ? "badge-danger" : "badge-success";
        const progress = course.capacity > 0
            ? Math.min(100, Math.round((course.enrolledCount / course.capacity) * 100))
            : 100;

        let actionButton;
        if (isEnrolled) {
            actionButton = `<button class="btn btn-secondary btn-sm" disabled style="flex: 1;">Enrolled</button>`;
        } else if (isFull) {
            actionButton = `<button class="btn btn-secondary btn-sm" disabled style="flex: 1;">Course Full</button>`;
        } else {
            actionButton = `<button class="btn btn-primary btn-sm" style="flex: 1;" onclick="enrollCurrentStudent(${course.id})">Enroll</button>`;
        }

        return `
            <div class="course-card">
                <div class="course-card-header">
                    <span class="course-code-badge">ID #${course.id}</span>
                    <span class="badge badge-primary">${course.credits} Credits</span>
                </div>
                <h3 class="course-title">${escapeHtml(course.name)}</h3>
                <div class="course-meta-row">
                    <div>Capacity: <strong>${course.capacity} Seats</strong></div>
                    <div>${course.enrolledCount}/${course.capacity}</div>
                </div>
                <div class="capacity-tracker">
                    <div class="capacity-info">
                        <span>Status</span>
                        <span class="badge ${statusClass}">${status}</span>
                    </div>
                    <div class="capacity-bar-bg">
                        <div class="capacity-bar-fill" style="width: ${progress}%;"></div>
                    </div>
                </div>
                <div class="course-card-actions">
                    ${actionButton}
                    <button class="btn btn-secondary btn-sm" onclick="openCourseDetailsModal(${course.id})">Details</button>
                </div>
            </div>
        `;
    }).join("");
}

function renderStudentProfile() {
    const student = state.currentStudent;
    if (!student) return;

    const idEl = document.getElementById("profileStudentId");
    const nameEl = document.getElementById("profileStudentName");
    const emailEl = document.getElementById("profileStudentEmail");

    if (idEl) idEl.textContent = `#${student.id}`;
    if (nameEl) nameEl.textContent = student.name;
    if (emailEl) emailEl.textContent = student.email;
}

window.enrollCurrentStudent = async function(courseId) {
    try {
        const course = state.courses.find(item => item.id === courseId);
        if (!course) return;

        const res = await apiFetch(`/api/enrollments/me/${courseId}`, { method: "POST" });
        if (res.ok) {
            showToast("Success", `Enrolled in "${course.name}" successfully.`, "success");
            await loadStudentData();
            switchStudentTab("catalog");
        }
    } catch (error) {
        showToast("Enrollment Error", error.message || "Enrollment failed.", "error");
    }
};

window.switchToCatalogTab = function() {
    switchStudentTab("catalog");
};

/* --------------------------------------------------------------------------
   ADMIN WORKSPACE
   -------------------------------------------------------------------------- */

async function initAdminWorkspace() {
    const studentArea = document.getElementById("studentWorkspace");
    const adminArea = document.getElementById("adminWorkspace");
    if (studentArea) studentArea.style.display = "none";
    if (adminArea) adminArea.style.display = "flex";

    setupAdminNavigation();
    await loadAdminData();
    switchAdminView("overview");
}

function setupAdminNavigation() {
    document.querySelectorAll(".admin-nav-btn").forEach(btn => {
        btn.addEventListener("click", () => switchAdminView(btn.dataset.view));
    });

    const courseSearch = document.getElementById("adminCourseSearch");
    if (courseSearch) {
        courseSearch.addEventListener("input", event => {
            renderAdminCoursesTable(event.target.value.trim().toLowerCase());
        });
    }

    const studentSearch = document.getElementById("adminStudentSearch");
    if (studentSearch) {
        studentSearch.addEventListener("input", event => {
            renderAdminStudentsTable(event.target.value.trim().toLowerCase());
        });
    }

    setupAdminForms();
}

function switchAdminView(viewName) {
    state.activeAdminView = viewName;

    document.querySelectorAll(".admin-nav-btn").forEach(btn => {
        btn.classList.toggle("active", btn.dataset.view === viewName);
    });

    document.querySelectorAll(".admin-view-section").forEach(section => {
        section.classList.remove("active");
        section.style.display = "none";
    });

    const target = document.getElementById(`adminView_${viewName}`);
    if (target) {
        target.classList.add("active");
        target.style.display = "block";
    }
}

window.switchAdminView = switchAdminView;

async function loadAdminData() {
    try {
        const [coursesRes, studentsRes, enrollmentsRes] = await Promise.all([
            apiFetch("/api/courses"),
            apiFetch("/api/students"),
            apiFetch("/api/enrollments")
        ]);

        state.courses = coursesRes.data || [];
        state.students = studentsRes.data || [];
        state.enrollments = enrollmentsRes.data || [];

        renderAdminKPIs();
        renderAdminCoursesTable();
        renderAdminStudentsTable();
        renderAdminEnrollmentsHub();
        populateEnrollmentDropdowns();
    } catch (error) {
        console.error("Error loading admin data:", error);
        showToast("Load Error", error.message || "Failed to load records.", "error");
    }
}

function renderAdminKPIs() {
    const totalCapacity = state.courses.reduce((sum, course) => sum + (course.capacity || 0), 0);

    const studentsEl = document.getElementById("kpiTotalStudents");
    const coursesEl = document.getElementById("kpiTotalCourses");
    const enrollmentsEl = document.getElementById("kpiTotalEnrollments");
    const capacityEl = document.getElementById("kpiTotalCapacity");

    if (studentsEl) studentsEl.textContent = state.students.length;
    if (coursesEl) coursesEl.textContent = state.courses.length;
    if (enrollmentsEl) enrollmentsEl.textContent = state.enrollments.length;
    if (capacityEl) capacityEl.textContent = totalCapacity;
}

function renderAdminCoursesTable(filterText = "") {
    const tbody = document.getElementById("adminCoursesTableBody");
    if (!tbody) return;

    const filtered = state.courses.filter(course => {
        if (!filterText) return true;
        return (course.name || "").toLowerCase().includes(filterText)
            || String(course.id).includes(filterText)
            || String(course.credits).includes(filterText);
    });

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:1.5rem;color:var(--text-muted);">No courses found.</td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map(course => `
        <tr>
            <td><strong>#${course.id}</strong></td>
            <td><strong>${escapeHtml(course.name)}</strong></td>
            <td><span class="badge badge-primary">${course.credits} Credits</span></td>
            <td>${course.enrolledCount}/${course.capacity} Seats</td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="openCourseDetailsModal(${course.id})">Details</button>
                <button class="btn btn-secondary btn-sm" onclick="editCourse(${course.id})">Edit</button>
                <button class="btn btn-danger btn-sm" onclick="deleteCourse(${course.id})">Delete</button>
            </td>
        </tr>
    `).join("");
}

function renderAdminStudentsTable(filterText = "") {
    const tbody = document.getElementById("adminStudentsTableBody");
    if (!tbody) return;

    const filtered = state.students.filter(student => {
        if (!filterText) return true;
        return (student.name || "").toLowerCase().includes(filterText)
            || (student.email || "").toLowerCase().includes(filterText)
            || String(student.id).includes(filterText);
    });

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:1.5rem;color:var(--text-muted);">No students found.</td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map(student => {
        const enrolledCount = state.enrollments.filter(e => e.studentId === student.id).length;
        return `
            <tr>
                <td><strong>#${student.id}</strong></td>
                <td><strong>${escapeHtml(student.name)}</strong></td>
                <td>${escapeHtml(student.email)}</td>
                <td><span class="badge badge-success">${enrolledCount} Courses</span></td>
                <td>
                    <button class="btn btn-secondary btn-sm" onclick="inspectStudentCoursesModal(${student.id}, '${escapeJsString(student.name)}')">View Courses</button>
                    <button class="btn btn-secondary btn-sm" onclick="editStudent(${student.id})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteStudent(${student.id})">Delete</button>
                </td>
            </tr>
        `;
    }).join("");
}

function renderAdminEnrollmentsHub() {
    const tbody = document.getElementById("adminEnrollmentsTableBody");
    if (!tbody) return;

    if (state.enrollments.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:1.5rem;color:var(--text-muted);">No active enrollments.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.enrollments.map(enrollment => `
        <tr>
            <td><strong>${escapeHtml(enrollment.studentName)}</strong> <span style="font-size:.75rem;color:var(--text-muted);">(ID #${enrollment.studentId})</span></td>
            <td>${escapeHtml(enrollment.courseName)}</td>
            <td><span class="badge badge-primary">${getCourseCredits(enrollment.courseId)} Credits</span></td>
            <td><span class="badge badge-success">Active</span></td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="deleteEnrollment(${enrollment.id})">Remove</button>
            </td>
        </tr>
    `).join("");
}

function getCourseCredits(courseId) {
    const course = state.courses.find(item => item.id === courseId);
    return course ? course.credits : "-";
}

function populateEnrollmentDropdowns() {
    const studentSelect = document.getElementById("enrollStudentSelect");
    const courseSelect = document.getElementById("enrollCourseSelect");

    if (studentSelect) {
        studentSelect.innerHTML = `<option value="">-- Select Student --</option>`
            + state.students.map(student => `<option value="${student.id}">${escapeHtml(student.name)} (ID #${student.id})</option>`).join("");
    }

    if (courseSelect) {
        courseSelect.innerHTML = `<option value="">-- Select Course --</option>`
            + state.courses.map(course => `<option value="${course.id}" ${course.availableSeats <= 0 ? "disabled" : ""}>${escapeHtml(course.name)} (${course.availableSeats} seats available)</option>`).join("");
    }
}

function setupAdminForms() {
    const addCourseForm = document.getElementById("addCourseForm");
    if (addCourseForm) {
        addCourseForm.addEventListener("submit", async event => {
            event.preventDefault();
            const name = document.getElementById("newCourseName").value.trim();
            const credits = Number(document.getElementById("newCourseCredits").value);
            const capacity = Number(document.getElementById("newCourseCapacity").value);

            if (!validateCourse(name, credits, capacity)) return;

            try {
                await apiFetch("/api/courses", {
                    method: "POST",
                    body: JSON.stringify({ name, credits, capacity })
                });
                showToast("Success", `Course "${name}" added.`, "success");
                addCourseForm.reset();
                closeModal("addCourseModal");
                await loadAdminData();
            } catch (error) {
                showToast("Error", error.message || "Failed to create course.", "error");
            }
        });
    }

    const addStudentForm = document.getElementById("addStudentForm");
    if (addStudentForm) {
        addStudentForm.addEventListener("submit", async event => {
            event.preventDefault();
            const name = document.getElementById("newStudentName").value.trim();
            const username = document.getElementById("newStudentUsername").value.trim();
            const password = document.getElementById("newStudentPassword").value;
            const email = document.getElementById("newStudentEmail").value.trim().toLowerCase();

            if (!validateStudent(name, email)) return;
            if (!/^[A-Za-z0-9._-]{3,50}$/.test(username)) {
                showToast("Validation Error", "Username must be 3-50 characters and contain only letters, numbers, dots, underscores or hyphens.", "error");
                return;
            }
            if (password.length < 6 || password.length > 100) {
                showToast("Validation Error", "Password must be between 6 and 100 characters.", "error");
                return;
            }

            try {
                await apiFetch("/api/students", {
                    method: "POST",
                    body: JSON.stringify({ name, username, password, email })
                });
                showToast("Success", `Student "${name}" added. They can now log in with the username and password provided.`, "success");
                addStudentForm.reset();
                closeModal("addStudentModal");
                await loadAdminData();
            } catch (error) {
                showToast("Error", error.message || "Failed to create student.", "error");
            }
        });
    }

    const manualEnrollForm = document.getElementById("manualEnrollForm");
    if (manualEnrollForm) {
        manualEnrollForm.addEventListener("submit", async event => {
            event.preventDefault();
            const studentId = Number(document.getElementById("enrollStudentSelect").value);
            const courseId = Number(document.getElementById("enrollCourseSelect").value);

            if (!studentId || !courseId) {
                showToast("Validation Error", "Please select both a student and a course.", "error");
                return;
            }

            try {
                await apiFetch("/api/enrollments", {
                    method: "POST",
                    body: JSON.stringify({ studentId, courseId })
                });
                showToast("Success", "Enrollment completed.", "success");
                manualEnrollForm.reset();
                closeModal("manualEnrollModal");
                await loadAdminData();
            } catch (error) {
                showToast("Error", error.message || "Enrollment failed.", "error");
            }
        });
    }
}

function validateCourse(name, credits, capacity) {
    if (!/^[A-Za-z][A-Za-z0-9 &().,'+:/-]{2,99}$/.test(name)) {
        showToast("Validation Error", "Course name must be 3-100 characters, start with a letter, and contain valid characters.", "error");
        return false;
    }
    if (!Number.isInteger(credits) || credits < 1 || credits > 12) {
        showToast("Validation Error", "Credits must be a whole number between 1 and 12.", "error");
        return false;
    }
    if (!Number.isInteger(capacity) || capacity < 1 || capacity > 500) {
        showToast("Validation Error", "Capacity must be a whole number between 1 and 500.", "error");
        return false;
    }
    return true;
}

function validateStudent(name, email) {
    if (!/^[A-Za-z][A-Za-z .'-]{1,99}$/.test(name)) {
        showToast("Validation Error", "Please enter a valid student name.", "error");
        return false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showToast("Validation Error", "Please enter a valid email address.", "error");
        return false;
    }
    return true;
}

/* --------------------------------------------------------------------------
   ADMIN CRUD ACTIONS
   -------------------------------------------------------------------------- */

window.editCourse = async function(courseId) {
    const course = state.courses.find(item => item.id === courseId);
    if (!course) return;

    const name = prompt("Course name:", course.name);
    if (name === null) return;
    const credits = Number(prompt("Credits (1-12):", course.credits));
    if (!Number.isInteger(credits)) return;
    const capacity = Number(prompt("Capacity (1-500):", course.capacity));
    if (!Number.isInteger(capacity)) return;

    if (!validateCourse(name.trim(), credits, capacity)) return;

    try {
        await apiFetch(`/api/courses/${courseId}`, {
            method: "PUT",
            body: JSON.stringify({ name: name.trim(), credits, capacity })
        });
        showToast("Success", "Course updated successfully.", "success");
        await loadAdminData();
    } catch (error) {
        showToast("Update Error", error.message || "Failed to update course.", "error");
    }
};

window.deleteCourse = async function(courseId) {
    const course = state.courses.find(item => item.id === courseId);
    if (!course) return;

    if (!confirm(`Delete course "${course.name}"? Its enrollment records will also be removed.`)) return;

    try {
        await apiFetch(`/api/courses/${courseId}`, { method: "DELETE" });
        showToast("Success", "Course deleted successfully.", "success");
        await loadAdminData();
    } catch (error) {
        showToast("Delete Error", error.message || "Failed to delete course.", "error");
    }
};

window.editStudent = async function(studentId) {
    const student = state.students.find(item => item.id === studentId);
    if (!student) return;

    const name = prompt("Student name:", student.name);
    if (name === null) return;
    const email = prompt("Email address:", student.email);
    if (email === null) return;

    const cleanName = name.trim();
    const cleanEmail = email.trim().toLowerCase();
    if (!validateStudent(cleanName, cleanEmail)) return;

    try {
        await apiFetch(`/api/students/${studentId}`, {
            method: "PUT",
            body: JSON.stringify({ name: cleanName, email: cleanEmail })
        });
        showToast("Success", "Student updated successfully.", "success");
        await loadAdminData();
    } catch (error) {
        showToast("Update Error", error.message || "Failed to update student.", "error");
    }
};

window.deleteStudent = async function(studentId) {
    const student = state.students.find(item => item.id === studentId);
    if (!student) return;

    const enrollmentCount = state.enrollments.filter(e => e.studentId === studentId).length;
    const suffix = enrollmentCount ? ` This will also remove ${enrollmentCount} enrollment record(s).` : "";
    if (!confirm(`Delete student "${student.name}"?${suffix}`)) return;

    try {
        await apiFetch(`/api/students/${studentId}`, { method: "DELETE" });
        showToast("Success", "Student deleted successfully.", "success");
        await loadAdminData();
    } catch (error) {
        showToast("Delete Error", error.message || "Failed to delete student.", "error");
    }
};

window.deleteEnrollment = async function(enrollmentId) {
    if (!confirm("Remove this enrollment?")) return;

    try {
        await apiFetch(`/api/enrollments/${enrollmentId}`, { method: "DELETE" });
        showToast("Success", "Enrollment removed.", "success");
        await loadAdminData();
    } catch (error) {
        showToast("Delete Error", error.message || "Failed to remove enrollment.", "error");
    }
};

/* --------------------------------------------------------------------------
   MODALS
   -------------------------------------------------------------------------- */

window.openCourseDetailsModal = function(courseId) {
    const course = state.courses.find(item => item.id === courseId);
    if (!course) return;

    document.getElementById("modalCourseTitle").textContent = course.name;
    document.getElementById("modalCourseId").textContent = `#${course.id}`;
    document.getElementById("modalCourseCredits").textContent = `${course.credits} Credits`;
    document.getElementById("modalCourseCapacity").textContent = `${course.enrolledCount}/${course.capacity} Seats occupied`;

    openModal("courseDetailsModal");
};

window.inspectStudentCoursesModal = async function(studentId, studentName) {
    const modalTitle = document.getElementById("studentCoursesModalTitle");
    const container = document.getElementById("studentCoursesModalList");

    if (modalTitle) modalTitle.textContent = `${studentName}'s Enrolled Courses`;
    if (container) container.innerHTML = `<div style="text-align:center;padding:1.5rem;"><span class="spinner"></span> Loading courses...</div>`;

    openModal("studentCoursesModal");

    try {
        const res = await apiFetch(`/api/students/${studentId}/courses`);
        const list = res.data || [];

        if (list.length === 0) {
            container.innerHTML = `<div class="empty-state" style="padding:1.5rem;"><p class="empty-desc">This student is not currently enrolled in any courses.</p></div>`;
            return;
        }

        container.innerHTML = `
            <table class="data-table" style="width:100%;">
                <thead><tr><th>ID</th><th>Course Title</th><th>Credits</th><th>Capacity</th></tr></thead>
                <tbody>
                    ${list.map(course => `
                        <tr>
                            <td>#${course.id}</td>
                            <td><strong>${escapeHtml(course.name)}</strong></td>
                            <td><span class="badge badge-primary">${course.credits} Credits</span></td>
                            <td>${course.enrolledCount}/${course.capacity}</td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    } catch (error) {
        container.innerHTML = `<div class="badge badge-danger" style="width:100%;padding:.5rem;">${escapeHtml(error.message || "Failed to load courses.")}</div>`;
    }
};

function escapeJsString(value) {
    return String(value)
        .replace(/\\/g, "\\\\")
        .replace(/'/g, "\\'")
        .replace(/\n/g, "\\n")
        .replace(/\r/g, "\\r");
}
