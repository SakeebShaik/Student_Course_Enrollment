Student Course Management System

A web-based Student Course Management System built with Java 17+,
Spring Boot, Spring Web, Spring Data JPA, MySQL/PostgreSQL, HTML5, CSS3,
and JavaScript.

The application provides separate workspaces for Administrators and
Students. Administrators can manage students, courses, and
enrollments, while students can view the course catalog, check course
capacity, and enroll in courses.

Features

Authentication and Authorization

User login using username and password.

JWT-based authentication.

Role-based access for Administrator and Student.

Protected backend API endpoints.

The frontend loads the appropriate workspace according to the
authenticated user's role.

Administrator Features

View dashboard KPIs.

View, add, update, and delete students.

View, add, update, and delete courses.

View all enrollments.

Manually enroll a student into a course.

Remove an enrollment.

View courses enrolled by a specific student.

Search/filter students and courses.

Student Features

View student profile.

View enrolled courses.

View course catalog.

Search courses.

View course capacity and available seats.

Enroll in a course.

View course details.

View enrollment and credit statistics.

Frontend Features

The dashboard uses JavaScript to update the DOM without a full browser
refresh after CRUD operations.

Typical flow:

User Action
    ↓
JavaScript Event Handler
    ↓
apiFetch()
    ↓
HTTP Request
    ↓
Spring Boot Backend
    ↓
Database
    ↓
HTTP Response
    ↓
Frontend State
    ↓
render...() Function
    ↓
DOM Update

Technology Stack

Layer            Technology

Backend          Java 17+
Framework        Spring Boot
REST API         Spring Web
Persistence      Spring Data JPA
Database         MySQL / PostgreSQL
Authentication   JWT
Frontend         HTML5, CSS3, JavaScript
Build Tool       Maven
IDE              IntelliJ IDEA

Architecture

┌──────────────────────────────┐
│          Browser             │
│                              │
│ HTML + CSS + JavaScript      │
└──────────────┬───────────────┘
               │
               │ HTTP / JSON
               │ JWT Authorization
               ▼
┌──────────────────────────────┐
│       Spring Boot API        │
│                              │
│ Controllers                  │
│ Services                     │
│ Security / JWT               │
│ Repositories                 │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│       MySQL / PostgreSQL     │
│                              │
│ Students                     │
│ Courses                      │
│ Enrollments                  │
│ Authentication data          │
└──────────────────────────────┘

Frontend

The main dashboard JavaScript maintains temporary frontend state:

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

Important State Properties

currentUser --- authenticated user's information.

currentStudent --- logged-in student's profile.

courses --- available courses.

students --- students loaded for the administrator.

enrollments --- enrollment records.

studentEnrolledCourses --- courses belonging to the logged-in
student.

activeTab --- current Student tab.

activeAdminView --- current Admin view.

Dashboard Initialization

The dashboard waits for the HTML document to finish loading:

document.addEventListener("DOMContentLoaded", async () => {

It then:

Initializes the theme.

Checks whether the user is logged in.

Initializes common UI.

Checks the user's role.

Loads the Admin or Student workspace.

DOMContentLoaded
      ↓
checkLogin()
      ↓
Authenticated?
   ┌──┴──┐
  No    Yes
  ↓      ↓
Stop   Check role
          ↓
    ┌─────┴─────┐
  Admin       Student
    ↓             ↓
Admin UI       Student UI

Student Data Flow

The Student workspace requests:

GET /api/students/me
GET /api/courses
GET /api/students/me/courses

The responses are stored in frontend state:

state.currentStudent = studentRes.data;
state.courses = coursesRes.data || [];
state.studentEnrolledCourses = enrolledRes.data || [];

Then the UI is rendered using functions such as:

renderStudentProfile()
renderStudentStats()
renderStudentEnrolledCourses()
renderStudentCatalog()

Student Enrollment Flow

When a student clicks Enroll:

Click Enroll
    ↓
enrollCurrentStudent(courseId)
    ↓
POST /api/enrollments/me/{courseId}
    ↓
Backend
    ↓
Database
    ↓
Success
    ↓
loadStudentData()
    ↓
Fresh API data
    ↓
Render functions
    ↓
Updated DOM

The browser does not need to perform a full page reload.

Administrator Data Flow

The Admin workspace requests:

GET /api/courses
GET /api/students
GET /api/enrollments

The data is stored in:

state.courses
state.students
state.enrollments

Then the following functions render the Admin UI:

renderAdminKPIs()
renderAdminCoursesTable()
renderAdminStudentsTable()
renderAdminEnrollmentsHub()
populateEnrollmentDropdowns()

Add Course Flow

When the administrator submits the Add Course form:

Submit form
    ↓
event.preventDefault()
    ↓
Frontend validation
    ↓
POST /api/courses
    ↓
Spring Boot backend
    ↓
Database
    ↓
loadAdminData()
    ↓
renderAdminCoursesTable()
    ↓
New course appears

event.preventDefault() prevents normal browser form submission and
therefore prevents the page from navigating/reloading.

Update and Delete Flow

Update Course

Edit
 ↓
PUT /api/courses/{courseId}
 ↓
Backend
 ↓
Database
 ↓
loadAdminData()
 ↓
renderAdminCoursesTable()

Delete Course

Delete
 ↓
DELETE /api/courses/{courseId}
 ↓
Backend
 ↓
Database
 ↓
loadAdminData()
 ↓
renderAdminCoursesTable()

The same approach is used for students and enrollments.

DOM Rendering

The application updates specific parts of the page rather than reloading
the entire document.

For example:

tbody.innerHTML = filtered.map(course => `
    <tr>
        ...
    </tr>
`).join("");

and:

element.textContent = value;

These operations update the DOM directly.

Important rendering functions include:

renderStudentStats()
renderStudentEnrolledCourses()
renderStudentCatalog()
renderStudentProfile()

renderAdminKPIs()
renderAdminCoursesTable()
renderAdminStudentsTable()
renderAdminEnrollmentsHub()

JWT Authentication

The application uses JWT-based authentication.

General flow:

Username + Password
        ↓
Login API
        ↓
Spring Security authentication
        ↓
JWT generated
        ↓
Frontend receives JWT
        ↓
JWT is stored by frontend
        ↓
apiFetch() sends authenticated requests
        ↓
Backend validates JWT
        ↓
Protected API operation

The dashboard functions call the common apiFetch() helper, for
example:

apiFetch("/api/courses")

rather than manually constructing the authentication request in every
business function.

The exact token-storage and Authorization-header implementation belongs
to the authentication/API utility code, not the dashboard rendering
functions shown in dashboard.js.

Validation

The frontend contains:

validateCourse()
validateStudent()

Course validation checks course name, credits, and capacity.

Student validation checks name and email, while the Add Student form
also validates username and password.

Frontend validation improves user experience, but equivalent validation
should exist on the backend because client-side validation cannot be
trusted for security.

Search and Filtering

Course and student searches are performed against data already loaded
into frontend state.

For example:

state.courses.filter(...)

The search therefore does not need an API request for every keystroke.

User types
    ↓
input event
    ↓
Filter local state
    ↓
Render filtered results

API Overview

Method   Endpoint                           Purpose

GET      /api/students/me                 Get current student
GET      /api/courses                     Get courses
GET      /api/students/me/courses         Get current student's courses
POST     /api/enrollments/me/{courseId}   Student enrollment
GET      /api/students                    Get students
POST     /api/students                    Create student
PUT      /api/students/{id}               Update student
DELETE   /api/students/{id}               Delete student
POST     /api/courses                     Create course
PUT      /api/courses/{id}                Update course
DELETE   /api/courses/{id}                Delete course
GET      /api/enrollments                 Get enrollments
POST     /api/enrollments                 Create enrollment
DELETE   /api/enrollments/{id}            Delete enrollment
GET      /api/students/{id}/courses       Get a student's courses

Concurrency Consideration

The frontend's course availability is only a display value. The backend
must perform the final validation when an enrollment request arrives.

For example:

Device A: Admin
       ↓
DELETE course
       ↓
Backend
       ↓
Course removed

Device B: Student
       ↓
POST enrollment
       ↓
Backend checks current database state
       ↓
Reject if course no longer exists

This prevents a stale browser view from being treated as the source of
truth.

If immediate updates between multiple open devices are required, the
application would need an additional real-time mechanism such as
WebSockets or Server-Sent Events. The current dashboard refreshes its
state when it makes another API request.

Running the Application

Prerequisites

Install:

Java 17 or later

Maven

MySQL or PostgreSQL

IntelliJ IDEA or another Java IDE

A modern web browser

Verify Java:

java -version

Verify Maven:

mvn -version

Database Configuration

Start MySQL or PostgreSQL.

Create the application database.

Configure the database connection in the Spring Boot configuration.

Start the application.

Typical configuration is similar to:

spring.datasource.url=jdbc:mysql://localhost:3306/student_course_management
spring.datasource.username=<database-username>
spring.datasource.password=<database-password>

Use the actual configuration required by the project environment.

Run with IntelliJ IDEA

Open the project in IntelliJ IDEA.

Allow Maven dependencies to load.

Configure the database.

Run the Spring Boot main application class.

Open the application in a browser.

Run with Maven

mvn spring-boot:run

Recommended Code-Reading Order

For someone learning this project, the recommended order is:

HTML
  ↓
JavaScript
  ↓
apiFetch()
  ↓
JWT Authentication
  ↓
REST Controllers
  ↓
Service Layer
  ↓
JPA Repositories
  ↓
Database

For the dashboard:

dashboard.html
      ↓
dashboard.js
      ↓
DOMContentLoaded
      ↓
checkLogin()
      ↓
initAdminWorkspace()
       OR
initStudentWorkspace()
      ↓
loadAdminData()
       OR
loadStudentData()
      ↓
render functions
      ↓
DOM

Key dashboard.js Functions

Function                           Responsibility

initShellUI()                    Initialize common dashboard UI
initStudentWorkspace()           Initialize Student workspace
setupStudentTabs()               Configure Student navigation
switchStudentTab()               Switch Student views
loadStudentData()                Fetch Student data
renderStudentStats()             Render Student statistics
renderStudentEnrolledCourses()   Render enrolled courses
renderStudentCatalog()           Render course catalog
renderStudentProfile()           Render Student profile
enrollCurrentStudent()           Enroll Student in a course
initAdminWorkspace()             Initialize Admin workspace
setupAdminNavigation()           Configure Admin navigation
switchAdminView()                Switch Admin views
loadAdminData()                  Fetch Admin data
renderAdminKPIs()                Render Admin metrics
renderAdminCoursesTable()        Render course table
renderAdminStudentsTable()       Render student table
renderAdminEnrollmentsHub()      Render enrollment table
populateEnrollmentDropdowns()    Populate enrollment forms
setupAdminForms()                Configure Admin forms
validateCourse()                 Validate course input
validateStudent()                Validate student input
editCourse()                     Update a course
deleteCourse()                   Delete a course
editStudent()                    Update a student
deleteStudent()                  Delete a student
deleteEnrollment()               Remove enrollment
openCourseDetailsModal()         Show course details
inspectStudentCoursesModal()     Show a student's courses
escapeJsString()                 Escape JavaScript strings

Future Improvements

Potential improvements include:

Add automated unit and integration tests.

Add comprehensive backend validation.

Add stronger concurrency handling for course enrollment.

Add centralized frontend error handling.

Add pagination for large datasets.

Add real-time synchronization with WebSockets or Server-Sent Events.

Replace inline onclick handlers with event listeners.

Add audit logging for administrator operations.

Add JWT expiration/refresh handling.

Add database-level constraints where appropriate.

Summary

The Student Course Management System is an API-driven application with
two primary roles:

Student Course Management System
              │
       ┌──────┴──────┐
       │             │
 Administrator     Student
       │             │
   Students       Profile
   Courses        Catalog
   Enrollments    My Courses

The central frontend pattern is:

User Action
    ↓
JavaScript
    ↓
apiFetch()
    ↓
REST API
    ↓
Spring Boot
    ↓
Database
    ↓
Response
    ↓
Frontend State
    ↓
render...()
    ↓
DOM Update

This design allows the application to update individual parts of the
page without requiring a complete browser refresh.
