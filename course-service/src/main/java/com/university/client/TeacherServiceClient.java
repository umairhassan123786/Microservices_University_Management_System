package com.university.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(
        name = "teacher-service",
        url = "http://localhost:8090",
        path = "/api/teachers",
        fallback = TeacherServiceFallback.class
)
public interface TeacherServiceClient {

    @GetMapping("/check-teacherid/{id}")
    Boolean existsByTeacherId(@PathVariable("id") String teacherId);

    @GetMapping("/exists/{id}")
    Boolean existsById(@PathVariable("id") Long teacherId);


    @GetMapping("/{id}")
    Map<String, Object> getTeacherById(@PathVariable("id") Long id);

    @PostMapping
    Object createTeacher(@RequestBody Map<String, Object> teacherData);

    @GetMapping("/user/{userId}")
    Object getTeacherByUserId(@PathVariable("userId") Long userId);

}