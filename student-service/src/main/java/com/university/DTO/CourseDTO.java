package com.university.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CourseDTO {
    private Long id;

    @JsonProperty("courseName")
    private String name;

    @JsonProperty("courseCode")
    private String code;
    private Integer credits;
    private String department;
    private String semester;
    private Long teacherId;
    private String teacherName;
}