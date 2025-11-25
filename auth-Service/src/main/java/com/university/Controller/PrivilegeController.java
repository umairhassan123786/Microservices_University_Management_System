package com.university.Controller;
import com.university.DTO.PrivilegeRequest;
import com.university.DTO.UserPrivilegeResponse;
import com.university.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/privileges")
public class PrivilegeController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/assign",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> assignPrivileges(@Valid @RequestBody PrivilegeRequest request) {
        try {
            if (request.getPrivileges() == null || request.getPrivileges().isEmpty()) {
                List<String> currentPrivileges = authService.getActivePrivilegesForUser(request.getUserId());
                return ResponseEntity.ok(Map.of(
                        "message", "No privileges to add - returning current privileges",
                        "userId", request.getUserId(),
                        "currentPrivileges", currentPrivileges,
                        "operation", "NO_CHANGE"
                ));
            }

            List<String> assignedPrivileges = authService.assignPrivilegesToUser(request.getUserId(), request.getPrivileges());

            return ResponseEntity.ok(Map.of(
                    "message", "Privileges added successfully",
                    "userId", request.getUserId(),
                    "assignedPrivileges", assignedPrivileges,
                    "operation", "ADD"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping(value = "/set-with-token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> setPrivilegesWithNewToken(@Valid @RequestBody PrivilegeRequest request) {
        try {
            Map<String, Object> result = authService.updatePrivilegesAndGetNewToken(request.getUserId(), request.getPrivileges());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/set", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> setPrivileges(@Valid @RequestBody PrivilegeRequest request) {
        try {
            System.out.println("=== SET PRIVILEGES ===");
            System.out.println("User ID: " + request.getUserId());
            System.out.println("Privileges to SET: " + request.getPrivileges());

            List<String> setPrivileges = authService.setUserPrivileges(request.getUserId(), request.getPrivileges());

            String message = request.getPrivileges().isEmpty() ?
                    "All privileges cleared successfully" :
                    "Privileges set successfully";

            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "userId", request.getUserId(),
                    "privileges", setPrivileges,
                    "operation", "REPLACE",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            System.err.println("ERROR in setPrivileges: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PostMapping(value = "/revoke",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> revokePrivileges(@Valid @RequestBody PrivilegeRequest request) {
        try {
            List<String> revokedPrivileges = authService.revokePrivilegesFromUser(request.getUserId(), request.getPrivileges());

            return ResponseEntity.ok(Map.of(
                    "message", "Privileges revoked successfully",
                    "userId", request.getUserId(),
                    "revokedPrivileges", revokedPrivileges
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPrivileges(@PathVariable Long userId) {
        try {
            UserPrivilegeResponse response = authService.getUserPrivileges(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/has-privilege/{privilegeType}")
    public ResponseEntity<?> hasPrivilege(@PathVariable Long userId, @PathVariable String privilegeType) {
        try {
            System.out.println("=== CONTROLLER: Check Privilege ===");
            System.out.println("User ID: " + userId);
            System.out.println("Privilege Type: " + privilegeType);

            boolean hasPrivilege = authService.hasPrivilege(userId, privilegeType);

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "privilegeType", privilegeType,
                    "hasPrivilege", hasPrivilege,
                    "message", hasPrivilege ?
                            "User has the specified privilege" :
                            "User does not have the specified privilege",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            System.err.println("ERROR in hasPrivilege controller: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "userId", userId,
                    "privilegeType", privilegeType,
                    "hasPrivilege", false,
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    // Clear all privileges
    @PostMapping(value = "/clear/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> clearPrivileges(@PathVariable Long userId) {
        try {
            List<String> clearedPrivileges = authService.setUserPrivileges(userId, new ArrayList<>());

            return ResponseEntity.ok(Map.of(
                    "message", "All privileges cleared successfully",
                    "userId", userId,
                    "privileges", clearedPrivileges,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}