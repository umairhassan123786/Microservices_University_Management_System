package com.university.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AttendanceDTO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private LocalDate date;
    private String status;
    private String studentName;
    private String courseName;
}