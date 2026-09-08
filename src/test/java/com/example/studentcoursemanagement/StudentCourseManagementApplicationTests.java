package com.example.studentcoursemanagement;

import com.example.studentcoursemanagement.dto.auth.LoginRequest;
import com.example.studentcoursemanagement.dto.auth.LoginResponse;
import com.example.studentcoursemanagement.dto.auth.RegisterRequest;
import com.example.studentcoursemanagement.dto.auth.UserResponse;
import com.example.studentcoursemanagement.dto.course.CourseRequest;
import com.example.studentcoursemanagement.dto.course.CourseResponse;
import com.example.studentcoursemanagement.dto.enrollment.EnrollmentRequest;
import com.example.studentcoursemanagement.dto.enrollment.EnrollmentResponse;
import com.example.studentcoursemanagement.dto.student.StudentCreateRequest;
import com.example.studentcoursemanagement.dto.student.StudentRequest;
import com.example.studentcoursemanagement.dto.student.StudentResponse;
import com.example.studentcoursemanagement.entity.Course;
import com.example.studentcoursemanagement.entity.Enrollment;
import com.example.studentcoursemanagement.entity.Role;
import com.example.studentcoursemanagement.entity.Student;
import com.example.studentcoursemanagement.entity.User;
import com.example.studentcoursemanagement.exception.CourseCapacityExceededException;
import com.example.studentcoursemanagement.exception.CourseNotFoundException;
import com.example.studentcoursemanagement.exception.DuplicateCourseException;
import com.example.studentcoursemanagement.exception.DuplicateEnrollmentException;
import com.example.studentcoursemanagement.exception.DuplicateStudentException;
import com.example.studentcoursemanagement.exception.DuplicateUserException;
import com.example.studentcoursemanagement.exception.EnrollmentNotFoundException;
import com.example.studentcoursemanagement.exception.InvalidCourseCapacityException;
import com.example.studentcoursemanagement.exception.StudentNotFoundException;
import com.example.studentcoursemanagement.repo.CourseRepository;
import com.example.studentcoursemanagement.repo.EnrollmentRepository;
import com.example.studentcoursemanagement.repo.StudentRepository;
import com.example.studentcoursemanagement.repo.UserRepository;
import com.example.studentcoursemanagement.security.JwtService;
import com.example.studentcoursemanagement.service.AuthService;
import com.example.studentcoursemanagement.service.CourseService;
import com.example.studentcoursemanagement.service.EnrollmentService;
import com.example.studentcoursemanagement.service.StudentService;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentCourseManagementApplicationTests {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    private CourseService courseService;
    private StudentService studentService;
    private EnrollmentService enrollmentService;
    private AuthService authService;

    private Validator validator;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(
                courseRepository,
                enrollmentRepository
        );

        studentService = new StudentService(
                studentRepository,
                enrollmentRepository,
                userRepository,
                passwordEncoder
        );

        enrollmentService = new EnrollmentService(
                enrollmentRepository,
                studentRepository,
                courseRepository
        );

        authService = new AuthService(
                userRepository,
                studentRepository,
                authenticationManager,
                jwtService,
                passwordEncoder
        );

        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ============================================================
    // BASIC APPLICATION TEST
    // ============================================================

    @Test
    void contextLoads() {
        assertNotNull(courseService);
        assertNotNull(studentService);
        assertNotNull(enrollmentService);
        assertNotNull(authService);
    }

    // ============================================================
    // COURSE VALIDATION TESTS
    // ============================================================

    @Test
    void courseRequest_shouldRejectNumericOnlyCourseName() {
        CourseRequest request = new CourseRequest(
                "123456",
                3,
                30
        );

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void courseRequest_shouldRejectBlankCourseName() {
        CourseRequest request = new CourseRequest(
                "",
                3,
                30
        );

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage().equals("Course name is required"))
        );
    }

    @Test
    void courseRequest_shouldRejectZeroCredits() {
        CourseRequest request = new CourseRequest(
                "Java Programming",
                0,
                30
        );

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void courseRequest_shouldRejectCreditsGreaterThan12() {
        CourseRequest request = new CourseRequest(
                "Java Programming",
                13,
                30
        );

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void courseRequest_shouldRejectZeroCapacity() {
        CourseRequest request = new CourseRequest(
                "Java Programming",
                3,
                0
        );

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void courseRequest_shouldRejectCapacityGreaterThan500() {
        CourseRequest request = new CourseRequest(
                "Java Programming",
                3,
                501
        );

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void courseRequest_shouldAcceptValidCourse() {
        CourseRequest request = new CourseRequest(
                "Java Programming",
                3,
                30
        );

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    // ============================================================
    // COURSE SERVICE TESTS
    // ============================================================

    @Test
    void createCourse_shouldCreateSuccessfully() {
        CourseRequest request = new CourseRequest(
                "Java Programming",
                3,
                30
        );

        Course course = new Course(
                "Java Programming",
                3,
                30
        );
        course.setId(1L);

        when(courseRepository.existsByNameIgnoreCase("Java Programming"))
                .thenReturn(false);

        when(courseRepository.save(any(Course.class)))
                .thenReturn(course);

        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(0L);

        CourseResponse response =
                courseService.createCourse(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Java Programming", response.getName());
        assertEquals(3, response.getCredits());
        assertEquals(30, response.getCapacity());
        assertEquals(0, response.getEnrolledCount());
        assertEquals(30, response.getAvailableSeats());

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_shouldTrimCourseName() {
        CourseRequest request = new CourseRequest(
                "  Java Programming  ",
                3,
                30
        );

        Course course = new Course(
                "Java Programming",
                3,
                30
        );
        course.setId(1L);

        when(courseRepository.existsByNameIgnoreCase("Java Programming"))
                .thenReturn(false);

        when(courseRepository.save(any(Course.class)))
                .thenReturn(course);

        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(0L);

        CourseResponse response =
                courseService.createCourse(request);

        assertEquals("Java Programming", response.getName());
    }

    @Test
    void createCourse_shouldThrowExceptionForDuplicateCourse() {
        CourseRequest request = new CourseRequest(
                "Java Programming",
                3,
                30
        );

        when(courseRepository.existsByNameIgnoreCase("Java Programming"))
                .thenReturn(true);

        assertThrows(
                DuplicateCourseException.class,
                () -> courseService.createCourse(request)
        );

        verify(courseRepository, never()).save(any());
    }

    @Test
    void getCourse_shouldReturnCourseSuccessfully() {
        Course course = new Course(
                "Spring Boot",
                4,
                40
        );
        course.setId(1L);

        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(10L);

        CourseResponse response =
                courseService.getCourse(1L);

        assertEquals(1L, response.getId());
        assertEquals("Spring Boot", response.getName());
        assertEquals(4, response.getCredits());
        assertEquals(40, response.getCapacity());
        assertEquals(10, response.getEnrolledCount());
        assertEquals(30, response.getAvailableSeats());
    }

    @Test
    void getCourse_shouldThrowExceptionWhenCourseDoesNotExist() {
        when(courseRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                CourseNotFoundException.class,
                () -> courseService.getCourse(999L)
        );
    }

    @Test
    void getAllCourses_shouldReturnAllCourses() {
        Course course1 = new Course(
                "Java",
                3,
                30
        );
        course1.setId(1L);

        Course course2 = new Course(
                "Spring Boot",
                4,
                40
        );
        course2.setId(2L);

        when(courseRepository.findAll())
                .thenReturn(List.of(course1, course2));

        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(5L);

        when(enrollmentRepository.countByCourseId(2L))
                .thenReturn(10L);

        List<CourseResponse> courses =
                courseService.getAllCourses();

        assertEquals(2, courses.size());
        assertEquals("Java", courses.get(0).getName());
        assertEquals("Spring Boot", courses.get(1).getName());
    }

    @Test
    void updateCourse_shouldUpdateSuccessfully() {
        Course existingCourse = new Course(
                "Java",
                3,
                30
        );
        existingCourse.setId(1L);

        CourseRequest request = new CourseRequest(
                "Advanced Java",
                4,
                50
        );

        when(courseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(existingCourse));

        when(courseRepository.existsByNameIgnoreCaseAndIdNot(
                "Advanced Java",
                1L
        )).thenReturn(false);

        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(10L);

        when(courseRepository.save(existingCourse))
                .thenReturn(existingCourse);

        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(10L);

        CourseResponse response =
                courseService.updateCourse(1L, request);

        assertEquals("Advanced Java", response.getName());
        assertEquals(4, response.getCredits());
        assertEquals(50, response.getCapacity());
        assertEquals(10, response.getEnrolledCount());
        assertEquals(40, response.getAvailableSeats());
    }

    @Test
    void updateCourse_shouldThrowExceptionWhenCourseDoesNotExist() {
        when(courseRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        CourseRequest request =
                new CourseRequest("Java", 3, 30);

        assertThrows(
                CourseNotFoundException.class,
                () -> courseService.updateCourse(99L, request)
        );
    }

    @Test
    void updateCourse_shouldRejectDuplicateCourseName() {
        Course existingCourse = new Course(
                "Java",
                3,
                30
        );
        existingCourse.setId(1L);

        when(courseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(existingCourse));

        when(courseRepository.existsByNameIgnoreCaseAndIdNot(
                "Spring",
                1L
        )).thenReturn(true);

        CourseRequest request =
                new CourseRequest("Spring", 4, 30);

        assertThrows(
                DuplicateCourseException.class,
                () -> courseService.updateCourse(1L, request)
        );
    }

    @Test
    void updateCourse_shouldRejectCapacityLessThanCurrentEnrollment() {
        Course existingCourse = new Course(
                "Java",
                3,
                30
        );
        existingCourse.setId(1L);

        when(courseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(existingCourse));

        when(courseRepository.existsByNameIgnoreCaseAndIdNot(
                "Java",
                1L
        )).thenReturn(false);

        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(20L);

        CourseRequest request =
                new CourseRequest("Java", 3, 10);

        assertThrows(
                InvalidCourseCapacityException.class,
                () -> courseService.updateCourse(1L, request)
        );

        verify(courseRepository, never()).save(any());
    }

    @Test
    void deleteCourse_shouldDeleteCourseAndItsEnrollments() {
        Course course = new Course(
                "Java",
                3,
                30
        );
        course.setId(1L);

        when(courseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(course));

        courseService.deleteCourse(1L);

        verify(enrollmentRepository)
                .deleteByCourseId(1L);

        verify(courseRepository)
                .delete(course);
    }

    // ============================================================
    // STUDENT VALIDATION TESTS
    // ============================================================

    @Test
    void studentRequest_shouldRejectInvalidEmail() {
        StudentRequest request =
                new StudentRequest(
                        "John",
                        "invalid-email"
                );

        Set<ConstraintViolation<StudentRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void studentRequest_shouldRejectNumericOnlyName() {
        StudentRequest request =
                new StudentRequest(
                        "12345",
                        "john@example.com"
                );

        Set<ConstraintViolation<StudentRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void studentRequest_shouldAcceptValidStudent() {
        StudentRequest request =
                new StudentRequest(
                        "John Doe",
                        "john@example.com"
                );

        Set<ConstraintViolation<StudentRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    // ============================================================
    // STUDENT SERVICE TESTS
    // ============================================================


    @Test
    void getStudent_shouldReturnStudentSuccessfully() {
        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        StudentResponse response =
                studentService.getStudent(1L);

        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
    }

    @Test
    void getStudent_shouldThrowExceptionWhenStudentDoesNotExist() {
        when(studentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                StudentNotFoundException.class,
                () -> studentService.getStudent(999L)
        );
    }

    @Test
    void getAllStudents_shouldReturnAllStudents() {
        Student student1 =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student1.setId(1L);

        Student student2 =
                new Student(
                        "Jane Doe",
                        "jane@example.com"
                );
        student2.setId(2L);

        when(studentRepository.findAll())
                .thenReturn(List.of(student1, student2));

        List<StudentResponse> students =
                studentService.getAllStudents();

        assertEquals(2, students.size());
        assertEquals("John Doe", students.get(0).getName());
        assertEquals("Jane Doe", students.get(1).getName());
    }

    @Test
    void getCurrentStudent_shouldReturnStudent() {
        User user =
                new User(
                        "john123",
                        "password",
                        Role.STUDENT
                );

        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);
        student.setUser(user);

        when(studentRepository.findByUsername("john123"))
                .thenReturn(Optional.of(student));

        StudentResponse response =
                studentService.getCurrentStudent("john123");

        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
    }

    @Test
    void updateStudent_shouldUpdateSuccessfully() {
        Student student =
                new Student(
                        "John",
                        "john@example.com"
                );
        student.setId(1L);

        StudentRequest request =
                new StudentRequest(
                        "John Smith",
                        "johnsmith@example.com"
                );

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(studentRepository.existsByEmailIgnoreCaseAndIdNot(
                "johnsmith@example.com",
                1L
        )).thenReturn(false);

        when(studentRepository.save(student))
                .thenReturn(student);

        StudentResponse response =
                studentService.updateStudent(1L, request);

        assertEquals("John Smith", response.getName());
        assertEquals("johnsmith@example.com", response.getEmail());
    }

    @Test
    void updateStudent_shouldRejectDuplicateEmail() {
        Student student =
                new Student(
                        "John",
                        "john@example.com"
                );
        student.setId(1L);

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(studentRepository.existsByEmailIgnoreCaseAndIdNot(
                "jane@example.com",
                1L
        )).thenReturn(true);

        StudentRequest request =
                new StudentRequest(
                        "John",
                        "jane@example.com"
                );

        assertThrows(
                DuplicateStudentException.class,
                () -> studentService.updateStudent(1L, request)
        );
    }

    @Test
    void deleteStudent_shouldDeleteStudentEnrollmentsAndUser() {
        User user =
                new User(
                        "john123",
                        "password",
                        Role.STUDENT
                );
        user.setId(10L);

        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);
        student.setUser(user);

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        studentService.deleteStudent(1L);

        verify(enrollmentRepository)
                .deleteByStudentId(1L);

        verify(studentRepository)
                .delete(student);

        verify(studentRepository)
                .flush();

        verify(userRepository)
                .delete(user);
    }

    @Test
    void getStudentCourses_shouldReturnEnrolledCourses() {
        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);

        Course course =
                new Course(
                        "Java",
                        3,
                        30
                );
        course.setId(10L);

        Enrollment enrollment =
                new Enrollment(
                        student,
                        course,
                        LocalDateTime.now()
                );

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(enrollmentRepository.findByStudentIdWithCourse(1L))
                .thenReturn(List.of(enrollment));

        when(enrollmentRepository.countByCourseId(10L))
                .thenReturn(5L);

        List<CourseResponse> courses =
                studentService.getStudentCourses(1L);

        assertEquals(1, courses.size());
        assertEquals("Java", courses.get(0).getName());
        assertEquals(30, courses.get(0).getCapacity());
        assertEquals(5, courses.get(0).getEnrolledCount());
        assertEquals(25, courses.get(0).getAvailableSeats());
    }

    @Test
    void getStudentCourses_shouldThrowExceptionWhenStudentDoesNotExist() {
        when(studentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                StudentNotFoundException.class,
                () -> studentService.getStudentCourses(999L)
        );
    }

    // ============================================================
    // ENROLLMENT VALIDATION TESTS
    // ============================================================

    @Test
    void enrollmentRequest_shouldRejectNegativeStudentId() {
        EnrollmentRequest request =
                new EnrollmentRequest(
                        -1L,
                        10L
                );

        Set<ConstraintViolation<EnrollmentRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void enrollmentRequest_shouldRejectNegativeCourseId() {
        EnrollmentRequest request =
                new EnrollmentRequest(
                        1L,
                        -10L
                );

        Set<ConstraintViolation<EnrollmentRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void enrollmentRequest_shouldAcceptValidIds() {
        EnrollmentRequest request =
                new EnrollmentRequest(
                        1L,
                        10L
                );

        Set<ConstraintViolation<EnrollmentRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    // ============================================================
    // ENROLLMENT SERVICE TESTS
    // ============================================================

    @Test
    void enrollStudent_shouldEnrollSuccessfully() {
        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);

        Course course =
                new Course(
                        "Java",
                        3,
                        30
                );
        course.setId(10L);

        Enrollment enrollment =
                new Enrollment(
                        student,
                        course,
                        LocalDateTime.now()
                );
        enrollment.setId(100L);

        EnrollmentRequest request =
                new EnrollmentRequest(
                        1L,
                        10L
                );

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourseId(
                1L,
                10L
        )).thenReturn(false);

        when(enrollmentRepository.countByCourseId(10L))
                .thenReturn(5L);

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenReturn(enrollment);

        EnrollmentResponse response =
                enrollmentService.enrollStudent(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getStudentId());
        assertEquals(10L, response.getCourseId());
        assertEquals("John Doe", response.getStudentName());
        assertEquals("Java", response.getCourseName());
    }

    @Test
    void enrollStudent_shouldThrowExceptionWhenStudentDoesNotExist() {
        EnrollmentRequest request =
                new EnrollmentRequest(
                        999L,
                        10L
                );

        when(studentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                StudentNotFoundException.class,
                () -> enrollmentService.enrollStudent(request)
        );
    }

    @Test
    void enrollStudent_shouldThrowExceptionWhenCourseDoesNotExist() {
        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);

        EnrollmentRequest request =
                new EnrollmentRequest(
                        1L,
                        999L
                );

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                CourseNotFoundException.class,
                () -> enrollmentService.enrollStudent(request)
        );
    }

    @Test
    void enrollStudent_shouldRejectDuplicateEnrollment() {
        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);

        Course course =
                new Course(
                        "Java",
                        3,
                        30
                );
        course.setId(10L);

        EnrollmentRequest request =
                new EnrollmentRequest(
                        1L,
                        10L
                );

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourseId(
                1L,
                10L
        )).thenReturn(true);

        assertThrows(
                DuplicateEnrollmentException.class,
                () -> enrollmentService.enrollStudent(request)
        );

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_shouldRejectEnrollmentWhenCourseIsFull() {
        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);

        Course course =
                new Course(
                        "Java",
                        3,
                        30
                );
        course.setId(10L);

        EnrollmentRequest request =
                new EnrollmentRequest(
                        1L,
                        10L
                );

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourseId(
                1L,
                10L
        )).thenReturn(false);

        when(enrollmentRepository.countByCourseId(10L))
                .thenReturn(30L);

        assertThrows(
                CourseCapacityExceededException.class,
                () -> enrollmentService.enrollStudent(request)
        );

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollCurrentStudent_shouldEnrollSuccessfully() {
        User user =
                new User(
                        "john123",
                        "password",
                        Role.STUDENT
                );

        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);
        student.setUser(user);

        Course course =
                new Course(
                        "Spring Boot",
                        4,
                        40
                );
        course.setId(10L);

        Enrollment enrollment =
                new Enrollment(
                        student,
                        course,
                        LocalDateTime.now()
                );
        enrollment.setId(100L);

        when(studentRepository.findByUsername("john123"))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourseId(
                1L,
                10L
        )).thenReturn(false);

        when(enrollmentRepository.countByCourseId(10L))
                .thenReturn(5L);

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenReturn(enrollment);

        EnrollmentResponse response =
                enrollmentService.enrollCurrentStudent(
                        "john123",
                        10L
                );

        assertNotNull(response);
        assertEquals("Spring Boot", response.getCourseName());
    }

    @Test
    void deleteEnrollment_shouldDeleteSuccessfully() {
        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);

        Course course =
                new Course(
                        "Java",
                        3,
                        30
                );
        course.setId(10L);

        Enrollment enrollment =
                new Enrollment(
                        student,
                        course,
                        LocalDateTime.now()
                );
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L))
                .thenReturn(Optional.of(enrollment));

        enrollmentService.deleteEnrollment(100L);

        verify(enrollmentRepository)
                .delete(enrollment);
    }

    @Test
    void deleteEnrollment_shouldThrowExceptionWhenEnrollmentDoesNotExist() {
        when(enrollmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EnrollmentNotFoundException.class,
                () -> enrollmentService.deleteEnrollment(999L)
        );
    }

    @Test
    void deleteCurrentStudentEnrollment_shouldAllowOwnerToDeleteEnrollment() {
        User user =
                new User(
                        "john123",
                        "password",
                        Role.STUDENT
                );

        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);
        student.setUser(user);

        Course course =
                new Course(
                        "Java",
                        3,
                        30
                );
        course.setId(10L);

        Enrollment enrollment =
                new Enrollment(
                        student,
                        course,
                        LocalDateTime.now()
                );
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L))
                .thenReturn(Optional.of(enrollment));

        enrollmentService.deleteCurrentStudentEnrollment(
                100L,
                "john123"
        );

        verify(enrollmentRepository)
                .delete(enrollment);
    }

    @Test
    void deleteCurrentStudentEnrollment_shouldRejectDifferentStudent() {
        User user =
                new User(
                        "john123",
                        "password",
                        Role.STUDENT
                );

        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);
        student.setUser(user);

        Course course =
                new Course(
                        "Java",
                        3,
                        30
                );
        course.setId(10L);

        Enrollment enrollment =
                new Enrollment(
                        student,
                        course,
                        LocalDateTime.now()
                );
        enrollment.setId(100L);

        when(enrollmentRepository.findById(100L))
                .thenReturn(Optional.of(enrollment));

        assertThrows(
                EnrollmentNotFoundException.class,
                () -> enrollmentService.deleteCurrentStudentEnrollment(
                        100L,
                        "anotherUser"
                )
        );

        verify(enrollmentRepository, never())
                .delete(enrollment);
    }

    @Test
    void getAllEnrollments_shouldReturnAllEnrollments() {
        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);

        Course course =
                new Course(
                        "Java",
                        3,
                        30
                );
        course.setId(10L);

        Enrollment enrollment =
                new Enrollment(
                        student,
                        course,
                        LocalDateTime.now()
                );
        enrollment.setId(100L);

        when(enrollmentRepository.findAllWithStudentAndCourse())
                .thenReturn(List.of(enrollment));

        List<EnrollmentResponse> enrollments =
                enrollmentService.getAllEnrollments();

        assertEquals(1, enrollments.size());
        assertEquals(100L, enrollments.get(0).getId());
        assertEquals("John Doe", enrollments.get(0).getStudentName());
        assertEquals("Java", enrollments.get(0).getCourseName());
    }

    // ============================================================
    // AUTH VALIDATION TESTS
    // ============================================================

    @Test
    void registerRequest_shouldRejectInvalidUsername() {
        RegisterRequest request =
                new RegisterRequest(
                        "ab",
                        "password123",
                        "John Doe",
                        "john@example.com"
                );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void registerRequest_shouldRejectInvalidEmail() {
        RegisterRequest request =
                new RegisterRequest(
                        "john123",
                        "password123",
                        "John Doe",
                        "invalid-email"
                );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void registerRequest_shouldRejectShortPassword() {
        RegisterRequest request =
                new RegisterRequest(
                        "john123",
                        "123",
                        "John Doe",
                        "john@example.com"
                );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void registerRequest_shouldAcceptValidRequest() {
        RegisterRequest request =
                new RegisterRequest(
                        "john123",
                        "password123",
                        "John Doe",
                        "john@example.com"
                );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    // ============================================================
    // AUTH SERVICE TESTS
    // ============================================================

    @Test
    void register_shouldCreateUserAndStudentSuccessfully() {
        RegisterRequest request =
                new RegisterRequest(
                        "john123",
                        "password123",
                        "John Doe",
                        "john@example.com"
                );

        User user =
                new User(
                        "john123",
                        "encodedPassword",
                        Role.STUDENT
                );
        user.setId(1L);

        Student student =
                new Student(
                        "John Doe",
                        "john@example.com"
                );
        student.setId(1L);
        student.setUser(user);

        when(userRepository.existsByUsernameIgnoreCase("john123"))
                .thenReturn(false);

        when(studentRepository.existsByEmailIgnoreCase("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(studentRepository.save(any(Student.class)))
                .thenReturn(student);

        UserResponse response =
                authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john123", response.getUsername());
        assertEquals(Role.STUDENT, response.getRole());

        verify(userRepository).save(any(User.class));
        verify(userRepository).flush();
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void register_shouldRejectDuplicateUsername() {
        RegisterRequest request =
                new RegisterRequest(
                        "john123",
                        "password123",
                        "John Doe",
                        "john@example.com"
                );

        when(userRepository.existsByUsernameIgnoreCase("john123"))
                .thenReturn(true);

        assertThrows(
                DuplicateUserException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequest request =
                new RegisterRequest(
                        "john123",
                        "password123",
                        "John Doe",
                        "john@example.com"
                );

        when(userRepository.existsByUsernameIgnoreCase("john123"))
                .thenReturn(false);

        when(studentRepository.existsByEmailIgnoreCase("john@example.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateStudentException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldAuthenticateAndGenerateJwtToken() {
        LoginRequest request =
                new LoginRequest(
                        "john123",
                        "password123"
                );

        UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        "john123",
                        "encodedPassword",
                        List.of(
                                new SimpleGrantedAuthority("ROLE_STUDENT")
                        )
                );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(jwtService.generateToken(userDetails))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService)
                .generateToken(userDetails);
    }
}
