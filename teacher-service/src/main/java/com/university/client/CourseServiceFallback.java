package com.university.client;

import com.university.DTO.CourseDTO;
import com.university.DTO.StudentDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CourseServiceFallback implements CourseServiceClient {

    @Override
    public List<Map<String, Object>> getCoursesByTeacherId(Long teacherId) {
        System.out.println("⚠️ Course service down - Returning empty courses list for teacher: " + teacherId);

        // Return empty list with error info
        Map<String, Object> errorCourse = new HashMap<>();
        errorCourse.put("id", -1L);
        errorCourse.put("courseName", "Course Service Unavailable");
        errorCourse.put("department", "Service Down");
        errorCourse.put("error", true);
        errorCourse.put("message", "Course service is currently unavailable");

        return List.of(errorCourse);
    }

    @Override
    public List<StudentDTO> getCourseStudents(Long courseId) {
        System.out.println("⚠️ Course service down - Returning empty students list for course: " + courseId);
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getCourseStudentDetails(Long courseId) {
        System.out.println("⚠️ Course service down - Returning empty student details for course: " + courseId);

        // Return meaningful error response
        Map<String, Object> errorStudent = new HashMap<>();
        errorStudent.put("id", -1L);
        errorStudent.put("name", "Student Data Unavailable");
        errorStudent.put("email", "service@unavailable.com");
        errorStudent.put("rollNumber", "N/A");
        errorStudent.put("error", true);
        errorStudent.put("message", "Course service is currently down");

        return List.of(errorStudent);
    }

    @Override
    public CourseDTO getCourseById(Long courseId) {
        System.out.println("Course service down - Returning fallback course for ID: " + courseId);

        CourseDTO fallbackCourse = new CourseDTO();
        fallbackCourse.setId(courseId);
        fallbackCourse.setName("Course Information Unavailable");
        fallbackCourse.setCode("SERVICE-DOWN");
        fallbackCourse.setCredits(0);
        return fallbackCourse;
    }
}