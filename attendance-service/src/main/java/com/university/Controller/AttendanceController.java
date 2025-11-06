package com.university.Controller;
import com.university.DTO.UpdateAttendanceRequest;
import com.university.Entities.Attendance;
import com.university.Enum.AttendanceStatus;
import com.university.Service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(@RequestBody Attendance attendance) {
        try {
            Attendance savedAttendance = attendanceService.markAttendance(attendance);
            return ResponseEntity.ok(savedAttendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public List<Attendance> getStudentAttendance(@PathVariable Long studentId) {
        return attendanceService.getStudentAttendance(studentId);
    }

    @GetMapping("/course/{courseId}")
    public List<Attendance> getCourseAttendance(@PathVariable Long courseId) {
        return attendanceService.getCourseAttendance(courseId);
    }

    @GetMapping("/course/{courseId}/date/{date}")
    public List<Attendance> getAttendanceByDateAndCourse(
            @PathVariable Long courseId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getAttendanceByDateAndCourse(courseId, date);
    }
    @GetMapping("/student/{studentId}/semester/{semester}")
    public List<Attendance> getStudentSemesterAttendance(
            @PathVariable Long studentId,
            @PathVariable String semester) {
        return attendanceService.getStudentSemesterAttendance(studentId, semester);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
        try {
            attendanceService.deleteAttendance(id);
            return ResponseEntity.ok("Attendance record deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/student/{studentId}/course/{courseId}")
    public List<Attendance> getStudentCourseAttendance(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        return attendanceService.getStudentCourseAttendance(studentId, courseId);
    }

    @GetMapping("/student/{studentId}/date-range")
    public List<Attendance> getStudentAttendanceInDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return attendanceService.getStudentAttendanceInDateRange(studentId, startDate, endDate);
    }

    @GetMapping("/course/{courseId}/date-range")
    public List<Attendance> getCourseAttendanceInDateRange(
            @PathVariable Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return attendanceService.getCourseAttendanceInDateRange(courseId, startDate, endDate);
    }

    @GetMapping("/statistics/student/{studentId}/course/{courseId}")
    public Map<AttendanceStatus, Long> getAttendanceStatistics(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        return attendanceService.getAttendanceStatistics(studentId, courseId);
    }

    @GetMapping("/percentage/student/{studentId}/course/{courseId}")
    public Map<String, Double> getAttendancePercentage(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        double percentage = attendanceService.getAttendancePercentage(studentId, courseId);
        return Map.of("percentage", percentage);
    }
    @PutMapping("/{attendanceId}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long attendanceId,
            @RequestBody UpdateAttendanceRequest request) {
        try {
            Attendance updatedAttendance = attendanceService.updateAttendance(attendanceId, request);
            return ResponseEntity.ok(updatedAttendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/teacher/{teacherId}/courses")
    public ResponseEntity<List<Attendance>> getTeacherCourseAttendance(
            @PathVariable Long teacherId,
            @RequestParam(required = false) Long courseId) {
        try {
            List<Attendance> attendance = attendanceService.getTeacherCourseAttendance(teacherId, courseId);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PostMapping("/bulk-mark")
    public ResponseEntity<?> bulkMarkAttendance(@RequestBody List<Attendance> attendanceList) {
        try {
            List<Attendance> savedAttendances = attendanceService.bulkMarkAttendance(attendanceList);
            return ResponseEntity.ok(savedAttendances);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}