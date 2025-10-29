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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            Map<String, Object> response = authService.register(registerRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }    @PostMapping("/logout")
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
}