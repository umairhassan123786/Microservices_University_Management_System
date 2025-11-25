package com.university.Controller;
import com.university.DTO.*;
import com.university.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        TokenValidationResponse response = authService.validateToken(request.getToken());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody(required = false) RegisterRequest registerRequest) {
        if (registerRequest == null) {
            return ResponseEntity.badRequest().body("Request body is null!");
        }
        try {
            Map<String, Object> response = authService.register(registerRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract token from "Bearer <token>"
            String token = authService.extractTokenFromHeader(authorizationHeader);

            if (token == null) {
                return ResponseEntity.badRequest().body("Invalid authorization header");
            }

            boolean success = authService.logout(token);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "message", "Successfully logged out",
                        "timestamp", new Date()
                ));
            } else {
                return ResponseEntity.badRequest().body("Logout failed");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Logout error: " + e.getMessage());
        }
    }
//    @GetMapping("/profile")
//    public ResponseEntity<?> getLoggedInStudentProfile(@RequestHeader("Authorization") String authorizationHeader) {
//        try {
//            //Extract token from "Bearer <token>"
//            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Missing or invalid Authorization header"));
//            }
//
//            String token = authorizationHeader.substring(7);
//
//            // Call Auth Service to validate token & get user info
//            Map<String, Object> userData = studentService.getUserDetailsFromAuth(token);
//            if (userData == null || userData.get("userId") == null) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
//            }
//
//            Long userId = Long.parseLong(userData.get("userId").toString());
//
//            //Get student by userId
//            Optional<Student> student = studentService.getStudentByUserId(userId);
//            return student.<ResponseEntity<?>>map(ResponseEntity::ok)
//                    .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Student not found for this user")));
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
//        }
//    }

}