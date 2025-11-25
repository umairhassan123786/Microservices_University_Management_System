package com.university.filter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;
    private final WebClient studentServiceWebClient;

    public AuthFilter() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
        this.studentServiceWebClient = WebClient.builder()
                .baseUrl("http://localhost:8083")
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        String method = request.getMethod().name();

        if (isPublicEndpoint(path)) {
            System.out.println("Public endpoint - allowing access");
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Missing or invalid authorization header");
            return unauthorized(exchange, "Missing authorization token");
        }

        String token = authHeader.substring(7);
        return webClient.post()
                .uri("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Collections.singletonMap("token", token))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    Boolean isValid = (Boolean) response.get("valid");
                    if (!Boolean.TRUE.equals(isValid)) {
                        System.out.println("Token validation failed");
                        return unauthorized(exchange, "Invalid token");
                    }

                    String username = (String) response.get("username");
                    String role = (String) response.get("role");
                    Object userIdObj = response.get("userId");
                    Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;

                    @SuppressWarnings("unchecked")
                    List<String> privileges = (List<String>) response.get("privileges");
                    if (isPrivilegeManagementEndpoint(path, method)) {
                        System.out.println("Checking privilege management access...");

                        boolean hasPrivilegeAccess = canManagePrivileges(role, privileges);
                        if (!hasPrivilegeAccess) {
                            System.out.println("Access denied for privilege management");
                            return forbidden(exchange, "Only ADMIN users or users with ADMIN_CRUD privilege can manage privileges");
                        }

                        System.out.println("Privilege management access granted");
                    }
                   if (isStudentProfileAccess(path, method)) {
                        return handleStudentProfileAccess(exchange, chain, path, userId, role, username, privileges);
                    }
                    boolean hasAccess = hasAccess(role, path, method, userId, username, privileges);

                    if (!hasAccess) {
                        System.out.println("Access denied for path: " + path);
                        debugAccessCheck(role, path, method, privileges);
                        return forbidden(exchange, "Insufficient permissions");
                    }
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", userId != null ? userId.toString() : "")
                            .header("X-User-Role", role != null ? role : "")
                            .header("X-User-Name", username != null ? username : "")
                            .header("X-User-Privileges", privileges != null ? String.join(",", privileges) : "")
                            .build();

                    System.out.println("=== AUTH FILTER END - ACCESS GRANTED ===");
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(e -> {
                    System.err.println("AuthFilter - Token validation error: " + e.getMessage());
                    return unauthorized(exchange, "Token validation service unavailable");
                });
    }
    private boolean isPrivilegeManagementEndpoint(String path, String method) {
        boolean isPrivilegeEndpoint = path.startsWith("/api/privileges") &&
                ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method));

        System.out.println("Privilege management endpoint check: " + isPrivilegeEndpoint + " for " + path + " " + method);
        return isPrivilegeEndpoint;
    }
    private boolean canManagePrivileges(String role, List<String> privileges) {
        if ("ADMIN".equals(role)) {
            System.out.println("ACCESS GRANTED - ADMIN ROLE");
            return true;
        }
        if (privileges != null && privileges.contains("ADMIN_CRUD")) {
            System.out.println("ACCESS GRANTED - ADMIN_CRUD PRIVILEGE");
            return true;
        }

        System.out.println("ACCESS DENIED - No ADMIN role or ADMIN_CRUD privilege");
        return false;
    }

    private void debugAccessCheck(String role, String path, String method, List<String> privileges) {
        boolean privilegeAccess = hasPrivilegeAccess(path, method, privileges);
        System.out.println(" Privilege Access: " + privilegeAccess);

        boolean roleAccess = false;
        switch (role) {
            case "STUDENT":
                roleAccess = hasStudentAccess(path, method, null, privileges);
                break;
            case "TEACHER":
                roleAccess = hasTeacherAccess(path, method, null, privileges);
                break;
            case "ADMIN":
                roleAccess = true;
                break;
            default:
                roleAccess = false;
        }
    }

    private boolean hasAccess(String role, String path, String method, Long userId, String username, List<String> privileges) {
        if (role == null) {
            System.out.println("Role is null - denying access");
            return false;
        }
        boolean privilegeAccess = hasPrivilegeAccess(path, method, privileges);
        System.out.println("Privilege access: " + privilegeAccess);

        if (privilegeAccess) {
            return true;
        }
        boolean roleAccess = false;
        switch (role) {
            case "STUDENT":
                roleAccess = hasStudentAccess(path, method, userId, privileges);
                break;
            case "TEACHER":
                roleAccess = hasTeacherAccess(path, method, userId, privileges);
                break;
            case "ADMIN":
                roleAccess = true;
                break;
            default:
                roleAccess = false;
        }

        System.out.println("Role access: " + roleAccess);
        return roleAccess;
    }

    private boolean hasPrivilegeAccess(String path, String method, List<String> privileges) {
        if (privileges == null || privileges.isEmpty()) {
            System.out.println("No privileges available");
            return false;
        }

        System.out.println("Checking privilege access for: " + path + " (" + method + ")");
        if (privileges.contains("ADMIN_CRUD")) {
            System.out.println("ADMIN_CRUD found - granting FULL ACCESS");
            return true;
        }
        if (path.startsWith("/api/students") && privileges.contains("STUDENT_CRUD")) {
            System.out.println("STUDENT_CRUD found - granting students access");
            return true;
        }

        if (path.startsWith("/api/teachers") && privileges.contains("TEACHER_CRUD")) {
            System.out.println("TEACHER_CRUD found - granting teachers access");
            return true;
        }
        if (path.startsWith("/api/courses") && privileges.contains("COURSE_MANAGEMENT")) {
            System.out.println("COURSE_MANAGEMENT found - granting courses access");
            return true;
        }
        if (path.startsWith("/api/attendance") && privileges.contains("ATTENDANCE_MANAGEMENT")) {
            System.out.println("ATTENDANCE_MANAGEMENT found - granting attendance access");
            return true;
        }

        System.out.println("No matching privileges found");
        return false;
    }

    private Mono<Void> handleStudentProfileAccess(ServerWebExchange exchange, GatewayFilterChain chain,
                                                  String path, Long userId, String role, String username, List<String> privileges) {
        Long studentIdFromPath = extractStudentIdFromPath(path);

        if (studentIdFromPath == null) {
            return badRequest(exchange, "Invalid student ID in path");
        }

        System.out.println("Student profile access - Student ID from path: " + studentIdFromPath);
        if ("ADMIN".equals(role) || "TEACHER".equals(role)) {
            System.out.println("Admin/Teacher access granted to student profile");
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId.toString())
                    .header("X-User-Role", role)
                    .header("X-User-Name", username != null ? username : "")
                    .header("X-User-Privileges", privileges != null ? String.join(",", privileges) : "")
                    .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }
        if ("STUDENT".equals(role)) {
            System.out.println("Checking student profile ownership");
            return studentServiceWebClient.get()
                    .uri("/api/students/{studentId}/profile", studentIdFromPath)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .flatMap(profileResponse -> {
                        Map<String, Object> studentData = (Map<String, Object>) profileResponse.get("student");
                        if (studentData == null) {
                            return forbidden(exchange, "Student profile not found");
                        }

                        Object profileUserIdObj = studentData.get("userId");
                        if (profileUserIdObj == null) {
                            return forbidden(exchange, "Unable to verify profile ownership");
                        }

                        Long profileUserId = Long.valueOf(profileUserIdObj.toString());

                        System.out.println("Token UserId=" + userId + ", Profile UserId=" + profileUserId);
                        if (profileUserId.equals(userId)) {
                            System.out.println("Student profile access granted - ownership verified");
                            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId.toString())
                                    .header("X-User-Role", role)
                                    .header("X-User-Name", username != null ? username : "")
                                    .header("X-User-Privileges", privileges != null ? String.join(",", privileges) : "")
                                    .build();
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        } else {
                            System.out.println("Student profile access denied - ownership mismatch");
                            return forbidden(exchange, "You don't have access to this student profile");
                        }
                    })
                    .onErrorResume(e -> {
                        System.err.println("Error fetching student profile: " + e.getMessage());
                        return forbidden(exchange, "Unable to verify profile ownership - service error");
                    });
        }

        return forbidden(exchange, "Insufficient permissions to access student profile");
    }

    private boolean hasStudentAccess(String path, String method, Long studentId, List<String> privileges) {
        System.out.println("Student access check - Path: " + path + ", StudentId: " + studentId);
        if (isStudentProfileAccess(path, method)) {
            return true;
        }
        if (path.startsWith("/api/students/")) {
            Long pathStudentId = extractIdFromPath(path, "students");
            if (pathStudentId != null) {
                boolean allowed = pathStudentId.equals(studentId);
                System.out.println("Student ID comparison - Path: " + pathStudentId + ", Token: " + studentId + ", Allowed: " + allowed);
                return allowed;
            }
            return isAllowedGeneralStudentEndpoint(path, method);
        }

        if (path.startsWith("/api/courses/") && "GET".equals(method)) {
            return true;
        }

        if (path.startsWith("/api/attendance/")) {
            Long attendanceStudentId = extractIdFromPath(path, "attendance");
            if (attendanceStudentId != null) {
                return attendanceStudentId.equals(studentId);
            }
            return path.equals("/api/attendance/my-attendance") && "GET".equals(method);
        }

        return false;
    }

    private boolean hasTeacherAccess(String path, String method, Long teacherId, List<String> privileges) {
        if (path.startsWith("/api/teachers/")) {
            Long pathTeacherId = extractIdFromPath(path, "teachers");
            if (pathTeacherId != null) {
                return pathTeacherId.equals(teacherId);
            }
            return true;
        }

        if (path.startsWith("/api/courses/")) {
            return true;
        }

        if (path.startsWith("/api/attendance/")) {
            return true;
        }

        if (path.startsWith("/api/students/") && "GET".equals(method)) {
            return true;
        }

        return false;
    }

    private boolean isStudentProfileAccess(String path, String method) {
        boolean isProfileAccess = path.matches("/api/students/\\d+/profile") && "GET".equals(method);
        System.out.println("Is student profile access: " + isProfileAccess + " for path: " + path);
        return isProfileAccess;
    }

    private Long extractStudentIdFromPath(String path) {
        try {
            Pattern pattern = Pattern.compile("/api/students/(\\d+)/profile");
            java.util.regex.Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        } catch (NumberFormatException e) {
            System.err.println("Error extracting student ID from path: " + path);
            return null;
        }
        return null;
    }

    private Long extractIdFromPath(String path, String resource) {
        try {
            Pattern pattern = Pattern.compile("/api/" + resource + "/(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        } catch (NumberFormatException e) {
            System.err.println("Error extracting ID from path: " + path + " for resource: " + resource);
            return null;
        }
        return null;
    }

    private boolean isAllowedGeneralStudentEndpoint(String path, String method) {
        boolean allowed = (path.equals("/api/students/courses") && "GET".equals(method)) ||
                (path.equals("/api/students/schedule") && "GET".equals(method)) ||
                (path.equals("/api/students/profile") && "GET".equals(method));

        System.out.println("🎓 Is allowed general student endpoint: " + allowed + " for path: " + path);
        return allowed;
    }

    private boolean isPublicEndpoint(String path) {
        boolean isPublic = path.startsWith("/api/auth/") ||
                path.contains("/actuator/health") ||
                path.contains("/eureka/") ||
                path.equals("/api/auth/login") ||
                path.equals("/api/auth/register");

        System.out.println("Is public endpoint: " + isPublic + " for path: " + path);
        return isPublic;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        System.out.println("Unauthorized: " + message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        System.out.println("Forbidden: " + message);
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"error\": \"Forbidden\", \"message\": \"" + message + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private Mono<Void> badRequest(ServerWebExchange exchange, String message) {
        System.out.println("Bad Request: " + message);
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"error\": \"Bad Request\", \"message\": \"" + message + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}