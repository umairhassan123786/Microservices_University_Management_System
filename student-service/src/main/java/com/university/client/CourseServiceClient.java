package com.university.client;
import com.university.DTO.CourseServiceResponseDTO;
import com.university.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(
        name = "course-service",
        path = "/api/courses",
        configuration = FeignConfig.class,
        fallback = CourseServiceFallback.class
)
public interface CourseServiceClient {

    @GetMapping("/student/{studentId}")
    List<CourseServiceResponseDTO> getCoursesByStudentId(@PathVariable("studentId") Long studentId);

    @GetMapping("/{courseId}")
    CourseServiceResponseDTO getCourseById(@PathVariable("courseId") Long courseId);

    @GetMapping("/teacher/{teacherId}")
    List<CourseServiceResponseDTO> getCoursesByTeacherId(@PathVariable("teacherId") Long teacherId);
}