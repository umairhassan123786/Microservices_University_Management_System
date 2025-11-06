package com.university.client;
import com.university.dto.StudentProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "student-service", url = "http://localhost:8083")
public interface StudentServiceClient {

    @GetMapping("/api/students/{studentId}/profile")
    StudentProfileResponse getStudentProfile(
            @PathVariable Long studentId
    );
}