package com.university.DTO;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AttendanceRequest {
    private Long studentId;
    private Long courseId;
    private LocalDate date;
    private String status;
    private String semester;
    private String remarks;
}