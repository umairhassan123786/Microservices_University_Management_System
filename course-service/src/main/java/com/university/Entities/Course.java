package com.university.Entities;

import javax.persistence.*;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String courseName;

    private String department;
    private String semester;
    private Integer credits;

    // ✅ Teacher ID (nullable foreign key)
    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;

    // ✅ Constructors
    public Course() {}

    public Course(String courseName, String department, String semester, Integer credits) {
        this.courseName = courseName;
        this.department = department;
        this.semester = semester;
        this.credits = credits;
        this.teacherId = null; // ✅ Initially null
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}