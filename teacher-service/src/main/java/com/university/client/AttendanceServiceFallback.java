package com.university.client;
import com.university.Entities.Attendance;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class AttendanceServiceFallback implements AttendanceServiceClient {

    @Override
    public Attendance markAttendance(Attendance attendance) {
        attendance.setRemarks("ERROR: Attendance service unavailable - Fallback activated");
        return attendance;
    }

    @Override
    public List<Attendance> bulkMarkAttendance(List<Attendance> attendanceList) {
        for (Attendance attendance : attendanceList) {
            attendance.setRemarks("ERROR: Bulk attendance service unavailable");
        }
        return attendanceList;
    }

    @Override
    public List<Attendance> getCourseAttendance(Long courseId) {
        System.out.println("Attendance service down - Returning empty course attendance list");
        return new ArrayList<>();
    }

    @Override
    public List<Attendance> getAttendanceByDateAndCourse(Long courseId, String date) {
        System.out.println("Attendance service down - Returning empty attendance by date list");
        return new ArrayList<>();
    }
}