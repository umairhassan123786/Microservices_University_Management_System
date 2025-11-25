package com.university.DTO;
import lombok.Data;
import java.util.List;

@Data
public class PrivilegeRequest {
    private Long userId;
    private List<String> privileges;
}