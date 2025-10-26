//package com.university.config;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Component;
//
//@Component("teacherSecurity")
//public class TeacherSecurity {
//
//    public boolean checkTeacherId(Authentication authentication, Long teacherId) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return false;
//        }
//
//        String role = authentication.getAuthorities().stream()
//                .findFirst()
//                .map(auth -> auth.getAuthority())
//                .orElse("");
//
//        // Admin can access any teacher data
//        if (role.equals("ROLE_ADMIN")) {
//            return true;
//        }
//
//        // Teacher can only access their own data
//        if (role.equals("ROLE_TEACHER")) {
//            // Simplified - in real app, check if teacherId matches logged-in teacher
//            return true;
//        }
//
//        return false;
//    }
//}