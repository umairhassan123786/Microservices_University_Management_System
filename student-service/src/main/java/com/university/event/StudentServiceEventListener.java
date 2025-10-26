//package com.university.event;
//import com.university.Entities.Student;
//import com.university.Repository.StudentRepository;
//import com.university.event.UserCreatedEvent;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//public class StudentServiceEventListener {
//
//    @Autowired
//    private StudentRepository studentRepository;
//
//    @EventListener
//    @Transactional
//    public void handleUserCreatedEvent(UserCreatedEvent event) {
//        if ("STUDENT".equals(event.getRole())) {
//            // Validate required fields
//            if (event.getFullName() == null || event.getFullName().trim().isEmpty()) {
//                throw new RuntimeException("Full name is required for student registration");
//            }
//            if (event.getStudentId() == null || event.getStudentId().trim().isEmpty()) {
//                throw new RuntimeException("Student ID is required");
//            }
//
//            // Check if student ID already exists
//            if (studentRepository.findByStudentId(event.getStudentId()).isPresent()) {
//                throw new RuntimeException("Student ID already exists: " + event.getStudentId());
//            }
//
//            // Create student profile with provided data
//            Student student = new Student();
//            student.setUserId(event.getUserId());
//            student.setName(event.getFullName()); // Use provided full name
//            student.setEmail(event.getEmail());
//            student.setStudentId(event.getStudentId()); // Use provided student ID
//            student.setDepartment(event.getDepartment() != null ? event.getDepartment() : "Not Assigned");
//            student.setSemester(event.getSemester() != null ? event.getSemester() : "1");
//
//            studentRepository.save(student);
//            System.out.println("Student profile created: " + event.getFullName() + " (" + event.getStudentId() + ")");
//        }
//    }
//}