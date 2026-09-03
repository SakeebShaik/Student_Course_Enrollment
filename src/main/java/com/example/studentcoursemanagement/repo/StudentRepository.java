package com.example.studentcoursemanagement.repo;

import com.example.studentcoursemanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("select s from Student s join fetch s.user u where lower(u.username) = lower(:username)")
    Optional<Student> findByUsername(@Param("username") String username);
}
