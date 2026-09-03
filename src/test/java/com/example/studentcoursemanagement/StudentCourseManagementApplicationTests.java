package com.example.studentcoursemanagement;

import com.example.studentcoursemanagement.dto.course.CourseRequest;
import com.example.studentcoursemanagement.dto.enrollment.EnrollmentRequest;
import com.example.studentcoursemanagement.dto.student.StudentCreateRequest;
import com.example.studentcoursemanagement.entity.Course;
import com.example.studentcoursemanagement.entity.Enrollment;
import com.example.studentcoursemanagement.entity.Student;
import com.example.studentcoursemanagement.exception.CourseCapacityExceededException;
import com.example.studentcoursemanagement.exception.DuplicateEnrollmentException;
import com.example.studentcoursemanagement.repo.CourseRepository;
import com.example.studentcoursemanagement.repo.EnrollmentRepository;
import com.example.studentcoursemanagement.repo.StudentRepository;
import com.example.studentcoursemanagement.service.EnrollmentService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentCourseManagementApplicationTests {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private Course course;

    private Validator validator;

    @BeforeEach
    void setUp() {

        student = new Student("Sakeeb", "sakeeb@gmail.com");
        student.setId(1L);

        course = new Course("Java Programming", 4, 2);
        course.setId(1L);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ---------------------------------------------------------
    // ENROLLMENT TESTS
    // ---------------------------------------------------------

    @Test
    void shouldEnrollStudentSuccessfully() {

        EnrollmentRequest request =
                new EnrollmentRequest(1L, 1L);

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L))
                .thenReturn(false);

        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(0L);

        Enrollment savedEnrollment =
                new Enrollment(student, course, LocalDateTime.now());

        savedEnrollment.setId(1L);

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenReturn(savedEnrollment);

        var response = enrollmentService.enrollStudent(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getStudentId());
        assertEquals(1L, response.getCourseId());
        assertEquals("Sakeeb", response.getStudentName());
        assertEquals("Java Programming", response.getCourseName());

        verify(studentRepository).findById(1L);
        verify(courseRepository).findByIdForUpdate(1L);
        verify(enrollmentRepository)
                .existsByStudentIdAndCourseId(1L, 1L);
        verify(enrollmentRepository)
                .countByCourseId(1L);
        verify(enrollmentRepository)
                .save(any(Enrollment.class));
    }

    @Test
    void shouldRejectDuplicateEnrollment() {

        EnrollmentRequest request =
                new EnrollmentRequest(1L, 1L);

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L))
                .thenReturn(true);

        DuplicateEnrollmentException exception =
                assertThrows(
                        DuplicateEnrollmentException.class,
                        () -> enrollmentService.enrollStudent(request)
                );

        assertEquals(
                "Student is already enrolled in this course",
                exception.getMessage()
        );

        verify(enrollmentRepository)
                .existsByStudentIdAndCourseId(1L, 1L);

        verify(enrollmentRepository, never())
                .save(any(Enrollment.class));
    }

    @Test
    void shouldRejectEnrollmentWhenCourseCapacityIsFull() {

        EnrollmentRequest request =
                new EnrollmentRequest(1L, 1L);

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L))
                .thenReturn(false);

        // Course capacity = 2
        // Current enrollment = 2
        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(2L);

        CourseCapacityExceededException exception =
                assertThrows(
                        CourseCapacityExceededException.class,
                        () -> enrollmentService.enrollStudent(request)
                );

        assertEquals(
                "Course capacity has been reached",
                exception.getMessage()
        );

        verify(enrollmentRepository)
                .countByCourseId(1L);

        verify(enrollmentRepository, never())
                .save(any(Enrollment.class));
    }

    @Test
    void shouldAllowEnrollmentWhenCourseStillHasCapacity() {

        EnrollmentRequest request =
                new EnrollmentRequest(1L, 1L);

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L))
                .thenReturn(false);

        // Capacity = 2
        // Current enrollment = 1
        when(enrollmentRepository.countByCourseId(1L))
                .thenReturn(1L);

        Enrollment savedEnrollment =
                new Enrollment(student, course, LocalDateTime.now());

        savedEnrollment.setId(2L);

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenReturn(savedEnrollment);

        var response =
                enrollmentService.enrollStudent(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());

        verify(enrollmentRepository)
                .save(any(Enrollment.class));
    }

    // ---------------------------------------------------------
    // ENROLLMENT REQUEST VALIDATION
    // ---------------------------------------------------------

    @Test
    void shouldRejectInvalidEnrollmentRequest() {

        EnrollmentRequest request =
                new EnrollmentRequest(0L, -1L);

        Set<ConstraintViolation<EnrollmentRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("studentId"))
        );

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("courseId"))
        );
    }

    @Test
    void shouldRejectNullEnrollmentIds() {

        EnrollmentRequest request =
                new EnrollmentRequest(null, null);

        Set<ConstraintViolation<EnrollmentRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("studentId"))
        );

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("courseId"))
        );
    }

    // ---------------------------------------------------------
    // COURSE VALIDATION
    // ---------------------------------------------------------

    @Test
    void shouldRejectNumericCourseName() {

        CourseRequest request =
                new CourseRequest("0000", 4, 30);

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("name"))
        );
    }

    @Test
    void shouldRejectInvalidCourseCredits() {

        CourseRequest request =
                new CourseRequest("Java Programming", 0, 30);

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("credits"))
        );
    }

    @Test
    void shouldRejectInvalidCourseCapacity() {

        CourseRequest request =
                new CourseRequest("Java Programming", 4, 0);

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("capacity"))
        );
    }

    @Test
    void shouldAcceptValidCourseRequest() {

        CourseRequest request =
                new CourseRequest("Java Programming", 4, 30);

        Set<ConstraintViolation<CourseRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    // ---------------------------------------------------------
    // STUDENT VALIDATION
    // ---------------------------------------------------------

    @Test
    void shouldRejectNumericUsername() {

        StudentCreateRequest request =
                new StudentCreateRequest();

        request.setName("Sakeeb");
        request.setUsername("0000");
        request.setPassword("password123");
        request.setEmail("sakeeb@gmail.com");

        Set<ConstraintViolation<StudentCreateRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("username"))
        );
    }

    @Test
    void shouldAcceptValidUsername() {

        StudentCreateRequest request =
                new StudentCreateRequest();

        request.setName("Sakeeb");
        request.setUsername("sakeeb123");
        request.setPassword("password123");
        request.setEmail("sakeeb@gmail.com");

        Set<ConstraintViolation<StudentCreateRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .noneMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("username"))
        );
    }

    @Test
    void shouldRejectInvalidEmail() {

        StudentCreateRequest request =
                new StudentCreateRequest();

        request.setName("Sakeeb");
        request.setUsername("sakeeb123");
        request.setPassword("password123");
        request.setEmail("invalid-email");

        Set<ConstraintViolation<StudentCreateRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("email"))
        );
    }

    @Test
    void shouldRejectMissingPassword() {

        StudentCreateRequest request =
                new StudentCreateRequest();

        request.setName("Sakeeb");
        request.setUsername("sakeeb123");
        request.setPassword("");
        request.setEmail("sakeeb@gmail.com");

        Set<ConstraintViolation<StudentCreateRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath().toString()
                                        .equals("password"))
        );
    }
}

