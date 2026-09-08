Student Course Management System

A web-based Student Course Management System built using Java, Spring Boot, Spring Security, JWT, Spring Data JPA, MySQL/PostgreSQL, HTML5, CSS3, and JavaScript.

The application provides separate Admin and Student workspaces, allowing administrators to manage students, courses, and enrollments, while students can browse and enroll in available courses.

Features
Authentication & Security
Username and password-based login
JWT-based authentication
Role-based access control
Separate Admin and Student access
Protected REST APIs
JWT Bearer token included with authenticated API requests

Admin Features
Admin dashboard with statistics
Add, update, delete, and search students
Add, update, delete, and search courses
View and manage enrollments
Manually enroll students into courses
Remove enrollments
View courses enrolled by a specific student

Student Features
View student profile
View enrolled courses
Browse available course catalog
Search courses
View course credits, capacity, and available seats
Enroll in courses
View total enrolled courses and credits

Frontend
Dynamic dashboard using JavaScript
Client-side form validation
Dynamic search and filtering
Modal dialogs
Toast notifications
Dynamic DOM rendering
API-driven updates without complete page refresh
Separate Admin and Student navigation

Technology Stack
Layer	                           Technology
Programming Language	           Java 17+
Backend	                         Spring Boot
REST API	                       Spring Web
Security	                       Spring Security
Authentication	                 JWT
Data Access	                     Spring Data JPA
Database	                       MySQL
Frontend                       	 HTML5, CSS3, JavaScript
Build Tool	                     Maven
IDE	                             IntelliJ IDEA

Application Architecture
Admin / Student
       |
       v
HTML / CSS / JavaScript
       |
       | HTTP / JSON
       | JWT
       v
Spring Boot Backend
       |
       +-------------------+
       |                   |
       v                   v
 Controllers          Security
       |
       v
   Services
       |
       v
 Spring Data JPA
       |
       v
MySQL / PostgreSQL

The frontend communicates with the Spring Boot backend through REST APIs. The backend handles authentication, authorization, business logic, and database operations.

Authentication & JWT Flow

The application uses Spring Security and JWT to secure the backend APIs.

User enters username & password
              |
              v
        Login Request
              |
              v
       Spring Security
              |
              v
       User Verification
              |
              v
          JWT Created
              |
              v
      JWT sent to Frontend
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
              |
              v
       REST Controller

After successful login, the backend generates a JWT and sends it to the frontend. The frontend stores the token and includes it in the Authorization header for protected API requests.

The backend's Spring Security filter validates the JWT before allowing access to protected endpoints. Role-based authorization then determines whether the user can access Admin or Student functionality.

Backend Flow

The general request flow is:

Frontend
   |
   v
JavaScript
   |
   v
REST API
   |
   v
Controller
   |
   v
Service
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
Frontend

For example, when a student enrolls in a course:

Student clicks Enroll
        |
        v
POST /api/enrollments/me/{courseId}
        |
        v
Spring Boot Backend
        |
        v
Service Layer
        |
        v
Database
        |
        v
Successful Response
        |
        v
Frontend State Updated
        |
        v
UI Updated

The application updates the relevant UI dynamically without requiring a complete browser refresh.

API Endpoints
Student
Method	Endpoint	Description
GET	/api/students/me	Get logged-in student
GET	/api/courses	Get available courses
GET	/api/students/me/courses	Get student's courses
POST	/api/enrollments/me/{courseId}	Enroll in a course

Admin
Method	      Endpoint         	           Description
GET	         /api/students	               Get all students
POST	       /api/students	               Create student
PUT	         /api/students/{id}        	   Update student
DELETE	     /api/students/{id}	           Delete student
GET	         /api/students/{id}/courses	   Get student's courses
GET	         /api/courses	                 Get all courses
POST	      /api/courses	                 Create course
PUT	        /api/courses/{id}              Update course
DELETE	    /api/courses/{id}	             Delete course
GET	        /api/enrollments	             Get all enrollments
POST	      /api/enrollments	             Create enrollment
DELETE	    /api/enrollments/{id}       	 Delete enrollment
Database

The application uses a relational database such as MySQL or PostgreSQL.

Main data includes:

Users
Students
Courses
Enrollments

Spring Data JPA is used to communicate between the Spring Boot application and the database.

Validation

The application includes frontend validation for:

Student name, email, username, and password
Course name and format
Course credits
Course capacity

Frontend validation improves user experience, while important validation should also be enforced on the backend.

Screenshots
Login Page

<img width="1919" height="943" alt="image" src="https://github.com/user-attachments/assets/0b0c321b-64b5-4e70-9d8f-e2c91aa08131" />



Student Dashboard

<img width="1917" height="948" alt="image" src="https://github.com/user-attachments/assets/5c3eb986-85ef-4a1e-a283-68a14ccc4094" />



Student Course Catalog

<img width="1898" height="952" alt="image" src="https://github.com/user-attachments/assets/58724f4d-36aa-46bf-9d32-e0878eb55ab6" />



Student My Courses
<img width="1916" height="947" alt="image" src="https://github.com/user-attachments/assets/258f0d50-1c40-409d-936f-0a48c8babc50" />




Admin Dashboard
<img width="1895" height="940" alt="image" src="https://github.com/user-attachments/assets/b96ff84b-dbea-4efe-8daf-23c07ef06c46" />




Admin Course Management
<img width="1895" height="942" alt="image" src="https://github.com/user-attachments/assets/1c18f660-af7a-46dc-896f-89369c041591" />




Admin Student Management
<img width="1899" height="939" alt="image" src="https://github.com/user-attachments/assets/f4fa02a2-c3b7-4231-b0ac-2321301f5e5a" />




Admin Enrollment Management
<img width="1893" height="945" alt="image" src="https://github.com/user-attachments/assets/b233bb86-50ea-44f5-b001-cbc5572ea013" />




Running the Application
Prerequisites
Java 17 or later
Maven
MySQL or PostgreSQL
IntelliJ IDEA
Modern web browser
Database Configuration

Configure the database connection in the Spring Boot application properties:

spring.datasource.url=jdbc:mysql://localhost:3306/student_course_management
spring.datasource.username=<database-username>
spring.datasource.password=<database-password>

Run Using IntelliJ IDEA
Clone the repository.
Open the project in IntelliJ IDEA.
Allow Maven dependencies to load.
Configure the database.
Check the application configuration.
Run the Spring Boot main application class.
Open the application in your browser.
Run Using Maven
mvn spring-boot:run
Project Structure
Frontend
   |
   +-- HTML
   +-- CSS
   +-- JavaScript
            |
            v
       REST API
            |
            v
      Spring Boot
            |
     +------+------+
     |             |
 Security       Business Logic
     |             |
     +------+------+
            |
            v
      Spring Data JPA
            |
            v
         Database
Future Improvements

Possible future enhancements:

JUnit and Mockito test coverage
Integration testing
Stronger backend validation
Pagination
Standardized API error handling
OpenAPI/Swagger documentation
JWT refresh-token support
Audit logging
Real-time updates using WebSockets

Author
Sakeeb

Built as a Student Course Management System using Java and Spring Boot.
