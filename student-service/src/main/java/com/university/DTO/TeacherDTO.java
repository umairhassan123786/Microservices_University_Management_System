package com.university.DTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TeacherDTO {
    private Long id;
    private String name;
    private String email;
    private String department;
    private String qualification;
}