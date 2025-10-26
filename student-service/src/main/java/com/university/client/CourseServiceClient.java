package com.university.client;

import com.university.DTO.CourseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(
        name = "course-service",
        path = "/api/courses",
        fallback = CourseServiceFallback.class
)
public interface CourseServiceClient {

    @GetMapping("/student/{studentId}")
    List<CourseDTO> getCoursesByStudentId(@PathVariable("studentId") Long studentId);

    @GetMapping("/{courseId}")
    CourseDTO getCourseById(@PathVariable("courseId") Long courseId);

    @GetMapping("/teacher/{teacherId}")
    List<CourseDTO> getCoursesByTeacherId(@PathVariable("teacherId") Long teacherId);
}
