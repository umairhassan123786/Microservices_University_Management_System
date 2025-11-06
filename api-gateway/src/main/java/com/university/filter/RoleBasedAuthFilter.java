package com.university.filter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.Map;

//@Component
public class RoleBasedAuthFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;

    public RoleBasedAuthFilter() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

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

                    if (!hasAccess(role, path, userId, username)) {
                        return forbidden(exchange, "Insufficient permissions");
                    }

                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", userId != null ? userId.toString() : "")
                            .header("X-User-Role", role != null ? role : "")
                            .header("X-User-Name", username != null ? username : "")
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(e -> {
                    System.err.println("RoleBasedAuthFilter - Token validation error: " + e.getMessage());
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

    private boolean hasAccess(String role, String path, Long userId, String username) {
        if (role == null) return false;
        if ("STUDENT".equals(role)) {
            if (path.startsWith("/api/students/") && path.matches("/api/students/\\d+.*")) {
                Long studentIdFromPath = extractIdFromPath(path, "students");
                return studentIdFromPath != null && studentIdFromPath.equals(userId);
            }
            return path.startsWith("/api/students/");
        }

        if ("TEACHER".equals(role)) {
            return path.startsWith("/api/teachers/") ||
                    path.startsWith("/api/courses/") ||
                    path.startsWith("/api/attendance/");
        }

        if ("ADMIN".equals(role)) {
            return true;
        }

        return false;
    }

    private Long extractIdFromPath(String path, String resource) {
        try {
            String[] parts = path.split("/");
            for (int i = 0; i < parts.length; i++) {
                if (resource.equals(parts[i]) && i + 1 < parts.length) {
                    return Long.parseLong(parts[i + 1]);
                }
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