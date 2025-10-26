package com.university.DTO;
import lombok.*;
import java.util.Date;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Date expiryDate;
    private String username;
    private String role;
}