package com.example.studentcoursemanagement.repo;

import com.example.studentcoursemanagement.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByCourseId(Long courseId);

    @Query("select e from Enrollment e join fetch e.course where e.student.id = :studentId")
    List<Enrollment> findByStudentIdWithCourse(@Param("studentId") Long studentId);

    @Query("select e from Enrollment e join fetch e.student join fetch e.course order by e.enrolledAt desc")
    List<Enrollment> findAllWithStudentAndCourse();

    @Modifying
    @Query("delete from Enrollment e where e.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("delete from Enrollment e where e.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}
