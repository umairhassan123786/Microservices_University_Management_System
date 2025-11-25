package com.university.DTO;
import lombok.*;
import java.util.Date;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type;
    private Date expiryDate;
    private String username;
    private String role;
    private Long userId;

    @Override
    public String toString() {
        return "JwtResponse{" +
                "token='" + (token != null ? "PRESENT" : "NULL") + '\'' +
                ", type='" + type + '\'' +
                ", expiryDate=" + expiryDate +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", userId=" + userId +
                '}';
    }
}