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
//        if (path.startsWith("/api/auth/")) {
//            System.out.println("Skipping JWT for auth endpoint");
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String header = request.getHeader("Authorization");
//        if (header != null && header.startsWith("Bearer ")) {
//            String token = header.substring(7);
//            try {
//                TokenValidationResponse validationResponse = authService.validateToken(token);
//                if (validationResponse.isValid()) {
//                    String username = validationResponse.getUsername();
//                    String role = validationResponse.getRole();
//                      Collection<GrantedAuthority> authorities =
//                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
//                      UsernamePasswordAuthenticationToken authentication =
//                            new UsernamePasswordAuthenticationToken(
//                                    username,
//                                    null,
//                                    authorities
//                            );
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                    Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
//                } else {
//                    SecurityContextHolder.clearContext();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                SecurityContextHolder.clearContext();
//            }
//        } else {
//                SecurityContextHolder.clearContext();
//        }
//        filterChain.doFilter(request, response);
//    }
//}