package com.university.Entities;

import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "USERS")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "USER_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "USERNAME", unique = true, nullable = false)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;

    @Column(name = "ROLE", nullable = false)
    private String role; // STUDENT, TEACHER, ADMIN

    // ✅ FIXED: Oracle compatible - NUMBER(1) for boolean
    @Column(name = "ACTIVE", columnDefinition = "NUMBER(1) DEFAULT 1")
    private Integer active = 1; // 1 = true, 0 = false

    // ✅ Helper methods for boolean operations
    public Boolean isActive() {
        return active == 1;
    }

    public void setActive(Boolean active) {
        this.active = active ? 1 : 0;
    }
}