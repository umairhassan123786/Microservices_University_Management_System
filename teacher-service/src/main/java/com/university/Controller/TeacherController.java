package com.university.Controller;
import com.university.DTO.MarkAttendanceRequest;
import com.university.Entities.Attendance;
import com.university.Entities.Teacher;
import com.university.Repository.TeacherRepository;
import com.university.Service.TeacherService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private TeacherRepository teacherRepository;
    @GetMapping("/{teacherId}/courses")
    public ResponseEntity<List<Map<String, Object>>> getTeacherCourses(@PathVariable Long teacherId) {
        try {
            List<Map<String, Object>> courses = teacherService.getTeacherCourses(teacherId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{teacherId}/courses/{courseId}/students")
    public ResponseEntity<List<Map<String, Object>>> getStudentsInCourse(
            @PathVariable Long teacherId,
            @PathVariable Long courseId) {
        try {
            List<Map<String, Object>> students = teacherService.getStudentsInCourse(teacherId, courseId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{teacherId}/courses/{courseId}/attendance")
    public ResponseEntity<?> markAttendance(
            @PathVariable Long teacherId,
            @PathVariable Long courseId,
            @RequestBody MarkAttendanceRequest request) {
        try {
            Map<String, Object> result = teacherService.markAttendanceByTeacher(teacherId, courseId, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{teacherId}/courses/{courseId}/attendance")
    public ResponseEntity<?> getCourseAttendance(
            @PathVariable Long teacherId,
            @PathVariable Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            if (date != null) {
                List<Attendance> attendance = teacherService.getCourseAttendanceByDate(teacherId, courseId, date);
                return ResponseEntity.ok(attendance);
            } else {
                Map<String, Object> attendance = teacherService.getTeacherCourseAttendance(teacherId, courseId);
                return ResponseEntity.ok(attendance);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/exists/{teacherId}")
    public Boolean existsById(@PathVariable Long teacherId) {
        try {
            System.out.println("Checking teacher existence for ID: " + teacherId);
            boolean exists = teacherRepository.existsById(teacherId);
            System.out.println("Teacher exists: " + exists + " for ID: " + teacherId);
            return exists;
        } catch (Exception e) {
            System.out.println("Error checking teacher existence: " + e.getMessage());
            return false;
        }
    } @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeacherById(@PathVariable Long id) {
        try {
            Optional<Teacher> teacher = teacherService.getTeacherById(id);
            if (teacher.isPresent()) {
                Teacher t = teacher.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", t.getId());
                response.put("teacherId", t.getTeacherId());
                response.put("name", t.getName());
                response.put("email", t.getEmail());
                response.put("department", t.getDepartment());
                response.put("qualification", t.getQualification());
                response.put("userId", t.getUserId());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public Map<String, Object> createTeacher(@RequestBody Map<String, Object> teacherData) {
        try {
            System.out.println("Received teacher data: " + teacherData);

            Teacher savedTeacher = teacherService.createTeacher(teacherData);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedTeacher.getId());
            response.put("teacherId", savedTeacher.getTeacherId());
            response.put("name", savedTeacher.getName());
            response.put("email", savedTeacher.getEmail());
            response.put("userId", savedTeacher.getUserId());
            response.put("department", savedTeacher.getDepartment());
            response.put("message", "Teacher profile created successfully");

            System.out.println("Teacher created with ID: " + savedTeacher.getId() + ", UserId: " + savedTeacher.getUserId());
            return response;

        } catch (Exception e) {
            System.out.println("Error creating teacher: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create teacher: " + e.getMessage());
            errorResponse.put("userId", teacherData.get("userId"));
            return errorResponse;
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Teacher> getTeacherByUserId(@PathVariable Long userId) {
        try {
            Teacher teacher = teacherService.getTeacherByUserId(userId);
            return ResponseEntity.ok(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @RequestBody Teacher teacher) {
        try {
            Teacher updatedTeacher = teacherService.updateTeacher(id, teacher);
            return ResponseEntity.ok(updatedTeacher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{teacherId}/courses/{courseId}/bulk-attendance")
    public ResponseEntity<?> bulkMarkAttendance(
            @PathVariable Long teacherId,
            @PathVariable Long courseId,
            @RequestBody BulkAttendanceRequest bulkRequest) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("courseId", courseId);
            requestMap.put("date", bulkRequest.getDate().toString());
            requestMap.put("students", bulkRequest.getStudents());

            Map<String, Object> result = teacherService.bulkMarkAttendance(teacherId, requestMap);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        try {
            teacherService.deleteTeacher(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Getter
    @Setter
    public static class BulkAttendanceRequest {
        private LocalDate date;
        private List<Map<String, Object>> students;
    }
}