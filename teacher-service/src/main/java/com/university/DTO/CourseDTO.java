package com.university.DTO;

import lombok.Data;

@Data
public class CourseDTO {
    private Long id;
    private String name;
    private String code;
    private Integer credits;
    private Long teacherId;
    private String teacherName;
}