package com.university.client;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class StudentServiceFallback implements StudentServiceClient {

    @Override
    public Boolean existsByRollNumber(String rollNumber) {
        System.out.println("Student Service Circuit Breaker OPEN - Fallback for rollNumber: " + rollNumber);
        return false;
    }

    @Override
    public Boolean existsById(Long studentId) {
        System.out.println("Student Service Circuit Breaker OPEN - Fallback for studentId: " + studentId);
        return false;
    }

    @Override
    public Map<String, Object> getStudentById(Long studentId) {
         Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("Unable to get Data of", studentId);
        fallbackResponse.put("name", "Service Unavailable");
        fallbackResponse.put("rollNumber", "N/A");
        fallbackResponse.put("status", "fallback");
        fallbackResponse.put("message", "Student service temporarily unavailable");
        return fallbackResponse;
    }

    @Override
    public Map<String, Object> createStudent(Map<String, Object> studentData) {
         Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("status", "error");
        fallbackResponse.put("message", "Student service unavailable - data queued for processing");
        fallbackResponse.put("fallback", true);
        fallbackResponse.put("studentData", studentData);
        return fallbackResponse;
    }
}