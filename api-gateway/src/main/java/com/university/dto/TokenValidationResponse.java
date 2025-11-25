package com.university.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private String message;
    private String username;
    private String role;
    private Long userId;
    private List<String> privileges;
    public boolean isValid() {
        return valid;
    }
}