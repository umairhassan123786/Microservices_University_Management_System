package com.university.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CourseDTO {
    private Long id;

    @JsonProperty("courseName")  // JSON mein "courseName" ko "name" map karega
    private String name;

    @JsonProperty("courseCode")  // JSON mein "courseCode" ko "code" map karega
    private String code;
    private Integer credits;
    private String department;
    private String semester;
    private Long teacherId;
    private String teacherName;
}