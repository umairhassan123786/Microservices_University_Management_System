package com.university.Service;

import com.university.DTO.MarkAttendanceRequest;
import com.university.Entities.Attendance;
import com.university.Entities.Teacher;
import com.university.Enum.AttendanceStatus;
import com.university.Repository.TeacherRepository;
import com.university.client.AttendanceServiceClient;
import com.university.client.CourseServiceClient;
import com.university.client.StudentServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseServiceClient courseServiceClient;

    @Autowired
    private StudentServiceClient studentServiceClient;

    @Autowired
    private AttendanceServiceClient attendanceServiceClient;

    // ✅ BASIC CRUD METHODS
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }

    public Teacher createTeacher(Map<String, Object> teacherData) {
        try {
            Long userId = Long.valueOf(teacherData.get("userId").toString());
            if (teacherRepository.findByUserId(userId).isPresent()) {
                throw new RuntimeException("Teacher profile already exists for this user");
            }

            String teacherId = teacherData.get("teacherId").toString();
            if (teacherRepository.findByTeacherId(teacherId).isPresent()) {
                throw new RuntimeException("Teacher ID already exists: " + teacherId);
            }

            Teacher teacher = new Teacher();
            teacher.setUserId(userId);
            teacher.setName(teacherData.get("name").toString());
            teacher.setEmail(teacherData.get("email").toString());
            teacher.setDepartment(teacherData.get("department").toString());
            teacher.setQualification(teacherData.get("qualification").toString());
            teacher.setTeacherId(teacherId);

            return teacherRepository.save(teacher);

        } catch (Exception e) {
            throw new RuntimeException("Error creating teacher: " + e.getMessage());
        }
    }

    public Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher not found for user ID: " + userId));
    }

    public Teacher updateTeacher(Long id, Teacher teacherDetails) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        teacher.setName(teacherDetails.getName());
        teacher.setEmail(teacherDetails.getEmail());
        teacher.setDepartment(teacherDetails.getDepartment());
        teacher.setQualification(teacherDetails.getQualification());

        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(Long id) {
        teacherRepository.deleteById(id);
    }

    public List<Map<String, Object>> getTeacherCourses(Long teacherId) {
        try {
            return courseServiceClient.getCoursesByTeacherId(teacherId);
        } catch (Exception e) {
            System.out.println("⚠️ Course service down - Returning empty courses list");
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getStudentsInCourse(Long teacherId, Long courseId) {
        try {
            // Verify this course belongs to the teacher
            List<Map<String, Object>> teacherCourses = getTeacherCourses(teacherId);
            boolean courseBelongsToTeacher = teacherCourses.stream()
                    .anyMatch(course -> courseId.equals(Long.valueOf(course.get("id").toString())));

            if (!courseBelongsToTeacher) {
                throw new RuntimeException("Course does not belong to this teacher");
            }

            return courseServiceClient.getCourseStudentDetails(courseId);
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch course students: " + e.getMessage());
        }
    }

    public Map<String, Object> markAttendanceByTeacher(Long teacherId, Long courseId, MarkAttendanceRequest request) {
        try {
            Long studentId = request.getStudentId();
            LocalDate date = request.getDate();
            String status = request.getStatus();

            // Verify teacher has access to this course
            List<Map<String, Object>> teacherCourses = getTeacherCourses(teacherId);
            boolean hasAccess = teacherCourses.stream()
                    .anyMatch(course -> {
                        Object courseIdObj = course.get("id");
                        if (courseIdObj instanceof Integer) {
                            return courseId.equals(((Integer) courseIdObj).longValue());
                        } else if (courseIdObj instanceof Long) {
                            return courseId.equals(courseIdObj);
                        }
                        return false;
                    });

            if (!hasAccess) {
                throw new RuntimeException("Teacher does not have access to this course");
            }

            // Boolean studentExists = studentServiceClient.existsById(studentId);
            // if (studentExists == null || !studentExists) {
            //     throw new RuntimeException("Student not found with ID: " + studentId);
            // }
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setCourseId(courseId);
            attendance.setDate(date);

            // Convert status string to enum
            try {
                AttendanceStatus attendanceStatus = AttendanceStatus.valueOf(status.toUpperCase());
                attendance.setStatus(attendanceStatus);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid attendance status: " + status);
            }

            attendance.setRemarks("Marked by teacher ID: " + teacherId);
            attendance.setSemester("Spring 2024");

            // Call attendance service
            Attendance savedAttendance = attendanceServiceClient.markAttendance(attendance);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", savedAttendance.getId());
            response.put("studentId", savedAttendance.getStudentId());
            response.put("courseId", savedAttendance.getCourseId());
            response.put("date", savedAttendance.getDate().toString());
            response.put("status", savedAttendance.getStatus());
            response.put("message", "Attendance marked successfully");

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to mark attendance: " + e.getMessage());
            return errorResponse;
        }
    }
    public Map<String, Object> bulkMarkAttendance(Long teacherId, Map<String, Object> request) {
        try {
            Long courseId = Long.valueOf(request.get("courseId").toString());
            String dateStr = request.get("date").toString();
            LocalDate date = LocalDate.parse(dateStr);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> students = (List<Map<String, Object>>) request.get("students");

            List<Map<String, Object>> teacherCourses = getTeacherCourses(teacherId);
            boolean hasAccess = teacherCourses.stream()
                    .anyMatch(course -> {
                        Object courseIdObj = course.get("id");
                        if (courseIdObj instanceof Integer) {
                            return courseId.equals(((Integer) courseIdObj).longValue());
                        } else if (courseIdObj instanceof Long) {
                            return courseId.equals(courseIdObj);
                        }
                        return false;
                    });

            if (!hasAccess) {
                throw new RuntimeException("Teacher does not have access to this course");
            }

            List<Attendance> attendanceList = new ArrayList<>();
            for (Map<String, Object> student : students) {
                Long studentId = Long.valueOf(student.get("studentId").toString());
                String status = student.get("status").toString();

                Attendance attendance = new Attendance();
                attendance.setStudentId(studentId);
                attendance.setCourseId(courseId);
                attendance.setDate(date);

                try {
                    AttendanceStatus attendanceStatus = AttendanceStatus.valueOf(status.toUpperCase());
                    attendance.setStatus(attendanceStatus);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid attendance status: " + status);
                }

                attendance.setRemarks("Bulk marked by teacher: " + teacherId);
                attendance.setSemester("Spring 2024");
                attendanceList.add(attendance);
            }

            // Call bulk attendance service
            List<Attendance> savedAttendances = attendanceServiceClient.bulkMarkAttendance(attendanceList);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bulk attendance marked successfully");
            response.put("count", savedAttendances.size());
            response.put("attendances", savedAttendances);

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Bulk attendance marking failed: " + e.getMessage());
            return errorResponse;
        }
    }    public Map<String, Object> getTeacherCourseAttendance(Long teacherId, Long courseId) {
        try {
            // Verify teacher access
            List<Map<String, Object>> teacherCourses = getTeacherCourses(teacherId);
            boolean hasAccess = teacherCourses.stream()
                    .anyMatch(course -> {
                        Object courseIdObj = course.get("id");
                        if (courseIdObj instanceof Integer) {
                            return courseId.equals(((Integer) courseIdObj).longValue());
                        } else if (courseIdObj instanceof Long) {
                            return courseId.equals(courseIdObj);
                        }
                        return false;
                    });

            if (!hasAccess) {
                throw new RuntimeException("Teacher does not have access to this course");
            }

            // Get attendance from attendance service
            List<Attendance> attendance = attendanceServiceClient.getCourseAttendance(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseId", courseId);
            response.put("attendanceCount", attendance.size());
            response.put("attendance", attendance);

            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Unable to fetch course attendance: " + e.getMessage());
            return errorResponse;
        }
    }
    // TeacherService mein yeh method add karen
    public Boolean existsById(Long teacherId) {
        try {
            return teacherRepository.existsById(teacherId);
        } catch (Exception e) {
            System.out.println(" Error checking teacher existence: " + e.getMessage());
            return false;
        }
    }
    public List<Attendance> getCourseAttendanceByDate(Long teacherId, Long courseId, LocalDate date) {
        try {
            // Verify teacher access
            List<Map<String, Object>> teacherCourses = getTeacherCourses(teacherId);
            boolean hasAccess = teacherCourses.stream()
                    .anyMatch(course -> {
                        Object courseIdObj = course.get("id");
                        if (courseIdObj instanceof Integer) {
                            return courseId.equals(((Integer) courseIdObj).longValue());
                        } else if (courseIdObj instanceof Long) {
                            return courseId.equals(courseIdObj);
                        }
                        return false;
                    });

            if (!hasAccess) {
                throw new RuntimeException("Teacher does not have access to this course");
            }

            // Get attendance by date
            return attendanceServiceClient.getAttendanceByDateAndCourse(courseId, date.toString());
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch attendance by date: " + e.getMessage());
        }
    }
}