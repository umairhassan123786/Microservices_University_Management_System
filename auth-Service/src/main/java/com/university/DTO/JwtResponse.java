package com.university.DTO;
import java.util.Date;
public class JwtResponse {
    private String token;
    private String type;
    private Date expiryDate;
    private String username;
    private String role;
    private Long userId;
    public JwtResponse(String token, String type, Date expiryDate, String username, String role, Long userId) {
        this.token = token;
        this.type = type;
        this.expiryDate = expiryDate;
        this.username = username;
        this.role = role;
        this.userId = userId;
        System.out.println(" JwtResponse Constructor:");
        System.out.println("   Token: " + token);
        System.out.println("   Username: " + username);
        System.out.println("   Role: " + role);
        System.out.println("   UserId: " + userId);
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public String getUsername() {
        return username;
    }
    public String getRole() {
        return role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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