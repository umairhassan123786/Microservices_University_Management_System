package com.university.client;
import com.university.DTO.CourseDTO;
import com.university.DTO.StudentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

@FeignClient(name = "course-service", url = "http://localhost:8084")
public interface CourseServiceClient {

    @GetMapping("/api/courses/{id}")
    CourseDTO getCourseById(@PathVariable("id") Long id);

    @GetMapping("/api/courses/{courseId}/students")
    List<StudentDTO> getCourseStudents(@PathVariable("courseId") Long courseId);

    @GetMapping("/api/courses/teacher/{teacherId}")
    List<CourseDTO> getCoursesByTeacherId(@PathVariable("teacherId") Long teacherId);

    @GetMapping("/api/courses/{courseId}/student-details")
    List<Map<String, Object>> getCourseStudentDetails(@PathVariable("courseId") Long courseId);
}