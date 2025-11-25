package com.university.Entities;
import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "USER_PRIVILEGES",
        uniqueConstraints = @UniqueConstraint(columnNames = {"USER_ID"}))
@Data
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "privilege_seq")
    @SequenceGenerator(name = "privilege_seq", sequenceName = "PRIVILEGE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "USER_ID", nullable = false, unique = true)
    private Long userId;

    @Column(name = "PRIVILEGES_JSON", nullable = false, length = 4000)
    private String privilegesJson;

    @Column(name = "ACTIVE", columnDefinition = "NUMBER(1) DEFAULT 1")
    private Integer active = 1;

    public Boolean isActive() {
        return active == 1;
    }

    public void setActive(Boolean active) {
        this.active = active ? 1 : 0;
    }
}