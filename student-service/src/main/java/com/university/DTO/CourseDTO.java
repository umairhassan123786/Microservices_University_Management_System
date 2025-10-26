// Create this in student-service/src/main/java/com/university/dto/CourseDTO.java
package com.university.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer credits;
    private Long teacherId;
    private String teacherName;
}