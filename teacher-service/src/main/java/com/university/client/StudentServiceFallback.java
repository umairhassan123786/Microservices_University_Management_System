package com.university.client;

import com.university.DTO.StudentDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class StudentServiceFallback implements StudentServiceClient {

    @Override
    public StudentDTO getStudentById(Long studentId) {
        System.out.println("⚠️ Student service down - Returning fallback student for ID: " + studentId);

        StudentDTO fallbackStudent = new StudentDTO();
        fallbackStudent.setId(studentId);
        fallbackStudent.setName("⚠️ Student Information Unavailable");
        fallbackStudent.setEmail("service@unavailable.com");
        fallbackStudent.setRollNumber("SERVICE-DOWN");
        fallbackStudent.setDepartment("Service Unavailable");
        fallbackStudent.setSemester("N/A");
        fallbackStudent.setActive(false);

        return fallbackStudent;
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        System.out.println("⚠️ Student service down - Returning empty students list");
        return Collections.emptyList();
    }

    @Override
    public List<StudentDTO> getStudentsByDepartment(String department) {
        System.out.println("⚠️ Student service down - Returning empty department students list");
        return Collections.emptyList();
    }

    @Override
    public List<StudentDTO> getStudentsByCourseId(Long courseId) {
        System.out.println("⚠️ Student service down - Returning empty course students list");
        return Collections.emptyList();
    }

    @Override
    public Boolean existsById(Long studentId) {
        System.out.println("⚠️ Student service down - Cannot verify student existence for ID: " + studentId);
        return null; // Return null to indicate service unavailable
    }

    @Override
    public Map<String, Object> getStudentByIdObject(Long studentId) {
        System.out.println("Student service down - Returning fallback student object for ID: " + studentId);

        return Map.of(
                "id", studentId,
                "name", "Student Service Unavailable",
                "email", "service@unavailable.com",
                "rollNumber", "SERVICE-DOWN",
                "department", "Service Unavailable",
                "error", true,
                "message", "Student service is currently down"
        );
    }
}