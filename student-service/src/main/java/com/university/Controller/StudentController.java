package com.university.Controller;
import com.university.DTO.AttendanceDTO;
import com.university.DTO.CourseDTO;
import com.university.DTO.StudentProfileDTO;
import com.university.Entities.Student;
import com.university.Repository.StudentRepository;
import com.university.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/{studentId}/courses")
    public ResponseEntity<List<CourseDTO>> getStudentCourses(@PathVariable Long studentId) {
        try {
            List<CourseDTO> courses = studentService.getStudentCourses(studentId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{studentId}/attendance")
    public ResponseEntity<List<AttendanceDTO>> getStudentAttendance(@PathVariable Long studentId) {
        try {
            List<AttendanceDTO> attendance = studentService.getStudentAttendance(studentId);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{studentId}/attendance/course/{courseId}")
    public ResponseEntity<List<AttendanceDTO>> getCourseAttendance(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            List<AttendanceDTO> attendance = studentService.getCourseAttendance(studentId, courseId);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{studentId}/profile")
    public ResponseEntity<StudentProfileDTO> getStudentProfile(@PathVariable Long studentId) {
        try {
            StudentProfileDTO profile = studentService.getStudentProfile(studentId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
//        Optional<Student> student = studentService.getStudentById(id);
//        return student.map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Student> getStudentByUserId(@PathVariable Long userId) {
        Optional<Student> student = studentService.getStudentByUserId(userId);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> createStudent(@RequestBody Map<String, Object> studentData) {
        try {
            System.out.println("Received student data: " + studentData);
            if (!studentData.containsKey("userId") || !studentData.containsKey("rollNumber")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "userId and rollNumber are required fields"
                ));
            }

            Object student = studentService.createStudent(studentData);
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            System.out.println("Error creating student: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "status", "FAILED"
            ));
        }
    }

    @GetMapping("/check-rollnumber/{rollNumber}")
    public ResponseEntity<Boolean> existsByRollNumber(@PathVariable String rollNumber) {
        boolean exists = studentService.existsByRollNumber(rollNumber);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/{studentId}")
    public Boolean existsById(@PathVariable Long studentId) {
        try {
            System.out.println(" Checking student existence for ID: " + studentId);
            boolean exists = studentRepository.existsById(studentId);
            System.out.println("Student exists: " + exists + " for ID: " + studentId);
            return exists;
        } catch (Exception e) {
            System.out.println("Error checking student existence: " + e.getMessage());
            return false;
        }
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(@PathVariable Long studentId) {
        try {
            Object student = studentService.getStudentById(studentId);
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        try {
            Student updatedStudent = studentService.updateStudent(id, student);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/{studentId}/profileES")
    public ResponseEntity<Student> getStudentProfileByES(@PathVariable Long studentId) {
        try {
            Student profile = studentService.getStudentProfileByES(studentId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

//    @GetMapping("/searchES")
//    public ResponseEntity<List<Student>> searchStudentsByES(@RequestParam String name) {
//        try {
//            List<Student> students = studentService.searchStudentsByName(name);
//            return ResponseEntity.ok(students);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    // NEW: Get by department
//    @GetMapping("/department/{department}")
//    public ResponseEntity<List<Student>> getByDepartment(@PathVariable String department) {
//        try {
//            List<Student> students = studentService.getStudentsByDepartment(department);
//            return ResponseEntity.ok(students);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
}