package com.university.DTO;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class RegisterRequest {

    // ✅ Common fields for all roles
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|TEACHER|STUDENT", message = "Role must be ADMIN, TEACHER or STUDENT")
    private String role;

    // ✅ Common profile field
    private String fullName;

    // ✅ TEACHER specific fields
    private String teacherId;
    private String department;
    private String qualification;

    // ✅ STUDENT specific fields
    private String rollNumber;
    private String semester;
}