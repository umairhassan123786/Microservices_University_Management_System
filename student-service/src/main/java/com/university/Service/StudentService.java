package com.university.Service;
import com.university.DTO.*;
import com.university.Entities.Student;
import com.university.Repository.StudentRepository;
import com.university.client.AttendanceServiceClient;
import com.university.client.CourseServiceClient;
import com.university.client.TeacherServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseServiceClient courseServiceClient;

    @Autowired
    private AttendanceServiceClient attendanceServiceClient;

    @Autowired
    private TeacherServiceClient teacherServiceClient;

    public List<CourseDTO> getStudentCourses(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        try {
            List<CourseServiceResponseDTO> courseResponses = courseServiceClient.getCoursesByStudentId(studentId);

            if (courseResponses == null || courseResponses.isEmpty()) {
                return Collections.emptyList();
            }

            // Convert CourseServiceResponseDTO to CourseDTO
            List<CourseDTO> courses = courseResponses.stream()
                    .map(response -> {
                        CourseDTO course = new CourseDTO();
                        course.setId(response.getId());
                        course.setName(response.getCourseName());  // Map karo
                        course.setCode(response.getCourseCode());  // Map karo
                        course.setDepartment(response.getDepartment());
                        course.setSemester(response.getSemester());
                        course.setCredits(response.getCredits());
                        course.setTeacherId(response.getTeacherId());
                        return course;
                    })
                    .collect(Collectors.toList());

            for (CourseDTO course : courses) {
                if (course.getTeacherId() != null) {
                    try {
                        TeacherDTO teacher = teacherServiceClient.getTeacherById(course.getTeacherId());
                        course.setTeacherName(teacher != null ? teacher.getName() : "Teacher not found");
                    } catch (Exception e) {
                        course.setTeacherName("Teacher information unavailable");
                    }
                }
            }

            return courses;

        } catch (Exception e) {
            System.err.println("Error fetching courses for student " + studentId + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }    public List<AttendanceDTO> getStudentAttendance(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return attendanceServiceClient.getStudentAttendance(studentId);
    }

    public List<AttendanceDTO> getCourseAttendance(Long studentId, Long courseId) {
        if (!isStudentEnrolledInCourse(studentId, courseId)) {
            throw new RuntimeException("Student is not enrolled in this course");
        }

        return attendanceServiceClient.getCourseAttendance(studentId, courseId);
    }

    public StudentProfileDTO getStudentProfile(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<CourseDTO> courses = getStudentCourses(studentId);

        List<AttendanceDTO> attendance = getStudentAttendance(studentId);

        double attendancePercentage = calculateAttendancePercentage(attendance);

        return new StudentProfileDTO(student, courses, attendance, attendancePercentage);
    }

    private boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        try {
            List<CourseServiceResponseDTO> studentCourses = courseServiceClient.getCoursesByStudentId(studentId);
            return studentCourses.stream()
                    .anyMatch(course -> course.getId().equals(courseId));
        } catch (Exception e) {
            return false;
        }
    }

    private double calculateAttendancePercentage(List<AttendanceDTO> attendance) {
        if (attendance == null || attendance.isEmpty()) {
            return 0.0;
        }

        long presentCount = attendance.stream()
                .filter(a -> "PRESENT".equals(a.getStatus()))
                .count();

        return (double) presentCount / attendance.size() * 100;
    }
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId);
    }

    public Object createStudent(Map<String, Object> studentData) {
        try {
            if (!studentData.containsKey("userId")) {
                throw new RuntimeException("User ID is required");
            }
            if (!studentData.containsKey("rollNumber")) {
                throw new RuntimeException("Roll number is required");
            }

            Long userId = Long.valueOf(studentData.get("userId").toString());
            String rollNumber = studentData.get("rollNumber").toString();

            if (studentRepository.findByUserId(userId).isPresent()) {
                throw new RuntimeException("Student profile already exists for this user");
            }
            if (studentRepository.existsByRollNumber(rollNumber)) {
                throw new RuntimeException("Roll number '" + rollNumber + "' already exists");
            }

            Student student = new Student();
            student.setUserId(userId);
            student.setName(studentData.get("name").toString());
            student.setEmail(studentData.get("email").toString());
            student.setRollNumber(rollNumber);
            student.setSemester(studentData.get("semester").toString());
            student.setDepartment(studentData.get("department").toString());
            student.setActive(true);

            Student savedStudent = studentRepository.save(student);
            System.out.println("Student created successfully: " + savedStudent.getId());

            return Map.of(
                    "id", savedStudent.getId(),
                    "rollNumber", savedStudent.getRollNumber(),
                    "name", savedStudent.getName(),
                    "department", savedStudent.getDepartment(),
                    "status", "SUCCESS"
            );

        } catch (Exception e) {
            System.out.println("Error in createStudent: " + e.getMessage());
            throw new RuntimeException("Failed to create student: " + e.getMessage());
        }
    }

    public boolean existsByRollNumber(String rollNumber) {
        return studentRepository.existsByRollNumber(rollNumber);
    }
    public Student updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setDepartment(studentDetails.getDepartment());
        student.setSemester(studentDetails.getSemester());

        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}