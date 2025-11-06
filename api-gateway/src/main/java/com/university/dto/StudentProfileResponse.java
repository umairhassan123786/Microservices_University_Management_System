package com.university.dto;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentProfileResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String rollNumber;
    private String department;
    private Integer semester;

}