package com.university.Entities;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "teachers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String teacherId;

    private String name;
    private String email;
    private String department;
    private String qualification;
    @Column(name = "USER_ID", unique = true)
    private Long userId;
}