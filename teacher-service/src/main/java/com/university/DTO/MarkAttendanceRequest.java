package com.university.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MarkAttendanceRequest {
    private Long studentId;
    private String status;
    private LocalDate date;
    private Long courseId;
    private String remarks;
}