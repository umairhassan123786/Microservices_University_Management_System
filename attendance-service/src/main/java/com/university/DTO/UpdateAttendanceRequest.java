package com.university.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateAttendanceRequest {
    private String status; // PRESENT, ABSENT, LATE
    private String remarks;
    private LocalDate date;
}