package com.university.Service;

import com.university.DTO.JwtResponse;
import com.university.DTO.LoginRequest;
import com.university.DTO.RegisterRequest;
import com.university.DTO.TokenValidationResponse;
import com.university.Entities.User;
import com.university.Repository.UserRepository;
import com.university.Util.JwtUtil;
import com.university.Util.PasswordUtil;
import com.university.client.StudentServiceClient;
import com.university.client.TeacherServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private TeacherServiceClient teacherServiceClient;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private StudentServiceClient studentServiceClient;

    public JwtResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOptional.get();
        boolean passwordMatch = passwordUtil.matches(request.getPassword(), user.getPassword());
        if (!passwordMatch) {
            throw new RuntimeException("Invalid password");
        }
        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        Date expiryDate = jwtUtil.getExpirationDateFromToken(token);
        return new JwtResponse(token, "Bearer", expiryDate, user.getUsername(), user.getRole());
    }

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            // Pehle check karein token blacklisted toh nahi hai
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return new TokenValidationResponse(false, "Token has been invalidated", null, null);
            }

            // Phir JWT validation
            boolean isValid = jwtUtil.validateToken(token);
            if (isValid) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                return new TokenValidationResponse(true, "Valid token", username, role);
            }
            return new TokenValidationResponse(false, "Invalid token", null, null);
        } catch (Exception e) {
            return new TokenValidationResponse(false, "Token validation failed", null, null);
        }
    }

    public boolean logout(String token) {
        try {
            tokenBlacklistService.invalidateToken(token);
            return true;
        } catch (Exception e) {
            System.out.println("Logout error: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> register(RegisterRequest registerRequest) {
        // ✅ Check if username already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + registerRequest.getUsername() + "' already exists");
        }

        // ✅ Check if email already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email '" + registerRequest.getEmail() + "' already exists");
        }

        // ✅ For STUDENT - check roll number
        if ("STUDENT".equals(registerRequest.getRole()) && registerRequest.getRollNumber() != null) {
            // Call student service to check roll number
            Boolean rollNumberExists = studentServiceClient.existsByRollNumber(registerRequest.getRollNumber());
            if (rollNumberExists != null && rollNumberExists) {
                throw new RuntimeException("Roll number '" + registerRequest.getRollNumber() + "' already exists");
            }
        }

        // ✅ For TEACHER - check teacher ID
        if ("TEACHER".equals(registerRequest.getRole()) && registerRequest.getTeacherId() != null) {
            // Call teacher service to check teacher ID
            Boolean teacherIdExists = teacherServiceClient.existsByTeacherId(registerRequest.getTeacherId());
            if (teacherIdExists != null && teacherIdExists) {
                throw new RuntimeException("Teacher ID '" + registerRequest.getTeacherId() + "' already exists");
            }
        }
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordUtil.encodePassword(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());
        user.setActive(true);

        User savedUser = userRepository.save(user);
        System.out.println("User created in auth system: " + savedUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("role", savedUser.getRole());
        response.put("message", "User registered successfully");

        try {
            if ("TEACHER".equals(registerRequest.getRole())) {
                Object teacherResponse = createTeacherProfile(savedUser.getId(), registerRequest);
                response.put("teacherProfile", teacherResponse);
                response.put("message", "Teacher registered successfully with profile");
            }
            else if ("STUDENT".equals(registerRequest.getRole())) {
                Object studentResponse = createStudentProfile(savedUser.getId(), registerRequest);
                response.put("studentProfile", studentResponse);
                response.put("message", "Student registered successfully with profile");
            }
        } catch (Exception e) {
            System.out.println("Profile creation failed but user created: " + e.getMessage());
            response.put("warning", "User created but profile setup incomplete");
        }

        return response;
    }

    private Map<String, Object> createTeacherProfile(Long userId, RegisterRequest request) {
        try {
            Map<String, Object> teacherData = new HashMap<>();
            teacherData.put("userId", userId);
            teacherData.put("name", request.getFullName() != null ? request.getFullName() : request.getUsername());
            teacherData.put("email", request.getEmail());
            teacherData.put("department", request.getDepartment());
            teacherData.put("qualification", request.getQualification());
            teacherData.put("teacherId", generateTeacherId());

            System.out.println("📤 Sending teacher data: " + teacherData);

            // ✅ Try with explicit error handling
            try {
                Map<String, Object> response = teacherServiceClient.createTeacher(teacherData);
                System.out.println("✅ Teacher service response: " + response);

                if (response != null && response.containsKey("error")) {
                    throw new RuntimeException("Teacher service error: " + response.get("error"));
                }

                return response != null ? response : Map.of("status", "SUCCESS");

            } catch (feign.FeignException e) {
                System.out.println(" Feign Exception: " + e.status() + " - " + e.getMessage());
                if (e.status() == 404) {
                    throw new RuntimeException("Teacher service not found. Please check if teacher service is running.");
                } else if (e.status() == 500) {
                    throw new RuntimeException("Teacher service internal error.");
                } else {
                    throw new RuntimeException("Teacher service communication failed: " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("❌ General Exception: " + e.getMessage());
                throw new RuntimeException("Failed to communicate with teacher service: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Error in createTeacherProfile: " + e.getMessage());
            throw new RuntimeException("Failed to create teacher profile: " + e.getMessage());
        }
    }    public Object getTeacherByUserId(Long userId) {
        try {
            // Pehle check karein user exists aur teacher hai ya nahi
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found with ID: " + userId);
            }

            User user = userOptional.get();
            if (!"TEACHER".equals(user.getRole())) {
                throw new RuntimeException("User is not a teacher");
            }

            // Teacher service se teacher data fetch karein
            Object teacher = teacherServiceClient.getTeacherByUserId(userId);
            return teacher;
        } catch (Exception e) {
            System.out.println("Error in getTeacherByUserId: " + e.getMessage());
            throw new RuntimeException("Failed to fetch teacher: " + e.getMessage());
        }
    }
    private String generateTeacherId() {
        // Teacher ID generate karne ka logic - aap apne requirement ke hisaab modify kar sakte hain
        String prefix = "TCH";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomNum = String.valueOf((int)(Math.random() * 1000));
        return prefix + timestamp.substring(7) + randomNum;
    }
    private Map<String, Object> createStudentProfile(Long userId, RegisterRequest request) {
        try {
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("userId", userId);
            studentData.put("name", request.getFullName() != null ? request.getFullName() : request.getUsername());
            studentData.put("email", request.getEmail());
            studentData.put("rollNumber", request.getRollNumber());
            studentData.put("semester", request.getSemester());
            studentData.put("department", request.getDepartment());
            studentData.put("active", true);

            System.out.println("📤 Sending student data: " + studentData);

            // ✅ Better error handling for student service
            try {
                Map<String, Object> response = studentServiceClient.createStudent(studentData);
                System.out.println("✅ Student service response: " + response);

                if (response != null && response.containsKey("error")) {
                    throw new RuntimeException("Student service error: " + response.get("error"));
                }

                return response != null ? response : Map.of("status", "SUCCESS");

            } catch (feign.FeignException e) {
                System.out.println("❌ Student Feign Exception: " + e.status() + " - " + e.getMessage());
                if (e.status() == 404) {
                    throw new RuntimeException("Student service not found. Please check if student service is running.");
                } else if (e.status() == 400) {
                    throw new RuntimeException("Bad request to student service. Check student data.");
                } else if (e.status() == 500) {
                    throw new RuntimeException("Student service internal error.");
                } else {
                    throw new RuntimeException("Student service communication failed: " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("❌ General Exception in student creation: " + e.getMessage());
                throw new RuntimeException("Failed to communicate with student service: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Error in createStudentProfile: " + e.getMessage());
            throw new RuntimeException("Failed to create student profile: " + e.getMessage());
        }
    }
}