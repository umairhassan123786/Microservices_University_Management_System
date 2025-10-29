package com.university.client;
import com.university.DTO.StudentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

@FeignClient(name = "student-service", url = "http://localhost:8083",
        fallback = StudentServiceFallback.class)
public interface StudentServiceClient {

    @GetMapping("/api/students/{id}")
    StudentDTO getStudentById(@PathVariable("id") Long id);

    @GetMapping("/api/students")
    List<StudentDTO> getAllStudents();

    @GetMapping("/api/students/department/{department}")
    List<StudentDTO> getStudentsByDepartment(@PathVariable("department") String department);

    @GetMapping("/api/students/course/{courseId}")
    List<StudentDTO> getStudentsByCourseId(@PathVariable("courseId") Long courseId);

    @GetMapping("/api/students/{id}/exists")
    Boolean existsById(@PathVariable("id") Long id);

    @GetMapping("/api/students/{id}/object")
    Map<String, Object> getStudentByIdObject(@PathVariable("id") Long id);
}