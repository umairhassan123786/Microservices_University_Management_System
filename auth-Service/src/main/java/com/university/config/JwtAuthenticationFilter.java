//package com.university.config;
//
//import com.university.Service.AuthService;
//import com.university.DTO.TokenValidationResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Collections;
//
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private AuthService authService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String path = request.getServletPath();
//        String method = request.getMethod();
//
//        System.out.println("🚀 === JWT FILTER START ===");
//        System.out.println("🔍 Path: " + path);
//        System.out.println("🔍 Method: " + method);
//
//        // ✅ Skip JWT filter for auth endpoints
//        if (path.startsWith("/api/auth/")) {
//            System.out.println("✅ Skipping JWT for auth endpoint");
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String header = request.getHeader("Authorization");
//        System.out.println("🔍 Authorization Header: " + header);
//
//        if (header != null && header.startsWith("Bearer ")) {
//            String token = header.substring(7);
//            System.out.println("🔍 Token: " + token.substring(0, 20) + "...");
//
//            try {
//                TokenValidationResponse validationResponse = authService.validateToken(token);
//                System.out.println("🔍 Validation Response: " + validationResponse.isValid());
//                System.out.println("🔍 Validation Message: " + validationResponse.getMessage());
//
//                if (validationResponse.isValid()) {
//                    String username = validationResponse.getUsername();
//                    String role = validationResponse.getRole();
//
//                    System.out.println("✅ Valid Token - User: " + username + ", Role: " + role);
//
//                    // ✅ Create authorities with ROLE_ prefix
//                    Collection<GrantedAuthority> authorities =
//                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
//
//                    System.out.println("✅ Authorities: " + authorities);
//
//                    UsernamePasswordAuthenticationToken authentication =
//                            new UsernamePasswordAuthenticationToken(
//                                    username,
//                                    null,
//                                    authorities
//                            );
//
//                    // ✅ Set authentication in security context
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//
//                    // ✅ Verify authentication is set
//                    Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
//                    if (currentAuth != null && currentAuth.isAuthenticated()) {
//                        System.out.println("✅ Security Context SET - User: " + currentAuth.getName());
//                        System.out.println("✅ Security Context SET - Authorities: " + currentAuth.getAuthorities());
//                    } else {
//                        System.out.println("❌ Security Context NOT SET");
//                    }
//
//                } else {
//                    System.out.println("❌ Token invalid - clearing security context");
//                    SecurityContextHolder.clearContext();
//                }
//            } catch (Exception e) {
//                System.out.println("❌ Token validation error: " + e.getMessage());
//                e.printStackTrace();
//                SecurityContextHolder.clearContext();
//            }
//        } else {
//            System.out.println("❌ No valid Authorization header found");
//            SecurityContextHolder.clearContext();
//        }
//
//        System.out.println("🎯 === JWT FILTER END ===");
//        filterChain.doFilter(request, response);
//    }
//}