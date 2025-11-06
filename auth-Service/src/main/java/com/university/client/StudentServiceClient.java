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
        name = "student-service",
        path = "/api/students",
        configuration = FeignConfig.class,
        fallback = StudentServiceClientFallback.class
)
public interface StudentServiceClient {

    @GetMapping("/check-rollnumber/{rollNumber}")
    Boolean existsByRollNumber(@PathVariable("rollNumber") String rollNumber);

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> createStudent(@RequestBody Map<String, Object> studentData);

    @GetMapping("/user/{userId}")
    Map<String, Object> getStudentByUserId(@PathVariable("userId") Long userId);
}