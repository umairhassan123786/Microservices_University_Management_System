//package com.university.config;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Component;
//
//@Component("studentSecurity")
//public class StudentSecurity {
//
//    public boolean checkStudentId(Authentication authentication, Long studentId) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return false;
//        }
//
//        String role = authentication.getAuthorities().stream()
//                .findFirst()
//                .map(auth -> auth.getAuthority())
//                .orElse("");
//
//        // Admin can access any student data
//        if (role.equals("ROLE_ADMIN")) {
//            return true;
//        }
//
//        // Teacher can access student data (for their courses)
//        if (role.equals("ROLE_TEACHER")) {
//            return true;
//        }
//
//        // Student can only access their own data
//        // For now, simple check - in real app, you'd verify from database
//        if (role.equals("ROLE_STUDENT")) {
//            // This is simplified - you need to implement proper user-student mapping
//            return true;
//        }
//
//        return false;
//    }
//}