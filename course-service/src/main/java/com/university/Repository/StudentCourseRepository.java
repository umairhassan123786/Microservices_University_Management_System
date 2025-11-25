package com.university.Repository;

import com.university.Entities.Course;
import com.university.Entities.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    List<StudentCourse> findByStudentId(Long studentId);

    List<StudentCourse> findByCourseId(Long courseId);

    List<StudentCourse> findByStudentIdAndSemester(Long studentId, String semester);

    List<StudentCourse> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

     @Query("SELECT c FROM Course c JOIN StudentCourse sc ON c.id = sc.courseId WHERE sc.studentId = :studentId")
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);

    void deleteByStudentId(Long studentId);

    void deleteByCourseId(Long courseId);

    @Modifying
    @Transactional
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
}