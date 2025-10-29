package com.university.DTO;
import lombok.Data;

@Data
public class CourseServiceResponseDTO {
    private Long id;
    private String courseName;
    private String courseCode;
    private String description;
    private String department;
    private String semester;
    private Integer credits;
    private Long teacherId;
}