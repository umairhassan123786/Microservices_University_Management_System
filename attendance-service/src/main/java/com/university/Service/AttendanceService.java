package com.university.Service;
import com.university.DTO.UpdateAttendanceRequest;
import com.university.Entities.Attendance;
import com.university.Enum.AttendanceStatus;
import com.university.Repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    public Attendance markAttendance(Attendance attendance) {
        if (attendanceRepository.existsByStudentIdAndCourseIdAndDate(
                attendance.getStudentId(), attendance.getCourseId(), attendance.getDate())) {
            throw new RuntimeException("Attendance already marked for this student, course and date");
        }
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> bulkMarkAttendance(List<Attendance> attendanceList) {
        // Validate each record before saving
        for (Attendance attendance : attendanceList) {
            if (attendanceRepository.existsByStudentIdAndCourseIdAndDate(
                    attendance.getStudentId(), attendance.getCourseId(), attendance.getDate())) {
                throw new RuntimeException("Duplicate attendance found for student: " +
                        attendance.getStudentId() + " on date: " + attendance.getDate());
            }
        }
        return attendanceRepository.saveAll(attendanceList);
    }
    public List<Attendance> getCourseAttendance(Long courseId) {
        return attendanceRepository.findByCourseId(courseId);
    }

    public List<Attendance> getAttendanceByDateAndCourse(Long courseId, LocalDate date) {
        return attendanceRepository.findByCourseIdAndDate(courseId, date);
    }

    public List<Attendance> getStudentAttendance(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    public Attendance updateAttendance(Long id, UpdateAttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        try {
            AttendanceStatus status = AttendanceStatus.valueOf(request.getStatus().toUpperCase());
            attendance.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid attendance status: " + request.getStatus());
        }

        attendance.setRemarks(request.getRemarks());
        if (request.getDate() != null) {
            attendance.setDate(request.getDate());
        }

        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getStudentSemesterAttendance(Long studentId, String semester) {
        return attendanceRepository.findByStudentIdAndSemester(studentId, semester);
    }

    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }

    public List<Attendance> getStudentCourseAttendance(Long studentId, Long courseId) {
        return attendanceRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    public List<Attendance> getStudentAttendanceInDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByStudentIdAndDateBetween(studentId, startDate, endDate);
    }

    public List<Attendance> getCourseAttendanceInDateRange(Long courseId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByCourseIdAndDateBetween(courseId, startDate, endDate);
    }

    public Map<AttendanceStatus, Long> getAttendanceStatistics(Long studentId, Long courseId) {
        return Map.of(
                AttendanceStatus.PRESENT,
                attendanceRepository.countByStudentIdAndCourseIdAndStatus(studentId, courseId, AttendanceStatus.PRESENT),
                AttendanceStatus.ABSENT,
                attendanceRepository.countByStudentIdAndCourseIdAndStatus(studentId, courseId, AttendanceStatus.ABSENT),
                AttendanceStatus.LATE,
                attendanceRepository.countByStudentIdAndCourseIdAndStatus(studentId, courseId, AttendanceStatus.LATE),
                AttendanceStatus.HALF_DAY,
                attendanceRepository.countByStudentIdAndCourseIdAndStatus(studentId, courseId, AttendanceStatus.HALF_DAY)
        );
    }

    public double getAttendancePercentage(Long studentId, Long courseId) {
        List<Attendance> allRecords = attendanceRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (allRecords.isEmpty()) return 0.0;

        long presentCount = allRecords.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.PRESENT)
                .count();

        return (presentCount * 100.0) / allRecords.size();
    }
    public List<Attendance> getTeacherCourseAttendance(Long teacherId, Long courseId) {
        return attendanceRepository.findByCourseId(courseId);
    }
}