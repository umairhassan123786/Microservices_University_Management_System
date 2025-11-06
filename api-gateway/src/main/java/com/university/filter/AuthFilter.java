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

        System.out.println("DEBUG: Request Path: " + path + ", Method: " + method);

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
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
                        return unauthorized(exchange, "Invalid token");
                    }

                    String username = (String) response.get("username");
                    String role = (String) response.get("role");
                    Object userIdObj = response.get("userId");
                    Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;

                    System.out.println("DEBUG: User Role: " + role + ", UserId: " + userId);

                    // Special handling for student profile access - FIRST CHECK THIS
                    if (isStudentProfileAccess(path, method)) {
                        System.out.println("DEBUG: Handling student profile access");
                        return handleStudentProfileAccess(exchange, chain, path, userId, role, username);
                    }

                    // General access control for other endpoints
                    if (!hasAccess(role, path, method, userId, username)) {
                        System.out.println("DEBUG: Access denied by hasAccess method");
                        return forbidden(exchange, "Insufficient permissions");
                    }

                    // Add user info to headers and continue
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", userId != null ? userId.toString() : "")
                            .header("X-User-Role", role != null ? role : "")
                            .header("X-User-Name", username != null ? username : "")
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(e -> {
                    System.err.println("AuthFilter - Token validation error: " + e.getMessage());
                    return unauthorized(exchange, "Token validation service unavailable");
                });
    }

    private Mono<Void> handleStudentProfileAccess(ServerWebExchange exchange, GatewayFilterChain chain,
                                                  String path, Long userId, String role, String username) {
        Long studentIdFromPath = extractStudentIdFromPath(path);

        if (studentIdFromPath == null) {
            return badRequest(exchange, "Invalid student ID in path");
        }

        System.out.println("DEBUG: Student ID from path: " + studentIdFromPath);

        // ADMIN and TEACHER can access any student profile
        if ("ADMIN".equals(role) || "TEACHER".equals(role)) {
            System.out.println("DEBUG: Admin/Teacher access granted");
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId.toString())
                    .header("X-User-Role", role)
                    .header("X-User-Name", username != null ? username : "")
                    .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        // STUDENT role - check if they are accessing their own profile
        if ("STUDENT".equals(role)) {
            System.out.println("DEBUG: Checking student profile ownership");
            return studentServiceWebClient.get()
                    .uri("/api/students/{studentId}/profile", studentIdFromPath)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .flatMap(profileResponse -> {
                        // Extract userId from student profile response
                        Map<String, Object> studentData = (Map<String, Object>) profileResponse.get("student");
                        if (studentData == null) {
                            return forbidden(exchange, "Student profile not found");
                        }

                        Object profileUserIdObj = studentData.get("userId");
                        if (profileUserIdObj == null) {
                            return forbidden(exchange, "Unable to verify profile ownership");
                        }

                        Long profileUserId = Long.valueOf(profileUserIdObj.toString());

                        System.out.println("DEBUG: Token UserId=" + userId + ", Profile UserId=" + profileUserId);

                        // Check if the logged-in user owns this profile
                        if (profileUserId.equals(userId)) {
                            System.out.println("DEBUG: Student profile access granted - ownership verified");
                            // User owns this profile, allow access
                            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId.toString())
                                    .header("X-User-Role", role)
                                    .header("X-User-Name", username != null ? username : "")
                                    .build();
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        } else {
                            System.out.println("DEBUG: Student profile access denied - ownership mismatch");
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

    private boolean isStudentProfileAccess(String path, String method) {
        boolean isProfileAccess = path.matches("/api/students/\\d+/profile") && "GET".equals(method);
        System.out.println("DEBUG: isStudentProfileAccess - " + isProfileAccess + " for path: " + path);
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
            return null;
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
                path.contains("/actuator/health") ||
                path.contains("/eureka/") ||
                path.equals("/api/auth/login") ||
                path.equals("/api/auth/register");
    }

    private boolean hasAccess(String role, String path, String method, Long userId, String username) {
        if (role == null) return false;

        System.out.println("DEBUG: hasAccess called - Role: " + role + ", Path: " + path);

        // If it's student profile access, it should have been handled already
        if (isStudentProfileAccess(path, method)) {
            System.out.println("DEBUG: Student profile access should be handled separately");
            return true;
        }

        switch (role) {
            case "STUDENT":
                return hasStudentAccess(path, method, userId);
            case "TEACHER":
                return hasTeacherAccess(path, method, userId);
            case "ADMIN":
                return true;
            default:
                return false;
        }
    }

    private boolean hasStudentAccess(String path, String method, Long studentId) {
        System.out.println("DEBUG: hasStudentAccess - Path: " + path + ", StudentId: " + studentId);

        // Student profile access is handled separately in handleStudentProfileAccess
        if (isStudentProfileAccess(path, method)) {
            System.out.println("DEBUG: Student profile access - allowing for separate handling");
            return true; // Actual ownership check happens in handleStudentProfileAccess
        }

        // For other student endpoints, check ownership
        if (path.startsWith("/api/students/")) {
            Long pathStudentId = extractIdFromPath(path, "students");
            if (pathStudentId != null) {
                boolean allowed = pathStudentId.equals(studentId);
                System.out.println("DEBUG: Student ID comparison - Path: " + pathStudentId + ", Token: " + studentId + ", Allowed: " + allowed);
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

    private boolean isAllowedGeneralStudentEndpoint(String path, String method) {
        return (path.equals("/api/students/courses") && "GET".equals(method)) ||
                (path.equals("/api/students/schedule") && "GET".equals(method)) ||
                (path.equals("/api/students/profile") && "GET".equals(method));
    }

    private boolean hasTeacherAccess(String path, String method, Long teacherId) {
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

    private Long extractIdFromPath(String path, String resource) {
        try {
            Pattern pattern = Pattern.compile("/api/" + resource + "/(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"error\": \"Forbidden\", \"message\": \"" + message + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private Mono<Void> badRequest(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"error\": \"Bad Request\", \"message\": \"" + message + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}