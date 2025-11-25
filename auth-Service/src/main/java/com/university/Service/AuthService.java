package com.university.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.DTO.*;
import com.university.Entities.Privilege;
import com.university.Entities.User;
import com.university.Repository.PrivilegeRepository;
import com.university.Repository.UserRepository;
import com.university.Util.JwtUtil;
import com.university.Util.PasswordUtil;
import com.university.client.StudentServiceClient;
import com.university.client.TeacherServiceClient;
import com.university.kafka.UserRegistrationProducer;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.*;


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

    @Autowired
    private PrivilegeRepository privilegeRepository;

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

        List<String> privileges = getActivePrivilegesForUser(user.getId());
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId(), privileges);
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
                System.out.println("Token is blacklisted");
                return new TokenValidationResponse(false, "Token has been invalidated", null, null, null, null);
            }

            boolean isValid = jwtUtil.validateToken(token);
            if (!isValid) {
                return new TokenValidationResponse(false, "Invalid token", null, null, null, null);
            }

            Claims claims = jwtUtil.getClaimsFromToken(token);

            String username = claims.getSubject();
            String role = (String) claims.get("role");
            Object userIdObj = claims.get("userId");
            Long userId = null;

            if (userIdObj != null) {
                if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                }
            }

            List<String> latestPrivileges = getActivePrivilegesForUser(userId);

            return new TokenValidationResponse(true, "Token is valid", username, role, userId, latestPrivileges);

        } catch (Exception e) {
            return new TokenValidationResponse(false, "Token validation failed: " + e.getMessage(), null, null, null, null);
        }
    }
    public Map<String, Object> validateTokenAsMap(String token) {
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                System.out.println("Token is blacklisted");
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("error", "Token has been invalidated");
                return response;
            }
            boolean isValid = jwtUtil.validateToken(token);
            if (!isValid) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("error", "Invalid token");
                return response;
            }
            Claims claims = jwtUtil.getClaimsFromToken(token);

            String username = claims.getSubject();
            String role = (String) claims.get("role");
            Object userIdObj = claims.get("userId");
            Long userId = null;

            if (userIdObj != null) {
                if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                }
            }
            List<String> latestPrivileges = getActivePrivilegesForUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("username", username);
            response.put("role", role);
            response.put("userId", userId);
            response.put("privileges", latestPrivileges);

            return response;

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    public Map<String, Object> validateTokenWithClaims(String token) {
        try {
            Claims claims = jwtUtil.validateTokenAndGetClaims(token);

            String username = claims.getSubject();
            String role = (String) claims.get("role");
            Object userIdObj = claims.get("userId");
            Long userId = null;

            if (userIdObj != null) {
                if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                }
            }
            List<String> latestPrivileges = getActivePrivilegesForUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("username", username);
            response.put("role", role);
            response.put("userId", userId);
            response.put("privileges", latestPrivileges);

            return response;

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", e.getMessage());
            return response;
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

    private Map<String, Object> createTeacherProfile(Long userId, RegisterRequest request) {
        try {
            Map<String, Object> teacherData = new HashMap<>();
            teacherData.put("userId", userId);
            teacherData.put("name", request.getFullName() != null ? request.getFullName() : request.getUsername());
            teacherData.put("email", request.getEmail());
            teacherData.put("department", request.getDepartment());
            teacherData.put("qualification", request.getQualification());
            teacherData.put("teacherId", generateTeacherId());
            try {
                Map<String, Object> response = teacherServiceClient.createTeacher(teacherData);
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
    }

    public Object getTeacherByUserId(Long userId) {
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
            try {
                Map<String, Object> response = studentServiceClient.createStudent(studentData);
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

    public List<String> assignPrivilegesToUser(Long userId, List<String> privilegeTypes) {
        try {
            Optional<Privilege> existingPrivilegeOpt = privilegeRepository.findByUserId(userId);

            List<String> currentPrivileges;
            if (existingPrivilegeOpt.isPresent()) {

                Privilege privilege = existingPrivilegeOpt.get();
                currentPrivileges = getPrivilegesFromJson(privilege.getPrivilegesJson());
                System.out.println("Current privileges: " + currentPrivileges);
                for (String privilegeType : privilegeTypes) {
                    if (!currentPrivileges.contains(privilegeType)) {
                        currentPrivileges.add(privilegeType);
                        System.out.println("Added privilege: " + privilegeType);
                    } else {
                        System.out.println("Privilege already exists: " + privilegeType);
                    }
                }

                privilege.setPrivilegesJson(convertPrivilegesToJson(currentPrivileges));
                privilege.setActive(true);
                privilegeRepository.save(privilege);

                System.out.println("Final privileges after ADD: " + currentPrivileges);
            } else {
                currentPrivileges = new ArrayList<>(new LinkedHashSet<>(privilegeTypes)); // Remove duplicates
                Privilege privilege = new Privilege();
                privilege.setUserId(userId);
                privilege.setPrivilegesJson(convertPrivilegesToJson(currentPrivileges));
                privilege.setActive(true);
                privilegeRepository.save(privilege);
            }

            return currentPrivileges;
        } catch (Exception e) {
            System.err.println("ERROR in assignPrivilegesToUser: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to assign privileges: " + e.getMessage());
        }
    }

    public List<String> setUserPrivileges(Long userId, List<String> privilegeTypes) {
        try {
            List<String> uniquePrivileges = new ArrayList<>(new LinkedHashSet<>(privilegeTypes));
            String newJson = convertPrivilegesToJson(uniquePrivileges);

            Optional<Privilege> existingPrivilegeOpt = privilegeRepository.findByUserId(userId);
            if (existingPrivilegeOpt.isPresent()) {
                Privilege privilege = existingPrivilegeOpt.get();
                privilege.setPrivilegesJson(newJson);
                privilege.setActive(true);
                Privilege savedPrivilege = privilegeRepository.saveAndFlush(privilege);
            } else {
                Privilege privilege = new Privilege();
                privilege.setUserId(userId);
                privilege.setPrivilegesJson(newJson);
                privilege.setActive(true);

                Privilege savedPrivilege = privilegeRepository.save(privilege);
                System.out.println("New record created - ID: " + savedPrivilege.getId() + ", JSON: " + savedPrivilege.getPrivilegesJson());
            }
            privilegeRepository.flush();
            Optional<Privilege> verifyOpt = privilegeRepository.findByUserId(userId);
            if (verifyOpt.isPresent()) {
            }
            return uniquePrivileges;
        } catch (Exception e) {
            System.err.println("ERROR in setUserPrivileges: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to set privileges: " + e.getMessage());
        }
    }

    public List<String> revokePrivilegesFromUser(Long userId, List<String> privilegeTypes) {
        try {
            Optional<Privilege> existingPrivilegeOpt = privilegeRepository.findByUserId(userId);
            if (existingPrivilegeOpt.isEmpty()) {
                return new ArrayList<>();
            }
            Privilege privilege = existingPrivilegeOpt.get();
            List<String> currentPrivileges = getPrivilegesFromJson(privilege.getPrivilegesJson());
            if (currentPrivileges == null) {
                currentPrivileges = new ArrayList<>();
            }
            List<String> privilegesToRemove = new ArrayList<>(privilegeTypes);
            currentPrivileges.removeAll(privilegesToRemove);
            privilege.setPrivilegesJson(convertPrivilegesToJson(currentPrivileges));
            privilege.setActive(true);
            privilegeRepository.save(privilege);
            return currentPrivileges;
        } catch (Exception e) {
            System.err.println("ERROR in revokePrivilegesFromUser: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to revoke privileges: " + e.getMessage());
        }
    }

    public List<String> getActivePrivilegesForUser(Long userId) {
        try {
            Optional<Privilege> privilegeOpt = privilegeRepository.findByUserIdAndActive(userId, 1);
            if (privilegeOpt.isPresent()) {
                List<String> privileges = getPrivilegesFromJson(privilegeOpt.get().getPrivilegesJson());
                System.out.println("DEBUG: Retrieved privileges for user " + userId + ": " + privileges);
                return privileges != null ? privileges : new ArrayList<>();
            }
            System.out.println("DEBUG: No privileges found for user " + userId);
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("ERROR in getActivePrivilegesForUser: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean hasPrivilege(Long userId, String privilegeType) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                System.out.println("DEBUG: User not found with ID: " + userId);
                return false;
            }
            List<String> privileges = getActivePrivilegesForUser(userId);
            System.out.println("DEBUG: User privileges: " + privileges);
            boolean hasPrivilege = privileges.contains(privilegeType);
            System.out.println("DEBUG: User " + userId + " has privilege '" + privilegeType + "': " + hasPrivilege);

            return hasPrivilege;

        } catch (Exception e) {
            System.err.println("ERROR in hasPrivilege: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public UserPrivilegeResponse getUserPrivileges(Long userId) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User user = userOptional.get();
            List<String> privileges = getActivePrivilegesForUser(userId);

            UserPrivilegeResponse response = new UserPrivilegeResponse();
            response.setUserId(userId);
            response.setUsername(user.getUsername());
            response.setRole(user.getRole());
            response.setPrivileges(privileges);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user privileges: " + e.getMessage());
        }
    }

    private String convertPrivilegesToJson(List<String> privileges) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(privileges);
        } catch (Exception e) {
            System.err.println("ERROR converting privileges to JSON: " + e.getMessage());
            return "[]";
        }
    }

    private List<String> getPrivilegesFromJson(String privilegesJson) {
        try {
            if (privilegesJson == null || privilegesJson.trim().isEmpty() || "null".equals(privilegesJson)) {
                System.out.println("DEBUG: Empty or null JSON, returning empty list");
                return new ArrayList<>();
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(privilegesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            System.err.println("ERROR parsing privileges JSON: " + e.getMessage() + " - JSON: " + privilegesJson);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> updatePrivilegesAndGetNewToken(Long userId, List<String> privileges) {
        try {
            List<String> updatedPrivileges = setUserPrivileges(userId, privileges);
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String newToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId(), updatedPrivileges);
                return Map.of(
                        "message", "Privileges updated successfully",
                        "userId", userId,
                        "privileges", updatedPrivileges,
                        "newToken", newToken,
                        "timestamp", System.currentTimeMillis()
                );
            } else {
                throw new RuntimeException("User not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update privileges: " + e.getMessage());
        }
    }

    public Map<String, Object> updatePrivilegesWithoutNewToken(Long userId, List<String> privileges) {
        try {
            List<String> updatedPrivileges = setUserPrivileges(userId, privileges);

            System.out.println("Privileges updated without token refresh - User: " + userId + ", Privileges: " + updatedPrivileges);

            return Map.of(
                    "message", "Privileges updated successfully",
                    "userId", userId,
                    "privileges", updatedPrivileges,
                    "timestamp", System.currentTimeMillis()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to update privileges: " + e.getMessage());
        }
    }
}