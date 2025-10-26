package com.university.Service;

import com.university.Entities.Course;
import com.university.Entities.StudentCourse;
import com.university.Repository.CourseRepository;
import com.university.Repository.StudentCourseRepository;
import com.university.client.StudentServiceClient;
import com.university.client.TeacherServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final TeacherServiceClient teacherServiceClient;
    private final StudentServiceClient studentServiceClient;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public Course createCourse(Course course) {
        if (course.getTeacherId() != null) {
            validateTeacherExists(course.getTeacherId());
        }

        if (course.getTeacherId() != null && course.getTeacherId() == 0) {
            course.setTeacherId(null);
        }
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course courseDetails) {
        Course course = getCourseById(id);
        course.setCourseName(courseDetails.getCourseName());
        course.setDepartment(courseDetails.getDepartment());
        course.setSemester(courseDetails.getSemester());
        course.setCredits(courseDetails.getCredits());
        if (courseDetails.getTeacherId() != null) {
            validateTeacherExists(courseDetails.getTeacherId());
            course.setTeacherId(courseDetails.getTeacherId());
        }
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        studentCourseRepository.deleteByCourseId(id);
        courseRepository.deleteById(id);
    }

    public List<Course> getCoursesByStudentId(Long studentId) {
        return studentCourseRepository.findCoursesByStudentId(studentId);
    }

    public List<Course> getCoursesByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public StudentCourse enrollStudent(StudentCourse enrollment) {
        // ✅ Validate student exists before enrollment
        validateStudentExists(enrollment.getStudentId());
        validateCourseExists(enrollment.getCourseId());

        return studentCourseRepository.save(enrollment);
    }

    public StudentCourse enrollStudentInCourse(Long courseId, Long studentId) {
        validateCourseExists(courseId);
        validateStudentExists(studentId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        boolean alreadyEnrolled = studentCourseRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .stream().findFirst().isPresent();

        if (alreadyEnrolled) {
            throw new RuntimeException("Student already enrolled in this course");
        }

        StudentCourse enrollment = new StudentCourse();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setSemester("Spring 2024");

        return studentCourseRepository.save(enrollment);
    }

    public Course assignTeacherToCourse(Long courseId, Long teacherId) {
        validateCourseExists(courseId);
        validateTeacherExists(teacherId);

        Course course = getCourseById(courseId);
        course.setTeacherId(teacherId);
        return courseRepository.save(course);
    }

    public Course removeTeacherFromCourse(Long courseId) {
        Course course = getCourseById(courseId);
        course.setTeacherId(null);
        return courseRepository.save(course);
    }

    @Transactional
    public void unenrollStudent(Long courseId, Long studentId) {
        studentCourseRepository.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    @Transactional
    public void handleStudentDeletion(Long studentId) {
        studentCourseRepository.deleteByStudentId(studentId);
        System.out.println("Removed all enrollments for student ID: " + studentId);
    }

    @Transactional
    public void handleTeacherDeletion(Long teacherId) {
        List<Course> teacherCourses = courseRepository.findByTeacherId(teacherId);
        for (Course course : teacherCourses) {
            course.setTeacherId(null);
        }
        courseRepository.saveAll(teacherCourses);
        System.out.println("Removed teacher from courses for teacher ID: " + teacherId);
    }

    public List<StudentCourse> enrollMultipleStudents(Long courseId, List<Long> studentIds) {
        for (Long studentId : studentIds) {
            validateStudentExists(studentId);
        }

        return studentIds.stream()
                .map(studentId -> enrollStudentInCourse(courseId, studentId))
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesWithoutTeacher() {
        return courseRepository.findByTeacherIdIsNull();
    }

    public void validateTeacherExists(Long Id) {
        try {
            Boolean teacherExists = teacherServiceClient.existsById(Id);
            if (teacherExists == null || !teacherExists) {
                throw new RuntimeException("Teacher with ID " + Id + " does not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to verify teacher existence: " + e.getMessage());
        }
    }

    private void validateStudentExists(Long studentId) {
        try {
            Boolean studentExists = studentServiceClient.existsById(studentId);
            if (studentExists == null || !studentExists) {
                throw new RuntimeException("Student with ID " + studentId + " does not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to verify student existence: " + e.getMessage());
        }
    }

    public void validateCourseExists(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course with ID " + courseId + " does not exist");
        }
    }

    public Object getCourseTeacherDetails(Long courseId) {
        Course course = getCourseById(courseId);
        if (course.getTeacherId() == null) {
            return Map.of("message", "No teacher assigned to this course");
        }

        try {
            return teacherServiceClient.getTeacherById(course.getTeacherId());
        } catch (Exception e) {
            return Map.of("error", "Unable to fetch teacher details: " + e.getMessage());
        }
    }

    public List<Object> getCourseStudentDetails(Long courseId) {
        List<StudentCourse> enrollments = studentCourseRepository.findByCourseId(courseId);

        return enrollments.stream()
                .map(enrollment -> {
                    try {
                        return studentServiceClient.getStudentById(enrollment.getStudentId());
                    } catch (Exception e) {
                        return Map.of(
                                "studentId", enrollment.getStudentId(),
                                "error", "Unable to fetch student details"
                        );
                    }
                })
                .collect(Collectors.toList());
    }
    // ✅ CourseService mein ye methods add karein
    @Transactional
    public boolean unenrollStudentWithConfirmation(Long courseId, Long studentId) {
        try {

            List<StudentCourse> enrollments = studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId);

            if (enrollments.isEmpty()) {
                return false;
            }

            // ✅ Delete the enrollment
            studentCourseRepository.deleteByStudentIdAndCourseId(studentId, courseId);
              return true;

        } catch (Exception e) {
            throw new RuntimeException("Failed to unenroll student: " + e.getMessage());
        }
    }

    @Transactional
    public void unenrollStudentById(Long enrollmentId) {
        try {
            if (!studentCourseRepository.existsById(enrollmentId)) {
                throw new RuntimeException("Enrollment not found with ID: " + enrollmentId);
            }

            studentCourseRepository.deleteById(enrollmentId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete enrollment: " + e.getMessage());
        }
    }

    @Transactional
    public int unenrollMultipleStudents(Long courseId, List<Long> studentIds) {
        try {

            int totalUnenrolled = 0;
            for (Long studentId : studentIds) {
                try {
                    studentCourseRepository.deleteByStudentIdAndCourseId(studentId, courseId);
                    totalUnenrolled++;
                } catch (Exception e) {
                   // log.warn("Failed to unenroll student {} from course {}: {}", studentId, courseId, e.getMessage());
                }
            }

        //    log.info("Successfully unenrolled {} students from course {}", totalUnenrolled, courseId);
            return totalUnenrolled;

        } catch (Exception e) {
         //   log.error("Error in bulk unenrollment for course {}: {}", courseId, e.getMessage());
            throw new RuntimeException("Bulk unenrollment failed: " + e.getMessage());
        }
    }
}