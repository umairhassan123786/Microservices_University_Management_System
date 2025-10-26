package com.university.Entities;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "STUDENTS")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_seq")
    @SequenceGenerator(name = "student_seq", sequenceName = "STUDENT_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "USER_ID", unique = true)
    private Long userId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;

    @Column(name = "ROLL_NUMBER", unique = true, nullable = false)
    private String rollNumber;

    @Column(name = "SEMESTER")
    private String semester;

    @Column(name = "DEPARTMENT")
    private String department;

    @Column(name = "ACTIVE")
    private Boolean active = true;
}