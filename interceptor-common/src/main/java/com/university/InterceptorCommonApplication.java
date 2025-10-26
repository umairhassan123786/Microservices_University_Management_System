package com.university;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class InterceptorCommonApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterceptorCommonApplication.class, args);
    }
}