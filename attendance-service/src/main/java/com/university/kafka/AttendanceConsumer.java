package com.university.kafka;
import com.university.Entities.Attendance;
import com.university.Enum.AttendanceStatus;
import com.university.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;

@Service
public class AttendanceConsumer {

    @Autowired
    private AttendanceService attendanceService;
    @KafkaListener(topics = "attendance-topic", groupId = "attendance-service-group")
    public void consumeAttendance(String attendanceJson) {
        try {
            System.out.println("Received raw JSON: " + attendanceJson);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> attendanceData = mapper.readValue(attendanceJson, Map.class);
            Long studentId = Long.valueOf(attendanceData.get("studentId").toString());
            Long courseId = Long.valueOf(attendanceData.get("courseId").toString());
            LocalDate date = LocalDate.parse(attendanceData.get("date").toString());
            AttendanceStatus status = AttendanceStatus.valueOf(attendanceData.get("status").toString().toUpperCase());
            String remarks = attendanceData.get("remarks") != null ? attendanceData.get("remarks").toString() : null;
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setCourseId(courseId);
            attendance.setDate(date);
            attendance.setStatus(status);
            attendance.setRemarks(remarks);
            attendanceService.markAttendance(attendance);
            System.out.println("Attendance saved successfully for studentId=" + studentId);
        } catch (Exception e) {
            System.err.println("Error processing attendance JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
