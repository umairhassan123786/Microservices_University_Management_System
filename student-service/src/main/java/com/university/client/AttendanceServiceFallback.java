package com.university.client;

import com.university.DTO.AttendanceDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class AttendanceServiceFallback implements AttendanceServiceClient {

    @Override
    public List<AttendanceDTO> getStudentAttendance(Long studentId) {
        AttendanceDTO fallbackAttendance = new AttendanceDTO();
        fallbackAttendance.setId(1L);
        fallbackAttendance.setStudentId(studentId);
        fallbackAttendance.setCourseId(1L);
        fallbackAttendance.setDate(LocalDate.now());
        fallbackAttendance.setStatus("DATA_UNAVAILABLE");
        fallbackAttendance.setCourseName("Attendance Service Unavailable");

        return Arrays.asList(fallbackAttendance);
    }

    @Override
    public List<AttendanceDTO> getCourseAttendance(Long studentId, Long courseId) {
        AttendanceDTO fallbackAttendance = new AttendanceDTO();
        fallbackAttendance.setId(1L);
        fallbackAttendance.setStudentId(studentId);
        fallbackAttendance.setCourseId(courseId);
        fallbackAttendance.setDate(LocalDate.now());
        fallbackAttendance.setStatus("SERVICE_UNAVAILABLE");
        fallbackAttendance.setCourseName("Course Attendance Data Unavailable");

        return Arrays.asList(fallbackAttendance);
    }

    @Override
    public List<AttendanceDTO> getAttendanceByCourseAndDate(Long courseId, LocalDate date) {
        return Arrays.asList();
    }
}
