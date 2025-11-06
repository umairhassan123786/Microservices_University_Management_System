package com.university.client;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Map;

@Component
public class TeacherServiceClientFallback implements TeacherServiceClient {
    @Override
    public Boolean existsByTeacherId(String teacherId) {
        return false;
    }

    @Override
    public Map<String, Object> createTeacher(Map<String, Object> teacherData) {
        return Collections.singletonMap("error", "Teacher service unavailable");
    }

    @Override
    public Map<String, Object> getTeacherByUserId(Long userId) {
        return Collections.singletonMap("error", "Teacher service unavailable");
    }
}
