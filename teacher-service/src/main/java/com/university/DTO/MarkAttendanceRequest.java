package com.university.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class MarkAttendanceRequest {
    private Long studentId;
    private String status;
    private LocalDate date;
    private Long courseId;
    private String remarks;
    public MarkAttendanceRequest() {}

    public MarkAttendanceRequest(Long studentId, String status, LocalDate date, Long courseId, String remarks) {
        this.studentId = studentId;
        this.status = status;
        this.date = date;
        this.courseId = courseId;
        this.remarks = remarks;
    }
}