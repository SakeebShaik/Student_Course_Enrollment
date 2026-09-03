package com.example.studentcoursemanagement.service;

import com.example.studentcoursemanagement.dto.course.CourseRequest;
import com.example.studentcoursemanagement.dto.course.CourseResponse;
import com.example.studentcoursemanagement.entity.Course;
import com.example.studentcoursemanagement.exception.CourseNotFoundException;
import com.example.studentcoursemanagement.exception.DuplicateCourseException;
import com.example.studentcoursemanagement.exception.InvalidCourseCapacityException;
import com.example.studentcoursemanagement.repo.CourseRepository;
import com.example.studentcoursemanagement.repo.EnrollmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseService(CourseRepository courseRepository,
                         EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        String name = request.getName().trim();

        if (courseRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateCourseException("A course with this name already exists");
        }

        Course course = new Course();
        course.setName(name);
        course.setCredits(request.getCredits());
        course.setCapacity(request.getCapacity());

        try {
            return mapToResponse(courseRepository.save(course));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCourseException("A course with this name already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long id) {
        Course course = findCourse(id);
        return mapToResponse(course);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new CourseNotFoundException(
                        "Course not found with id: " + id
                ));
        String name = request.getName().trim();

        if (courseRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateCourseException("A course with this name already exists");
        }

        long enrolledCount = enrollmentRepository.countByCourseId(id);
        if (request.getCapacity() < enrolledCount) {
            throw new InvalidCourseCapacityException(
                    "Capacity cannot be less than the current enrollment count of " + enrolledCount
            );
        }

        course.setName(name);
        course.setCredits(request.getCredits());
        course.setCapacity(request.getCapacity());

        try {
            return mapToResponse(courseRepository.save(course));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCourseException("A course with this name already exists");
        }
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new CourseNotFoundException(
                        "Course not found with id: " + id
                ));
        enrollmentRepository.deleteByCourseId(id);
        courseRepository.delete(course);
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(
                        "Course not found with id: " + id
                ));
    }

    private CourseResponse mapToResponse(Course course) {
        long enrolledCount = enrollmentRepository.countByCourseId(course.getId());
        long availableSeats = Math.max(0, course.getCapacity() - enrolledCount);

        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getCredits(),
                course.getCapacity(),
                enrolledCount,
                availableSeats
        );
    }
}
