package com.university.DTO;
import com.university.Entities.Student;
import lombok.Data;
import java.util.List;

@Data
public class StudentProfileDTO {
    private Student student;
    private List<CourseDTO> courses;
    private List<AttendanceDTO> attendance;
    private double attendancePercentage;
    private String overallStatus;

    public StudentProfileDTO(Student student, List<CourseDTO> courses,
                             List<AttendanceDTO> attendance, double attendancePercentage) {
        this.student = student;
        this.courses = courses;
        this.attendance = attendance;
        this.attendancePercentage = attendancePercentage;
        this.overallStatus = calculateOverallStatus(attendancePercentage);
    }

    private String calculateOverallStatus(double percentage) {
        if (percentage >= 75) return "GOOD";
        else if (percentage >= 50) return "AVERAGE";
        else return "NEEDS_IMPROVEMENT";
    }
}