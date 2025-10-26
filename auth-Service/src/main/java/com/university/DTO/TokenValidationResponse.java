package com.university.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private String message;
    private String username;
    private String role;

    public boolean isValid() {
        return valid;
    }
}