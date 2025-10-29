package com.university.client;
import com.university.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "teacher-service",
        path = "/api/teachers",
        configuration = FeignConfig.class
)
public interface TeacherServiceClient {

    @GetMapping("/check-teacherid/{teacherId}")
    Boolean existsByTeacherId(@PathVariable("teacherId") String teacherId);

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> createTeacher(@RequestBody Map<String, Object> teacherData);

    @GetMapping("/user/{userId}")
    Map<String, Object> getTeacherByUserId(@PathVariable("userId") Long userId);
}