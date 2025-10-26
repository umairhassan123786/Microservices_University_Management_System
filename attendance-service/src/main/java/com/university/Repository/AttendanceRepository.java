package com.university.Repository;
import com.university.Entities.Attendance;
import com.university.Enum.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Existing methods
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByCourseId(Long courseId);
    List<Attendance> findByCourseIdAndDate(Long courseId, LocalDate date);
    List<Attendance> findByStudentIdAndSemester(Long studentId, String semester);

    // New methods
    List<Attendance> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Attendance> findByStudentIdAndDateBetween(Long studentId, LocalDate startDate, LocalDate endDate);

    List<Attendance> findByCourseIdAndDateBetween(Long courseId, LocalDate startDate, LocalDate endDate);

    List<Attendance> findByStudentIdAndCourseIdAndDateBetween(
            Long studentId, Long courseId, LocalDate startDate, LocalDate endDate);

    Long countByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, AttendanceStatus status);

    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId AND a.courseId = :courseId AND a.semester = :semester")
    List<Attendance> findStudentCourseSemesterAttendance(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("semester") String semester);

    boolean existsByStudentIdAndCourseIdAndDate(Long studentId, Long courseId, LocalDate date);


}