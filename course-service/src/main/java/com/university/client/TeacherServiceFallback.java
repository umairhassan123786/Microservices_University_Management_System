package com.university.client;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class TeacherServiceFallback implements TeacherServiceClient {

    @Override
    public Boolean existsByTeacherId(String teacherId) {
            return false;
    }

    @Override
    public Boolean existsById(Long teacherId) {
        return false;
    }

    @Override
    public Map<String, Object> getTeacherById(Long id) {
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("id", id);
        fallbackResponse.put("name", "Service Unavailable");
        fallbackResponse.put("teacherId", "N/A");
        fallbackResponse.put("status", "fallback");
        fallbackResponse.put("message", "Teacher service temporarily unavailable");
        return fallbackResponse;
    }

    @Override
    public Object createTeacher(Map<String, Object> teacherData) {
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("status", "error");
        fallbackResponse.put("message", "Teacher service unavailable - data queued for processing");
        fallbackResponse.put("fallback", true);
        fallbackResponse.put("teacherData", teacherData);
        return fallbackResponse;
    }

    @Override
    public Object getTeacherByUserId(Long userId) {
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("userId", userId);
        fallbackResponse.put("error", "Teacher service unavailable");
        fallbackResponse.put("status", "fallback");
        return fallbackResponse;
    }
}