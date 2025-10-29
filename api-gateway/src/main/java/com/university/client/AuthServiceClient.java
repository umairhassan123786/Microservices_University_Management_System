package com.university.client;
import com.university.dto.TokenValidationRequest;
import com.university.dto.TokenValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", url = "http://localhost:8081")
public interface AuthServiceClient {
    @PostMapping("/api/auth/validate")
    TokenValidationResponse validateToken(@RequestBody TokenValidationRequest request);
    @PostMapping("/api/auth/validate-with-role")
    TokenValidationResponse validateTokenWithRole(@RequestBody TokenValidationRequest request);
}