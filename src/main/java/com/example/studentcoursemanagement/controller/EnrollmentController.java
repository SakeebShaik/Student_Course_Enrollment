package com.example.studentcoursemanagement.controller;

import com.example.studentcoursemanagement.dto.enrollment.EnrollmentRequest;
import com.example.studentcoursemanagement.dto.enrollment.EnrollmentResponse;
import com.example.studentcoursemanagement.service.EnrollmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enrollStudent(request));
    }

    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/{courseId}")
    public ResponseEntity<EnrollmentResponse> enrollCurrentStudent(
            @PathVariable @NotNull Long courseId,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.enrollCurrentStudent(authentication.getName(), courseId));
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> deleteCurrentStudentEnrollment(
            @PathVariable Long id,
            Authentication authentication) {
        enrollmentService.deleteCurrentStudentEnrollment(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
