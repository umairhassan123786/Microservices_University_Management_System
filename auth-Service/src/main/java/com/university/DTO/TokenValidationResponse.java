package com.university.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private String message;
    private String username;
    private String role;
    private Long userId;

    public boolean isValid() {
        return valid;
    }
}