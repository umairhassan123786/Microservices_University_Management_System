package com.university.config;
import com.university.interceptor.TokenForwardingInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced  // Eureka service discovery ke liye
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
                Collections.singletonList(new TokenForwardingInterceptor())
        );
        return restTemplate;
    }
}