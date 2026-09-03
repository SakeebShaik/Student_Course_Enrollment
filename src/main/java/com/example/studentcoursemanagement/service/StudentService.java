package com.example.studentcoursemanagement.service;

import com.example.studentcoursemanagement.dto.course.CourseResponse;
import com.example.studentcoursemanagement.dto.student.StudentCreateRequest;
import com.example.studentcoursemanagement.dto.student.StudentRequest;
import com.example.studentcoursemanagement.dto.student.StudentResponse;
import com.example.studentcoursemanagement.entity.Course;
import com.example.studentcoursemanagement.entity.Student;
import com.example.studentcoursemanagement.entity.Role;
import com.example.studentcoursemanagement.entity.User;
import com.example.studentcoursemanagement.exception.DuplicateStudentException;
import com.example.studentcoursemanagement.exception.DuplicateUserException;
import com.example.studentcoursemanagement.exception.StudentNotFoundException;
import com.example.studentcoursemanagement.repo.EnrollmentRepository;
import com.example.studentcoursemanagement.repo.StudentRepository;
import com.example.studentcoursemanagement.repo.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          EnrollmentRepository enrollmentRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateUserException("Username already exists");
        }

        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateStudentException("Student with this email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        try {
            User savedUser = userRepository.save(user);
            userRepository.flush();

            Student student = new Student();
            student.setName(request.getName().trim());
            student.setEmail(email);
            student.setUser(savedUser);

            return mapToResponse(studentRepository.save(student));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateStudentException("Student with this email or username already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long id) {
        return mapToResponse(findStudent(id));
    }

    @Transactional(readOnly = true)
    public StudentResponse getCurrentStudent(String username) {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new StudentNotFoundException(
                        "No student profile is linked to user: " + username
                ));
        return mapToResponse(student);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findStudent(id);
        String email = request.getEmail().trim().toLowerCase();

        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateStudentException("Student with this email already exists");
        }

        student.setName(request.getName().trim());
        student.setEmail(email);

        try {
            return mapToResponse(studentRepository.save(student));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateStudentException("Student with this email already exists");
        }
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = findStudent(id);
        User linkedUser = student.getUser();

        enrollmentRepository.deleteByStudentId(id);
        studentRepository.delete(student);
        studentRepository.flush();

        if (linkedUser != null) {
            userRepository.delete(linkedUser);
        }
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getStudentCourses(Long studentId) {
        findStudent(studentId);
        return enrollmentRepository.findByStudentIdWithCourse(studentId)
                .stream()
                .map(enrollment -> mapCourse(enrollment.getCourse()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCurrentStudentCourses(String username) {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new StudentNotFoundException(
                        "No student profile is linked to user: " + username
                ));
        return getStudentCourses(student.getId());
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(
                        "Student not found with id: " + id
                ));
    }

    private StudentResponse mapToResponse(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getEmail());
    }

    private CourseResponse mapCourse(Course course) {
        long enrolledCount = enrollmentRepository.countByCourseId(course.getId());
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getCredits(),
                course.getCapacity(),
                enrolledCount,
                Math.max(0, course.getCapacity() - enrolledCount)
        );
    }
}
