package com.university.client;

import com.university.DTO.TeacherDTO;
import org.springframework.stereotype.Component;

@Component
public class TeacherServiceFallback implements TeacherServiceClient {

    @Override
    public TeacherDTO getTeacherById(Long teacherId) {
        TeacherDTO fallbackTeacher = new TeacherDTO();
        fallbackTeacher.setId(teacherId);
        fallbackTeacher.setName("Teacher Information Unavailable");
        fallbackTeacher.setEmail("service@unavailable.com");
        fallbackTeacher.setDepartment("N/A");
        fallbackTeacher.setQualification("Information temporarily unavailable");
        return fallbackTeacher;
    }

    @Override
    public TeacherDTO getTeacherByCourseId(Long courseId) {
        TeacherDTO fallbackTeacher = new TeacherDTO();
        fallbackTeacher.setId(1L);
        fallbackTeacher.setName("Course Teacher Information Unavailable");
        fallbackTeacher.setEmail("teacher@unavailable.com");
        fallbackTeacher.setDepartment("N/A");
        return fallbackTeacher;
    }
}
