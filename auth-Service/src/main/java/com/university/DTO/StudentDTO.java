package com.university.DTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class StudentDTO {

    private Long id;
    private Long userId;
    private String email;
    private String rollNo;
    private String semester;
    private String department;
    private Boolean active;
}
