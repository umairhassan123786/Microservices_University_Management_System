package com.university.Controller;
import com.university.Entities.Course;
import com.university.Entities.StudentCourse;
import com.university.Repository.StudentCourseRepository;
import com.university.Service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final StudentCourseRepository studentCourseRepository; // ✅ Import add kiya

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        try {
            Course course = courseService.getCourseById(id);
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseService.createCourse(course);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        try {
            Course updatedCourse = courseService.updateCourse(id, course);
            return ResponseEntity.ok(updatedCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok("Course deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/student/{studentId}")
    public List<Course> getCoursesByStudentId(@PathVariable Long studentId) {
        return courseService.getCoursesByStudentId(studentId);
    }

    @GetMapping("/teacher/{teacherId}")
    public List<Course> getCoursesByTeacherId(@PathVariable Long teacherId) {
        return courseService.getCoursesByTeacherId(teacherId);
    }

    @GetMapping("/without-teacher")
    public List<Course> getCoursesWithoutTeacher() {
        return courseService.getCoursesWithoutTeacher();
    }

    @PostMapping("/enroll")
    public StudentCourse enrollStudent(@RequestBody StudentCourse enrollment) {
        return courseService.enrollStudent(enrollment);
    }

    @PostMapping("/{courseId}/enroll-student/{studentId}")
    public ResponseEntity<?> enrollStudentInCourse(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        try {
            StudentCourse enrollment = courseService.enrollStudentInCourse(courseId, studentId);
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{courseId}/assign-teacher/{teacherId}")
    public ResponseEntity<?> assignTeacherToCourse(
            @PathVariable Long courseId,
            @PathVariable Long teacherId) {
        try {
            Course updatedCourse = courseService.assignTeacherToCourse(courseId, teacherId);
            return ResponseEntity.ok(updatedCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{courseId}/remove-teacher")
    public ResponseEntity<?> removeTeacherFromCourse(@PathVariable Long courseId) {
        try {
            Course updatedCourse = courseService.removeTeacherFromCourse(courseId);
            return ResponseEntity.ok(updatedCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/cleanup/student/{studentId}")
    public ResponseEntity<?> cleanupStudentEnrollments(@PathVariable Long studentId) {
        try {
            courseService.handleStudentDeletion(studentId);
            return ResponseEntity.ok("Student enrollments cleaned up successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/cleanup/teacher/{teacherId}")
    public ResponseEntity<?> cleanupTeacherCourses(@PathVariable Long teacherId) {
        try {
            courseService.handleTeacherDeletion(teacherId);
            return ResponseEntity.ok("Teacher courses cleaned up successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{courseId}/teacher-details")
    public ResponseEntity<?> getCourseTeacherDetails(@PathVariable Long courseId) {
        try {
            Object teacherDetails = courseService.getCourseTeacherDetails(courseId);
            return ResponseEntity.ok(teacherDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{courseId}/student-details")
    public ResponseEntity<?> getCourseStudentDetails(@PathVariable Long courseId) {
        try {
            List<Object> studentDetails = courseService.getCourseStudentDetails(courseId);
            return ResponseEntity.ok(studentDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{courseId}/validate-teacher/{teacherId}")
    public ResponseEntity<?> validateTeacherAssignment(
            @PathVariable Long courseId,
            @PathVariable Long teacherId) {
        try {
            courseService.validateTeacherExists(teacherId);
            courseService.validateCourseExists(courseId);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Teacher can be assigned to course"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", e.getMessage()
            ));
        }
    }
    @DeleteMapping("/{courseId}/unenroll-student/{studentId}")
    public ResponseEntity<?> unenrollStudent(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        try {
            courseService.unenrollStudent(courseId, studentId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Student successfully unenrolled from course"
            ));
        } catch (RuntimeException e) {
            log.error("Unenrollment error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{courseId}/unenroll-student-confirm/{studentId}")
    public ResponseEntity<?> unenrollStudentWithConfirmation(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        try {
            boolean success = courseService.unenrollStudentWithConfirmation(courseId, studentId);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Student successfully unenrolled from course"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Student was not enrolled in this course"
                ));
            }
        } catch (RuntimeException e) {
            log.error("Unenrollment error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
   @DeleteMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<?> unenrollStudentById(@PathVariable Long enrollmentId) {
        try {
            courseService.unenrollStudentById(enrollmentId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Enrollment successfully deleted"
            ));
        } catch (RuntimeException e) {
            log.error("Enrollment deletion error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @PostMapping("/{courseId}/bulk-unenroll")
    public ResponseEntity<?> bulkUnenrollStudents(
            @PathVariable Long courseId,
            @RequestBody List<Long> studentIds) {
        try {
            int unenrolledCount = courseService.unenrollMultipleStudents(courseId, studentIds);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Bulk unenrollment completed",
                    "unenrolledCount", unenrolledCount,
                    "totalRequested", studentIds.size()
            ));
        } catch (RuntimeException e) {
            log.error("Bulk unenrollment error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @GetMapping("/{courseId}/enrollment-status/{studentId}")
    public ResponseEntity<?> getEnrollmentStatus(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        try {
            boolean isEnrolled = studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId);

            return ResponseEntity.ok(Map.of(
                    "courseId", courseId,
                    "studentId", studentId,
                    "isEnrolled", isEnrolled
            ));
        } catch (Exception e) {
            log.error("Enrollment status check error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to check enrollment status"
            ));
        }
    }
}