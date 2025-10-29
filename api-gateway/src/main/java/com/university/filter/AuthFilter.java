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

    // Fixed constructor - properly initialize WebClient
    public AuthFilter() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        String method = request.getMethod().name();

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

                    // Check if user has access to the requested resource
                    if (!hasAccess(role, path, method, userId, username)) {
                        return forbidden(exchange, "Insufficient permissions");
                    }

                    // Add user info to headers for downstream services
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
        // Students can access general student endpoints
        if (path.equals("/api/students/profile") && "GET".equals(method)) {
            return true;
        }

        // Students can access their own specific data
        if (path.startsWith("/api/students/")) {
            Long pathStudentId = extractIdFromPath(path, "students");
            if (pathStudentId != null) {
                // Student can only access their own data
                return pathStudentId.equals(studentId);
            }

            // If no ID in path, check if it's a general student endpoint they're allowed to access
            return isAllowedGeneralStudentEndpoint(path, method);
        }

        // Students can view courses
        if (path.startsWith("/api/courses/") && "GET".equals(method)) {
            return true;
        }

        // Students can view their own attendance
        if (path.startsWith("/api/attendance/")) {
            Long attendanceStudentId = extractIdFromPath(path, "attendance");
            if (attendanceStudentId != null) {
                return attendanceStudentId.equals(studentId);
            }
            // Allow general attendance endpoints for student's own data
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
        // Teachers can access their own teacher profile
        if (path.startsWith("/api/teachers/")) {
            Long pathTeacherId = extractIdFromPath(path, "teachers");
            if (pathTeacherId != null) {
                // Teachers can access their own data, admins can access all
                return pathTeacherId.equals(teacherId);
            }
            return true;
        }

        // Teachers can manage courses
        if (path.startsWith("/api/courses/")) {
            return true;
        }

        // Teachers can manage attendance
        if (path.startsWith("/api/attendance/")) {
            return true;
        }

        // Teachers can view student profiles (read-only)
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
}