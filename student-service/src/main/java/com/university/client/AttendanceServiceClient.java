package com.university.client;
import com.university.DTO.AttendanceDTO;
import com.university.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDate;
import java.util.List;

@FeignClient(
        name = "attendance-service",
        path = "/api/attendance",
        configuration = FeignConfig.class,
        fallback = AttendanceServiceFallback.class
)
public interface AttendanceServiceClient {

    @GetMapping("/student/{studentId}")
    List<AttendanceDTO> getStudentAttendance(@PathVariable("studentId") Long studentId);

    @GetMapping("/student/{studentId}/course/{courseId}")
    List<AttendanceDTO> getCourseAttendance(
            @PathVariable("studentId") Long studentId,
            @PathVariable("courseId") Long courseId);

    @GetMapping("/course/{courseId}/date/{date}")
    List<AttendanceDTO> getAttendanceByCourseAndDate(
            @PathVariable("courseId") Long courseId,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
