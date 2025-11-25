package com.university.config;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(10000, 30000);
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                1000,
                5000,
                3
        );
    }

    @Bean
    public feign.codec.ErrorDecoder errorDecoder() {
        return new feign.codec.ErrorDecoder.Default();
    }
}