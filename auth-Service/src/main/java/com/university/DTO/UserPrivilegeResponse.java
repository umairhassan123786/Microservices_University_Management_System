package com.university.DTO;
import lombok.Data;
import java.util.List;

@Data
public class UserPrivilegeResponse {
    private Long userId;
    private String username;
    private String role;
    private List<String> privileges;
}