package com.example.studentcoursemanagement.exception;

public class CourseCapacityExceededException extends RuntimeException {

    public CourseCapacityExceededException(String message) {
        super(message);
    }
}