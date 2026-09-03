package com.example.studentcoursemanagement.service;

import com.example.studentcoursemanagement.dto.enrollment.EnrollmentRequest;
import com.example.studentcoursemanagement.dto.enrollment.EnrollmentResponse;
import com.example.studentcoursemanagement.entity.Course;
import com.example.studentcoursemanagement.entity.Enrollment;
import com.example.studentcoursemanagement.entity.Student;
import com.example.studentcoursemanagement.exception.CourseCapacityExceededException;
import com.example.studentcoursemanagement.exception.CourseNotFoundException;
import com.example.studentcoursemanagement.exception.DuplicateEnrollmentException;
import com.example.studentcoursemanagement.exception.EnrollmentNotFoundException;
import com.example.studentcoursemanagement.exception.StudentNotFoundException;
import com.example.studentcoursemanagement.repo.CourseRepository;
import com.example.studentcoursemanagement.repo.EnrollmentRepository;
import com.example.studentcoursemanagement.repo.StudentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(
                        "Student not found with id: " + request.getStudentId()
                ));

        return createEnrollment(student, request.getCourseId());
    }

    @Transactional
    public EnrollmentResponse enrollCurrentStudent(String username, Long courseId) {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new StudentNotFoundException(
                        "No student profile is linked to user: " + username
                ));

        return createEnrollment(student, courseId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAllWithStudentAndCourse()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void deleteEnrollment(Long enrollmentId) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        enrollmentRepository.delete(enrollment);
    }

    @Transactional
    public void deleteCurrentStudentEnrollment(Long enrollmentId, String username) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        String ownerUsername = enrollment.getStudent().getUser() == null
                ? null
                : enrollment.getStudent().getUser().getUsername();

        if (ownerUsername == null || !ownerUsername.equalsIgnoreCase(username)) {
            throw new EnrollmentNotFoundException("Enrollment not found");
        }

        enrollmentRepository.delete(enrollment);
    }

    private EnrollmentResponse createEnrollment(Student student, Long courseId) {
        Course course = courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new CourseNotFoundException(
                        "Course not found with id: " + courseId
                ));

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new DuplicateEnrollmentException("Student is already enrolled in this course");
        }

        long currentEnrollmentCount = enrollmentRepository.countByCourseId(courseId);
        if (currentEnrollmentCount >= course.getCapacity()) {
            throw new CourseCapacityExceededException("Course capacity has been reached");
        }

        Enrollment enrollment = new Enrollment(student, course, LocalDateTime.now());

        try {
            return mapToResponse(enrollmentRepository.save(enrollment));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEnrollmentException("Student is already enrolled in this course");
        }
    }

    private Enrollment findEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(
                        "Enrollment not found with id: " + enrollmentId
                ));
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getName(),
                enrollment.getEnrolledAt()
        );
    }
}
