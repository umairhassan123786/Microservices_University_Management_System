package com.university.dto;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class TokenValidationResponse {
    private boolean valid;
    private String message;
    private String username;
}