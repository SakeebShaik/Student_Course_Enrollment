Student Course Management System

A web-based Student Course Management System built using Java 17+, Spring Boot, Spring Web, Spring Data JPA, MySQL/PostgreSQL, HTML5, CSS3, and JavaScript.

The application provides separate workspaces for Administrators and Students. Administrators can manage students, courses, and enrollments, while students can view available courses, check course capacity, and enroll in courses.

Features
Authentication & Authorization
User login using username and password.
JWT-based authentication.
Role-based access control.
Separate Admin and Student workspaces.
Protected backend REST APIs.
JWT is included with authenticated API requests.
Administrator Features
View Admin dashboard.
View total students.
View total courses.
View total enrollments.
View total course capacity.
Add students.
Update student information.
Delete students.
Add courses.
Update courses.
Delete courses.
View all enrollments.
Manually enroll a student into a course.
Remove an enrollment.
View courses enrolled by a specific student.
Search students.
Search courses.
Student Features
View student profile.
View enrolled courses.
View available course catalog.
Search courses.
View course credits.
View course capacity.
View available seats.
Enroll in courses.
View course details.
View total enrolled courses.
View total credits.
Frontend Features
Dynamic dashboard using JavaScript.
Client-side form validation.
Dynamic course and student searching.
Dynamic DOM rendering.
Modal dialogs for details and forms.
No full-page refresh after CRUD operations.
Separate Admin and Student navigation.
Toast notifications for success and error messages.
Application Screenshots

Login Page




Student Dashboard




Student Course Catalog




Student My Courses




Admin Dashboard




Admin Course Management




Admin Student Management




Admin Enrollment Management




Technology Stack
Layer	Technology
Programming Language	Java 17+
Backend Framework	Spring Boot
REST API	Spring Web
Data Access	Spring Data JPA
Database	MySQL / PostgreSQL
Security	Spring Security
Authentication	JWT
Frontend	HTML5, CSS3, JavaScript
Build Tool	Maven
IDE	IntelliJ IDEA
Application Architecture

The application follows a frontend-backend architecture where the frontend communicates with the Spring Boot backend through REST APIs.

                 STUDENT COURSE MANAGEMENT SYSTEM
                              |
                +-------------+-------------+
                |                           |
                v                           v
           ADMIN USER                 STUDENT USER
                |                           |
                +-------------+-------------+
                              |
                              v
                    HTML / CSS / JavaScript
                              |
                              |
                         HTTP / JSON
                              |
                         JWT Token
                              |
                              v
                    Spring Boot Backend
                              |
                +-------------+-------------+
                |             |             |
                v             v             v
          Controllers     Services      Security
                |             |             |
                +-------------+-------------+
                              |
                              v
                     Spring Data JPA
                              |
                              v
                     MySQL / PostgreSQL
Project Flow

The overall application flow is:

User
  |
  v
Login Page
  |
  v
Username + Password
  |
  v
Authentication API
  |
  v
Spring Security
  |
  v
JWT Generated
  |
  v
JWT Stored in Frontend
  |
  v
Dashboard
  |
  +--------------------+
  |                    |
  v                    v
Admin Workspace    Student Workspace
  |                    |
  v                    v
CRUD Operations    Course Enrollment
  |                    |
  +---------+----------+
            |
            v
       REST APIs
            |
            v
       Spring Boot
            |
            v
         Database
Frontend

The frontend consists primarily of:

HTML pages
CSS files
JavaScript files

The JavaScript is responsible for:

Handling user interactions.
Calling REST APIs.
Handling authentication.
Maintaining frontend state.
Rendering data into the DOM.
Updating the UI after API operations.
Performing client-side validation.
Dashboard JavaScript

The main dashboard controller maintains application state using:

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

This state contains the data currently being used by the dashboard.

State Properties
Property	Purpose
currentUser	Stores the authenticated user
currentStudent	Stores the logged-in student's information
courses	Stores available courses
students	Stores student records
enrollments	Stores enrollment records
studentEnrolledCourses	Stores courses enrolled by the current student
activeTab	Stores the current Student tab
activeAdminView	Stores the current Admin view
Dashboard Initialization

When the dashboard page loads:

document.addEventListener("DOMContentLoaded", async () => {

the application:

Initializes the theme.
Checks whether the user is logged in.
Initializes common UI elements.
Checks the user's role.
Loads the appropriate workspace.

The flow is:

Page Loaded
    |
    v
DOMContentLoaded
    |
    v
initTheme()
    |
    v
checkLogin()
    |
    v
Is User Logged In?
    |
    +----------+
    |          |
   No         Yes
    |          |
    v          v
  Stop    Check User Role
                |
          +-----+-----+
          |           |
        Admin       Student
          |           |
          v           v
   Admin Workspace  Student Workspace
Student Workspace

The Student workspace is initialized using:

initStudentWorkspace()

It:

Displays the Student workspace.
Hides the Admin workspace.
Configures Student navigation.
Loads Student data.
Opens the My Courses tab.
Loading Student Data

The application loads three sets of data:

GET /api/students/me
GET /api/courses
GET /api/students/me/courses

These requests are executed using Promise.all().

The responses are stored in frontend state:

state.currentStudent = studentRes.data;
state.courses = coursesRes.data || [];
state.studentEnrolledCourses = enrolledRes.data || [];

The UI is then rendered using:

renderStudentProfile()
renderStudentStats()
renderStudentEnrolledCourses()
renderStudentCatalog()
Student Enrollment Flow

When a student clicks the Enroll button:

Student clicks Enroll
        |
        v
enrollCurrentStudent(courseId)
        |
        v
POST /api/enrollments/me/{courseId}
        |
        v
Spring Boot Backend
        |
        v
Database
        |
        v
Successful Response
        |
        v
loadStudentData()
        |
        v
Updated Frontend State
        |
        v
Rendering Functions
        |
        v
Updated DOM

The browser does not need to perform a complete page reload.

Admin Workspace

The Admin workspace is initialized using:

initAdminWorkspace()

It:

Displays the Admin workspace.
Hides the Student workspace.
Configures Admin navigation.
Loads Admin data.
Opens the Admin overview.
Loading Admin Data

The Admin dashboard loads:

GET /api/courses
GET /api/students
GET /api/enrollments

The returned data is stored in:

state.courses
state.students
state.enrollments

The following functions then update the Admin interface:

renderAdminKPIs()
renderAdminCoursesTable()
renderAdminStudentsTable()
renderAdminEnrollmentsHub()
populateEnrollmentDropdowns()
Admin Add Course Flow

When the administrator adds a course:

Admin opens Add Course
        |
        v
Admin enters course information
        |
        v
Submit Form
        |
        v
event.preventDefault()
        |
        v
validateCourse()
        |
        v
POST /api/courses
        |
        v
Spring Boot Backend
        |
        v
Database
        |
        v
Successful Response
        |
        v
loadAdminData()
        |
        v
renderAdminCoursesTable()
        |
        v
Updated Course Table

event.preventDefault() prevents the browser from performing its normal form submission and page navigation.

Admin Add Student Flow

The Admin can create a student by submitting:

Name
Username
Password
Email

The frontend validates the information and sends:

POST /api/students

Example request:

{
  "name": "John Doe",
  "username": "john.doe",
  "password": "password",
  "email": "john@example.com"
}

After successful creation:

POST /api/students
        |
        v
Backend
        |
        v
Database
        |
        v
loadAdminData()
        |
        v
renderAdminStudentsTable()
        |
        v
New Student Appears
Admin Update Course Flow

The administrator can update an existing course.

The frontend sends:

PUT /api/courses/{courseId}

with updated course information.

After a successful response:

await loadAdminData();

is called.

This retrieves the latest data and re-renders the course table.

Admin Delete Course Flow

The administrator can delete a course.

The frontend sends:

DELETE /api/courses/{courseId}

After successful deletion:

DELETE Request
      |
      v
Backend
      |
      v
Database
      |
      v
loadAdminData()
      |
      v
renderAdminCoursesTable()
      |
      v
Deleted Course Removed From UI

No complete browser refresh is required.

Admin Student Management

Administrators can:

Add students.
Edit students.
Delete students.
View a student's enrolled courses.

Relevant API endpoints include:

GET    /api/students
POST   /api/students
PUT    /api/students/{id}
DELETE /api/students/{id}
GET    /api/students/{id}/courses
Enrollment Management

Administrators can manually create an enrollment.

The frontend sends:

POST /api/enrollments

Example:

{
  "studentId": 1,
  "courseId": 5
}

Administrators can remove an enrollment using:

DELETE /api/enrollments/{enrollmentId}
DOM Rendering

One of the important frontend concepts in this project is dynamic DOM rendering.

Instead of refreshing the complete page, JavaScript updates specific elements.

For example:

tbody.innerHTML = filtered.map(course => `
    <tr>
        ...
    </tr>
`).join("");

This dynamically generates the rows of the course table.

Another example is:

element.textContent = value;

which updates the text of a particular element.

The main rendering functions are:

renderStudentStats()
renderStudentEnrolledCourses()
renderStudentCatalog()
renderStudentProfile()

renderAdminKPIs()
renderAdminCoursesTable()
renderAdminStudentsTable()
renderAdminEnrollmentsHub()
How the Page Updates Without Refreshing

The application follows this pattern:

User performs an action
        |
        v
JavaScript event handler
        |
        v
apiFetch()
        |
        v
REST API request
        |
        v
Spring Boot
        |
        v
Database
        |
        v
API Response
        |
        v
loadAdminData()
or
loadStudentData()
        |
        v
Frontend state updated
        |
        v
render...() function
        |
        v
DOM updated

Therefore, the browser does not have to reload the complete HTML page.

JWT Authentication

JWT is used to authenticate protected API requests.

The general authentication flow is:

User enters username and password
             |
             v
        Login Request
             |
             v
      Spring Security
             |
             v
       User Verified
             |
             v
         JWT Created
             |
             v
      Frontend Receives JWT
             |
             v
       JWT Stored
             |
             v
       Authenticated API Request
             |
             v
Authorization: Bearer <JWT>
             |
             v
      Spring Security Filter
             |
             v
       JWT Validation
             |
             v
      Request Authorized

The dashboard functions use the common apiFetch() helper:

apiFetch("/api/courses")

rather than manually creating the authentication header inside every individual operation.

The JWT storage and Authorization-header implementation are handled by the application's authentication/API utility code.

API Endpoints
Method	Endpoint	Description
GET	/api/students/me	Get logged-in student
GET	/api/courses	Get all courses
GET	/api/students/me/courses	Get logged-in student's courses
POST	/api/enrollments/me/{courseId}	Enroll current student
GET	/api/students	Get all students
POST	/api/students	Create student
PUT	/api/students/{id}	Update student
DELETE	/api/students/{id}	Delete student
GET	/api/students/{id}/courses	Get student's courses
POST	/api/courses	Create course
PUT	/api/courses/{id}	Update course
DELETE	/api/courses/{id}	Delete course
GET	/api/enrollments	Get all enrollments
POST	/api/enrollments	Create enrollment
DELETE	/api/enrollments/{id}	Delete enrollment
Validation

The frontend contains validation functions:

validateCourse()
validateStudent()
Course Validation

The course form validates:

Course name.
Course name length.
Course name format.
Credits.
Capacity.

Credits must be within the configured range and capacity must be within the configured range.

Student Validation

The student form validates:

Student name.
Email.
Username.
Password length.

Frontend validation improves user experience, but important validation should also be performed by the backend because client-side validation cannot be trusted as a security mechanism.

Search and Filtering

The Admin and Student dashboards support searching.

For example, course searching uses:

state.courses.filter(...)

The filtering happens against the data already loaded into frontend state.

The flow is:

User types search text
        |
        v
input event
        |
        v
Filter frontend state
        |
        v
Render filtered results

Therefore, the application does not need to call the backend for every search keystroke.

Important JavaScript Functions
Function	Responsibility
initShellUI()	Initializes common dashboard UI
initStudentWorkspace()	Initializes Student workspace
setupStudentTabs()	Configures Student navigation
switchStudentTab()	Switches Student tabs
loadStudentData()	Loads Student data
renderStudentStats()	Renders Student statistics
renderStudentEnrolledCourses()	Renders enrolled courses
renderStudentCatalog()	Renders course catalog
renderStudentProfile()	Renders Student profile
enrollCurrentStudent()	Enrolls Student in a course
initAdminWorkspace()	Initializes Admin workspace
setupAdminNavigation()	Configures Admin navigation
switchAdminView()	Switches Admin views
loadAdminData()	Loads Admin data
renderAdminKPIs()	Renders Admin dashboard statistics
renderAdminCoursesTable()	Renders course table
renderAdminStudentsTable()	Renders student table
renderAdminEnrollmentsHub()	Renders enrollment table
populateEnrollmentDropdowns()	Populates enrollment dropdowns
setupAdminForms()	Configures Admin forms
validateCourse()	Validates course input
validateStudent()	Validates student input
editCourse()	Updates a course
deleteCourse()	Deletes a course
editStudent()	Updates a student
deleteStudent()	Deletes a student
deleteEnrollment()	Removes an enrollment
openCourseDetailsModal()	Displays course details
inspectStudentCoursesModal()	Displays a student's courses
escapeJsString()	Escapes JavaScript strings
Concurrency Consideration

The frontend displays course availability using data retrieved from the backend, but the frontend should not be treated as the final source of truth.

For example, consider two devices:

Device A
Admin
  |
  | DELETE course
  v
Backend
  |
  v
Course Deleted


Device B
Student
  |
  | POST enrollment
  v
Backend
  |
  v
Check current database state
  |
  +----------------------+
  |                      |
Course exists       Course deleted
  |                      |
  v                      v
Process enrollment    Reject request

This is important because the Student's browser may still display an old course list while the Admin has already deleted that course.

The backend must perform the final validation against the current database state.

For real-time synchronization where changes on one device immediately appear on another device, an additional mechanism such as WebSockets or Server-Sent Events can be introduced.

Database

The application uses a relational database such as:

MySQL
PostgreSQL

The database stores application data such as:

Users
Students
Courses
Enrollments

Spring Data JPA is used to communicate between the Java application and the database.

Running the Application
Prerequisites

Install:

Java 17 or later
Maven
MySQL or PostgreSQL
IntelliJ IDEA
Modern web browser

Verify Java:

java -version

Verify Maven:

mvn -version
Database Configuration
Start MySQL or PostgreSQL.
Create the application database.
Configure the database connection in the Spring Boot configuration.
Start the application.

Example configuration:

spring.datasource.url=jdbc:mysql://localhost:3306/student_course_management
spring.datasource.username=<database-username>
spring.datasource.password=<database-password>

Use the actual database configuration required by your environment.

Running with IntelliJ IDEA
Clone or download the repository.
Open the project in IntelliJ IDEA.
Allow Maven dependencies to load.
Configure the database.
Check the application configuration.
Run the Spring Boot main application class.
Open the application in your browser.
Running with Maven

From the project root directory:

mvn spring-boot:run
Recommended Code Reading Order

If you are learning this project, the recommended order is:

1. HTML
       |
       v
2. CSS
       |
       v
3. JavaScript
       |
       v
4. apiFetch()
       |
       v
5. JWT Authentication
       |
       v
6. REST Controllers
       |
       v
7. Service Layer
       |
       v
8. Repository Layer
       |
       v
9. Database

For the dashboard specifically:

dashboard.html
       |
       v
dashboard.js
       |
       v
DOMContentLoaded
       |
       v
checkLogin()
       |
       +--------------------+
       |                    |
       v                    v
initAdminWorkspace()   initStudentWorkspace()
       |                    |
       v                    v
loadAdminData()        loadStudentData()
       |                    |
       v                    v
render...()            render...()
       |                    |
       +----------+---------+
                  |
                  v
                 DOM
Future Improvements

Possible future improvements include:

Add JUnit and Mockito test coverage.
Add integration tests.
Add stronger backend validation.
Add database constraints.
Add pagination for large datasets.
Add centralized frontend error handling.
Add real-time synchronization using WebSockets.
Add JWT refresh-token handling.
Add audit logging for Admin operations.
Replace inline onclick handlers with event listeners.
Add API documentation using OpenAPI/Swagger.
Improve exception handling and standardized API error responses.
Summary

The Student Course Management System provides role-based functionality for Administrators and Students.

                 Student Course
                 Management System
                        |
             +----------+----------+
             |                     |
             v                     v
        Administrator           Student
             |                     |
       +-----+-----+         +-----+-----+
       |     |     |         |     |     |
   Students Courses Enroll. Profile Catalog
                                  |
                                  v
                             My Courses

The core application flow is:

User
  |
  v
Frontend
  |
  v
JavaScript
  |
  v
JWT Authentication
  |
  v
REST API
  |
  v
Spring Boot
  |
  v
Service Layer
  |
  v
Repository
  |
  v
Database
  |
  v
Response
  |
  v
Frontend State
  |
  v
DOM Rendering

The frontend uses API-driven updates and dynamic DOM rendering so that operations such as adding, updating, deleting, and enrolling can update the relevant parts of the interface without requiring a complete browser refresh.

Author

Sakeeb

Built as a Student Course Management System project using Spring Boot and Java.
