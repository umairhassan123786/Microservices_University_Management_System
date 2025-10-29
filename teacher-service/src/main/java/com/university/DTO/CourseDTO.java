package com.university.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CourseDTO {
    private Long id;

    @JsonProperty("courseName")  // JSON field "courseName" ko "name" map karega
    private String name;

    @JsonProperty("courseCode")  // JSON field "courseCode" ko "code" map karega
    private String code;

    private String description;
    private Integer credits;
    private String department;
    private String semester;

    @JsonProperty("teacherId")
    private Long teacherId;

    private String teacherName;
}