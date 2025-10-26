package com.university.Entities;

import javax.persistence.*;

@Entity
@Table(name = "student_courses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
public class StudentCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    private String semester;
    private String grade;

    // ✅ Constructors
    public StudentCourse() {}

    public StudentCourse(Long studentId, Long courseId, String semester) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.semester = semester;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}