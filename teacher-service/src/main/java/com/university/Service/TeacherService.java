package com.university.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.DTO.CourseDTO;
import com.university.DTO.MarkAttendanceRequest;
import com.university.Entities.Attendance;
import com.university.Entities.Teacher;
import com.university.Enum.AttendanceStatus;
import com.university.Repository.TeacherRepository;
import com.university.client.AttendanceServiceClient;
import com.university.client.CourseServiceClient;
import com.university.client.StudentServiceClient;
import com.university.kafka.AttendanceProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseServiceClient courseServiceClient;

    @Autowired
    private StudentServiceClient studentServiceClient;
    @Autowired
    private AttendanceProducer attendanceProducer;
    @Autowired
    private AttendanceServiceClient attendanceServiceClient;
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
            System.out.println("Fetching courses for teacher: " + teacherId);

            // Get courses as List<CourseDTO> with proper mapping
            List<CourseDTO> courses = courseServiceClient.getCoursesByTeacherId(teacherId);

            System.out.println("Raw courses response: " + courses);

            if (courses == null || courses.isEmpty()) {
                System.out.println("No courses found for teacher: " + teacherId);
                throw new RuntimeException("No courses found for this teacher");
            }

            // Convert CourseDTO to Map
            List<Map<String, Object>> courseMaps = courses.stream()
                    .map(course -> {
                        System.out.println("Processing course: " + course);

                        Map<String, Object> courseMap = new HashMap<>();
                        courseMap.put("id", course.getId());
                        courseMap.put("name", course.getName());
                        courseMap.put("code", course.getCode());
                        courseMap.put("department", course.getDepartment());
                        courseMap.put("semester", course.getSemester());
                        courseMap.put("credits", course.getCredits());
                        courseMap.put("teacherId", course.getTeacherId());

                        System.out.println("Created course map: " + courseMap);
                        return courseMap;
                    })
                    .collect(Collectors.toList());

            System.out.println("Final course maps: " + courseMaps);
            return courseMaps;

        } catch (Exception e) {
            System.err.println("Error in getTeacherCourses: " + e.getMessage());
            throw new RuntimeException("Error fetching teacher courses: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getStudentsInCourse(Long teacherId, Long courseId) {
        try {
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

//    public Map<String, Object> markAttendanceByTeacher(Long teacherId, Long courseId, MarkAttendanceRequest request) {
//        try {
//            System.out.println("Marking attendance - Teacher: " + teacherId + ", Course: " + courseId + ", Student: " + request.getStudentId());
//
//            // Step 1: Validate teacher has access to this course
//            List<Map<String, Object>> teacherCourses = getTeacherCourses(teacherId);
//            System.out.println("Teacher courses: " + teacherCourses);
//
//            boolean hasAccess = teacherCourses.stream()
//                    .anyMatch(course -> {
//                        Object courseIdObj = course.get("id");
//                        System.out.println("Comparing course ID: " + courseIdObj + " with: " + courseId);
//                        return courseId != null && courseId.equals(courseIdObj);
//                    });
//
//            if (!hasAccess) {
//                throw new RuntimeException("Teacher with ID " + teacherId + " does not have access to course ID " + courseId);
//            }
//            System.out.println("Teacher course access validated");
//
//            // Step 2: Validate student is enrolled in the course
//            List<Map<String, Object>> enrolledStudents = getStudentsInCourse(teacherId, courseId);
//            System.out.println("Enrolled students: " + enrolledStudents);
//
//            boolean isEnrolled = enrolledStudents.stream()
//                    .anyMatch(student -> {
//                        if (student == null) {
//                            System.out.println("Found null student in enrolled list");
//                            return false;
//                        }
//                        Object studentIdObj = student.get("id");
//                        System.out.println("Comparing student ID: " + studentIdObj + " with: " + request.getStudentId());
//                        return studentIdObj != null && request.getStudentId().equals(Long.valueOf(studentIdObj.toString()));
//                    });
//
//            if (!isEnrolled) {
//                throw new RuntimeException("Student with ID " + request.getStudentId() + " is not enrolled in course ID " + courseId);
//            }
//            System.out.println("Student enrollment validated");
//
//            // Step 3: Create and save attendance
//            Attendance attendance = new Attendance();
//            attendance.setStudentId(request.getStudentId());
//            attendance.setCourseId(courseId);
//            attendance.setStatus(AttendanceStatus.valueOf(request.getStatus()));
//            attendance.setDate(request.getDate());
//            attendance.setRemarks(request.getRemarks());
//            attendance.setCreatedAt(LocalDate.now().atStartOfDay());
//            attendance.setUpdatedAt(LocalDate.now().atStartOfDay());
//
//            System.out.println("Sending attendance to service: " + attendance);
//
//            // Step 4: Call attendance service
//            Attendance savedAttendance = attendanceServiceClient.markAttendance(attendance);
//
//            System.out.println("Attendance saved: " + savedAttendance);
//
//            // Step 5: Return success response
//            Map<String, Object> result = new HashMap<>();
//            result.put("message", "Attendance marked successfully");
//            result.put("attendanceId", savedAttendance.getId());
//            result.put("studentId", savedAttendance.getStudentId());
//            result.put("courseId", savedAttendance.getCourseId());
//            result.put("status", savedAttendance.getStatus().toString());
//            result.put("date", savedAttendance.getDate());
//            result.put("remarks", savedAttendance.getRemarks());
//            result.put("teacherId", teacherId);
//
//            System.out.println("Returning result: " + result);
//            return result;
//
//        } catch (Exception e) {
//            System.err.println("Error in markAttendanceByTeacher: " + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException("Failed to mark attendance: " + e.getMessage());
//        }
//    }
//    public Map<String, Object> markAttendanceByTeacher(Long teacherId, Long courseId, MarkAttendanceRequest request) {
//        try {
//            System.out.println("Marking attendance - Teacher: " + teacherId + ", Course: " + courseId + ", Student: " + request.getStudentId());
//            List<Map<String, Object>> teacherCourses = getTeacherCourses(teacherId);
//            System.out.println("Teacher courses: " + teacherCourses);
//             boolean hasAccess = teacherCourses.stream()
//                    .anyMatch(course -> {
//                        Object courseIdObj = course.get("id");
//                     System.out.println("Comparing course ID: " + courseIdObj + " with: " + courseId);
//                     return courseIdObj != null && courseId.equals(courseIdObj);
//                    });
//         if (!hasAccess) {
//                throw new RuntimeException("Teacher with ID " + teacherId + " does not have access to course ID " + courseId);
//            }
//            List<Map<String, Object>> enrolledStudents = getStudentsInCourse(teacherId, courseId);
//             boolean isEnrolled = enrolledStudents.stream()
//                    .anyMatch(student -> {
//                        if (student == null) return false;
//                        Object studentIdObj = student.get("id");
//                     return studentIdObj != null && request.getStudentId().equals(Long.valueOf(studentIdObj.toString()));
//                 });
//
//            if (!isEnrolled) {
//                throw new RuntimeException("Student with ID " + request.getStudentId() + " is not enrolled in course ID " + courseId);
//            }
//            Map<String, Object> attendanceEvent = new HashMap<>();
//            attendanceEvent.put("studentId", request.getStudentId());
//            attendanceEvent.put("courseId", courseId);
//            attendanceEvent.put("status", request.getStatus());
//            attendanceEvent.put("date", request.getDate().toString());
//            attendanceEvent.put("remarks", request.getRemarks());
//            attendanceEvent.put("teacherId", teacherId);
//            attendanceProducer.sendAttendanceEvent(attendanceEvent);
//            Map<String, Object> result = new HashMap<>();
//            result.put("message", "Attendance event sent to Kafka successfully");
//            result.put("teacherId", teacherId);
//            result.put("studentId", request.getStudentId());
//            result.put("courseId", courseId);
//            result.put("status", request.getStatus());
//            result.put("date", request.getDate());
//            result.put("remarks", request.getRemarks());
//            return result;
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to mark attendance: " + e.getMessage());
//        }
//    }

    public Map<String, Object> markAttendanceByTeacher(Long teacherId, Long courseId, MarkAttendanceRequest request) {
        try {
            System.out.println("Marking attendance - Teacher: " + teacherId + ", Course: " + courseId + ", Student: " + request.getStudentId());

            List<Map<String, Object>> teacherCourses = getTeacherCourses(teacherId);
            System.out.println("Teacher courses: " + teacherCourses);

            boolean hasAccess = teacherCourses.stream()
                    .anyMatch(course -> {
                        Object courseIdObj = course.get("id");
                        System.out.println("Comparing course ID: " + courseIdObj + " with: " + courseId);
                        return courseIdObj != null && courseId.equals(courseIdObj);
                    });

            if (!hasAccess) {
                throw new RuntimeException("Teacher with ID " + teacherId + " does not have access to course ID " + courseId);
            }

            List<Map<String, Object>> enrolledStudents = getStudentsInCourse(teacherId, courseId);
            boolean isEnrolled = enrolledStudents.stream()
                    .anyMatch(student -> {
                        if (student == null) return false;
                        Object studentIdObj = student.get("id");
                        return studentIdObj != null && request.getStudentId().equals(Long.valueOf(studentIdObj.toString()));
                    });

            if (!isEnrolled) {
                throw new RuntimeException("Student with ID " + request.getStudentId() + " is not enrolled in course ID " + courseId);
            }

            // Create attendance data Map
            Map<String, Object> attendanceEvent = new HashMap<>();
            attendanceEvent.put("studentId", request.getStudentId());
            attendanceEvent.put("courseId", courseId);
            attendanceEvent.put("status", request.getStatus());
            attendanceEvent.put("date", request.getDate().toString());
            attendanceEvent.put("remarks", request.getRemarks());
            attendanceEvent.put("teacherId", teacherId);

            // Convert Map to JSON String
            ObjectMapper objectMapper = new ObjectMapper();
            String attendanceJson = objectMapper.writeValueAsString(attendanceEvent);
            System.out.println("Converted to JSON: " + attendanceJson);

            // Send JSON string to Kafka
            attendanceProducer.sendAttendanceEvent(attendanceJson);

            // Return response
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Attendance event sent to Kafka successfully");
            result.put("teacherId", teacherId);
            result.put("studentId", request.getStudentId());
            result.put("courseId", courseId);
            result.put("status", request.getStatus());
            result.put("date", request.getDate());
            result.put("remarks", request.getRemarks());
            result.put("jsonSent", attendanceJson); // Optional: for debugging

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to mark attendance: " + e.getMessage());
        }
    }
    public Map<String, Object> bulkMarkAttendance(Long teacherId, Map<String, Object> request) {
        try {
            Long courseId = Long.valueOf(request.get("courseId").toString());
            String dateStr = request.get("date").toString();
            LocalDate date = LocalDate.parse(dateStr);
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
    }
    public Map<String, Object> getTeacherCourseAttendance(Long teacherId, Long courseId) {
        try {
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

            return attendanceServiceClient.getAttendanceByDateAndCourse(courseId, date.toString());
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch attendance by date: " + e.getMessage());
        }
    }
}