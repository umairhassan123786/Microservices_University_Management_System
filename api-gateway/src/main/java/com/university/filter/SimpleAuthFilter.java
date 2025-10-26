//package com.university.filter;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//public class SimpleAuthFilter extends AbstractGatewayFilterFactory<SimpleAuthFilter.Config> {
//
//    public SimpleAuthFilter() {
//        super(Config.class);
//    }
//
//    @Override
//    public GatewayFilter apply(Config config) {
//        return (exchange, chain) -> {
//            ServerHttpRequest request = exchange.getRequest();
//            String path = request.getPath().toString();
//            if (path.startsWith("/api/auth/")) {
//                return chain.filter(exchange);
//            }
//
//            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                return unauthorized(exchange, "Missing or invalid authorization token");
//            }
//            return chain.filter(exchange);
//        };
//    }
//
//    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
//        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//        return exchange.getResponse().setComplete();
//    }
//
//    public static class Config {
//    }
//}