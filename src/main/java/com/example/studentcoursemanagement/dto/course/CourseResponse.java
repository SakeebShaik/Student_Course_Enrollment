package com.example.studentcoursemanagement.dto.course;

public class CourseResponse {

    private Long id;
    private String name;
    private Integer credits;
    private Integer capacity;
    private long enrolledCount;
    private long availableSeats;

    public CourseResponse() {
    }

    public CourseResponse(Long id, String name, Integer credits, Integer capacity) {
        this(id, name, credits, capacity, 0, capacity == null ? 0 : capacity);
    }

    public CourseResponse(Long id, String name, Integer credits, Integer capacity,
                          long enrolledCount, long availableSeats) {
        this.id = id;
        this.name = name;
        this.credits = credits;
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
        this.availableSeats = availableSeats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public long getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(long enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public long getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(long availableSeats) {
        this.availableSeats = availableSeats;
    }
}
