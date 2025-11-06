package com.university.client;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Map;

@Component
public class StudentServiceClientFallback implements StudentServiceClient {
    @Override
    public Boolean existsByRollNumber(String rollNumber) {
        return false;
    }

    @Override
    public Map<String, Object> createStudent(Map<String, Object> studentData) {
        return Collections.singletonMap("Error", "Student service unavailable");
    }

    @Override
    public Map<String, Object> getStudentByUserId(Long userId) {
        return Collections.singletonMap("Error", "Student service unavailable");
    }
}
