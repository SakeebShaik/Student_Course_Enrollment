package com.example.studentcoursemanagement.controller;

import com.example.studentcoursemanagement.dto.course.CourseResponse;
import com.example.studentcoursemanagement.dto.student.StudentCreateRequest;
import com.example.studentcoursemanagement.dto.student.StudentRequest;
import com.example.studentcoursemanagement.dto.student.StudentResponse;
import com.example.studentcoursemanagement.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudent(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id,
                                                         @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<List<CourseResponse>> getStudentCourses(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentCourses(id));
    }

    @GetMapping("/me")
    public ResponseEntity<StudentResponse> getCurrentStudent(Authentication authentication) {
        return ResponseEntity.ok(studentService.getCurrentStudent(authentication.getName()));
    }

    @GetMapping("/me/courses")
    public ResponseEntity<List<CourseResponse>> getCurrentStudentCourses(Authentication authentication) {
        return ResponseEntity.ok(studentService.getCurrentStudentCourses(authentication.getName()));
    }
}
