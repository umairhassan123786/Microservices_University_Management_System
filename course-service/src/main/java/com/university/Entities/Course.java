package com.university.Entities;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    @Column(name = "course_code", nullable = true, unique = true)
    private String courseCode;
    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;
}