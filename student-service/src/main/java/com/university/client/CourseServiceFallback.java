package com.university.client;
import com.university.DTO.CourseServiceResponseDTO;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class CourseServiceFallback implements CourseServiceClient {
    @Override
    public List<CourseServiceResponseDTO> getCoursesByStudentId(Long studentId) {
        CourseServiceResponseDTO fallbackCourse = new CourseServiceResponseDTO();
        fallbackCourse.setId(0L);
        fallbackCourse.setCourseName("Course Service Unavailable");
        fallbackCourse.setCourseCode("SVC-DOWN");
        fallbackCourse.setDepartment("Technical Support");
        fallbackCourse.setSemester("N/A");
        fallbackCourse.setCredits(0);
        fallbackCourse.setTeacherId(0L);

        return Arrays.asList(fallbackCourse);
    }

    @Override
    public CourseServiceResponseDTO getCourseById(Long studentId) {
        CourseServiceResponseDTO fallbackCourse = new CourseServiceResponseDTO();
        fallbackCourse.setId(0L);
        fallbackCourse.setCourseName("Course Service Unavailable");
        fallbackCourse.setCourseCode("SVC-DOWN");
        fallbackCourse.setDepartment("Technical Support");
        fallbackCourse.setSemester("N/A");
        fallbackCourse.setCredits(0);
        fallbackCourse.setTeacherId(0L);

        return (CourseServiceResponseDTO) Arrays.asList(fallbackCourse);
    }
    @Override
    public List<CourseServiceResponseDTO> getCoursesByTeacherId(Long studentId) {
        CourseServiceResponseDTO fallbackCourse = new CourseServiceResponseDTO();
        fallbackCourse.setId(0L);
        fallbackCourse.setCourseName("Course Service Unavailable");
        fallbackCourse.setCourseCode("SVC-DOWN");
        fallbackCourse.setDepartment("Technical Support");
        fallbackCourse.setSemester("N/A");
        fallbackCourse.setCredits(0);
        fallbackCourse.setTeacherId(0L);

        return Arrays.asList(fallbackCourse);
    }
}