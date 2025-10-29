package com.university.client;
import com.university.DTO.TeacherDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "teacher-service",
        path = "/api/teachers",
        fallback = TeacherServiceFallback.class
)
public interface TeacherServiceClient {

    @GetMapping("/{teacherId}")
    TeacherDTO getTeacherById(@PathVariable("teacherId") Long teacherId);

    @GetMapping("/course/{courseId}")
    TeacherDTO getTeacherByCourseId(@PathVariable("courseId") Long courseId);
}
