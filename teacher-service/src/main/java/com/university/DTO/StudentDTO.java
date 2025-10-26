package com.university.DTO;

import lombok.Data;

@Data
public class StudentDTO {
    private Long id;
    private String name;
    private String email;
    private String rollNumber;
    private String department;
    private String semester;
    private Boolean active;
}