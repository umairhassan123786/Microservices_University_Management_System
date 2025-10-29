package com.university.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(
        name = "student-service",
        url = "http://localhost:8083",
        path = "/api/students"
)
public interface StudentServiceClient {

    @GetMapping("/check-rollnumber/{rollNumber}")
    Boolean existsByRollNumber(@PathVariable("rollNumber") String rollNumber);

    @GetMapping("/exists/{studentId}")
    Boolean existsById(@PathVariable("studentId") Long studentId);

    @GetMapping("/{studentId}")
    Map<String, Object> getStudentById(@PathVariable("studentId") Long studentId);

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> createStudent(@RequestBody Map<String, Object> studentData);
}