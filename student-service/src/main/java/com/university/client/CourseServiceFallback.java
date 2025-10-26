package com.university.client;

import com.university.DTO.CourseDTO;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class CourseServiceFallback implements CourseServiceClient {

    @Override
    public List<CourseDTO> getCoursesByStudentId(Long studentId) {
        CourseDTO fallbackCourse1 = new CourseDTO();
        fallbackCourse1.setId(1L);
        fallbackCourse1.setName("Course Information Temporarily Unavailable");
        fallbackCourse1.setCode("N/A");
        fallbackCourse1.setDescription("Course service is currently unavailable. Please try again later.");
        return Arrays.asList(fallbackCourse1);
    }

    @Override
    public CourseDTO getCourseById(Long courseId) {
        CourseDTO fallbackCourse = new CourseDTO();
        fallbackCourse.setId(courseId);
        fallbackCourse.setName("Course Information Unavailable");
        fallbackCourse.setCode("N/A");
        fallbackCourse.setDescription("Course service is temporarily unavailable");
        fallbackCourse.setCredits(0);
        return fallbackCourse;
    }

    @Override
    public List<CourseDTO> getCoursesByTeacherId(Long teacherId) {
        return Arrays.asList();
    }
}
