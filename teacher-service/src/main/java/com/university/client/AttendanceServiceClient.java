package com.university.client;
import com.university.Entities.Attendance;
import com.university.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(
        name = "attendance-service",
        url = "http://localhost:8085",
        fallback = AttendanceServiceFallback.class,
        configuration = FeignConfig.class
)
public interface AttendanceServiceClient {

    @PostMapping("/api/attendance/mark")
    Attendance markAttendance(@RequestBody Attendance attendance);

    @PostMapping("/api/attendance/bulk-mark")
    List<Attendance> bulkMarkAttendance(@RequestBody List<Attendance> attendanceList);

    @GetMapping("/api/attendance/course/{courseId}")
    List<Attendance> getCourseAttendance(@PathVariable("courseId") Long courseId);

    @GetMapping("/api/attendance/course/{courseId}/date/{date}")
    List<Attendance> getAttendanceByDateAndCourse(
            @PathVariable("courseId") Long courseId,
            @PathVariable("date") String date);
}