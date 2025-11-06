package com.university.Service;
import com.university.DTO.*;
import com.university.Entities.User;
import com.university.Repository.UserRepository;
import com.university.Util.JwtUtil;
import com.university.Util.PasswordUtil;
import com.university.client.StudentServiceClient;
import com.university.client.TeacherServiceClient;
import com.university.kafka.UserRegistrationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRegistrationProducer registrationProducer;
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
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());
                Date expiryDate = jwtUtil.getExpirationDateFromToken(token);
        JwtResponse response = new JwtResponse(token, "Bearer", expiryDate, user.getUsername(), user.getRole(), user.getId());

        return response;
    }
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return new TokenValidationResponse(false, "Token has been invalidated", null, null, null);
            }

            boolean isValid = jwtUtil.validateToken(token);
            if (isValid) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);


                Optional<User> userOptional = userRepository.findByUsername(username);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    return new TokenValidationResponse(true, "Valid token", username, role, user.getId());
                } else {
                    return new TokenValidationResponse(false, "User not found", null, null, null);
                }
            }
            return new TokenValidationResponse(false, "Invalid token", null, null, null);
        } catch (Exception e) {
            return new TokenValidationResponse(false, "Token validation failed", null, null, null);
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

//    @Transactional
//    public Map<String, Object> register(RegisterRequest registerRequest) {
//        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
//            throw new RuntimeException("Username '" + registerRequest.getUsername() + "' already exists");
//        }
//        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
//            throw new RuntimeException("Email '" + registerRequest.getEmail() + "' already exists");
//        }
//        if ("STUDENT".equals(registerRequest.getRole()) && registerRequest.getRollNumber() != null) {
//            Boolean rollNumberExists = studentServiceClient.existsByRollNumber(registerRequest.getRollNumber());
//            if (Boolean.TRUE.equals(rollNumberExists)) {
//                throw new RuntimeException("Roll number '" + registerRequest.getRollNumber() + "' already exists");
//            }
//        }
//        if ("TEACHER".equals(registerRequest.getRole()) && registerRequest.getTeacherId() != null) {
//            Boolean teacherIdExists = teacherServiceClient.existsByTeacherId(registerRequest.getTeacherId());
//            if (Boolean.TRUE.equals(teacherIdExists)) {
//                throw new RuntimeException("Teacher ID '" + registerRequest.getTeacherId() + "' already exists");
//            }
//        }
//        User user = new User();
//        user.setUsername(registerRequest.getUsername());
//        user.setEmail(registerRequest.getEmail());
//        user.setPassword(passwordUtil.encodePassword(registerRequest.getPassword()));
//        user.setRole(registerRequest.getRole());
//        user.setActive(true);
//        User savedUser = userRepository.save(user);
//        Map<String, Object> response = new HashMap<>();
//        response.put("userId", savedUser.getId());
//        response.put("username", savedUser.getUsername());
//        response.put("role", savedUser.getRole());
//
//        try {
//            if ("TEACHER".equals(registerRequest.getRole())) {
//                Map<String, Object> teacherResponse = createTeacherProfile(savedUser.getId(), registerRequest);
//                if (teacherResponse.containsKey("error")) {
//                    throw new RuntimeException("Teacher service unavailable — rolling back user creation");
//                }
//
//                response.put("teacherProfile", teacherResponse);
//                response.put("message", "Teacher registered successfully with profile");
//            } else if ("STUDENT".equals(registerRequest.getRole())) {
//                Map<String, Object> studentResponse = createStudentProfile(savedUser.getId(), registerRequest);
//                if (studentResponse.containsKey("error")) {
//                    throw new RuntimeException("Student service unavailable — rolling back user creation");
//                }
//                response.put("studentProfile", studentResponse);
//                response.put("message", "Student registered successfully with profile");
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage());
//        }
//
//        return response;
//    }
    private Map<String, Object> createTeacherProfile(Long userId, RegisterRequest request) {
        try {
            Map<String, Object> teacherData = new HashMap<>();
            teacherData.put("userId", userId);
            teacherData.put("name", request.getFullName() != null ? request.getFullName() : request.getUsername());
            teacherData.put("email", request.getEmail());
            teacherData.put("department", request.getDepartment());
            teacherData.put("qualification", request.getQualification());
            teacherData.put("teacherId", generateTeacherId());

            System.out.println("Sending teacher data: " + teacherData);

            try {
                Map<String, Object> response = teacherServiceClient.createTeacher(teacherData);
                System.out.println("Teacher service response: " + response);

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
                System.out.println("General Exception: " + e.getMessage());
                throw new RuntimeException("Failed to communicate with teacher service: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Error in createTeacherProfile: " + e.getMessage());
            throw new RuntimeException("Failed to create teacher profile: " + e.getMessage());
        }
    }    public Object getTeacherByUserId(Long userId) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found with ID: " + userId);
            }
            User user = userOptional.get();
            if (!"TEACHER".equals(user.getRole())) {
                throw new RuntimeException("User is not a teacher");
            }
            Object teacher = teacherServiceClient.getTeacherByUserId(userId);
            return teacher;
        } catch (Exception e) {
            System.out.println("Error in getTeacherByUserId: " + e.getMessage());
            throw new RuntimeException("Failed to fetch teacher: " + e.getMessage());
        }
    }
    private String generateTeacherId() {
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

            System.out.println("Sending student data: " + studentData);
            try {
                Map<String, Object> response = studentServiceClient.createStudent(studentData);
                System.out.println(" Student service response: " + response);

                if (response != null && response.containsKey("error")) {
                    throw new RuntimeException("Student service error: " + response.get("error"));
                }

                return response != null ? response : Map.of("status", "SUCCESS");

            } catch (feign.FeignException e) {
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
                throw new RuntimeException("Failed to communicate with student service: " + e.getMessage());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to create student profile: " + e.getMessage());
        }
    }
    @Transactional
    public Map<String, Object> register(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordUtil.encodePassword(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());
        user.setActive(true);
        User savedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("role", savedUser.getRole());


        if ("STUDENT".equalsIgnoreCase(savedUser.getRole())) {
            Map<String, Object> studentEvent = new HashMap<>();
            studentEvent.put("userId", savedUser.getId());
            studentEvent.put("name", registerRequest.getFullName() != null ? registerRequest.getFullName() : registerRequest.getUsername());
            studentEvent.put("email", savedUser.getEmail());
            studentEvent.put("rollNumber", registerRequest.getRollNumber());
            studentEvent.put("semester", registerRequest.getSemester());
            studentEvent.put("department", registerRequest.getDepartment());


            registrationProducer.sendStudentRegistrationEvent(studentEvent);
            response.put("message", "Student registration event published");
        }
        else if ("TEACHER".equalsIgnoreCase(savedUser.getRole())) {
            Map<String, Object> teacherEvent = new HashMap<>();
            teacherEvent.put("userId", savedUser.getId());
            teacherEvent.put("name", registerRequest.getFullName() != null ? registerRequest.getFullName() : registerRequest.getUsername());
            teacherEvent.put("email", savedUser.getEmail());
            teacherEvent.put("department", registerRequest.getDepartment());
            teacherEvent.put("qualification", registerRequest.getQualification());
            teacherEvent.put("teacherId", generateTeacherId());

            registrationProducer.sendTeacherRegistrationEvent(teacherEvent);
            response.put("message", "Teacher registration event published");
        }

        return response;
    }

}