package com.university.Repository;
import com.university.Entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDepartment(String department);
    List<Course> findBySemester(String semester);
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByTeacherIdIsNull();
    boolean existsByCourseCode(String courseCode);

}

