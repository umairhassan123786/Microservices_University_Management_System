package com.university.Entities;
import lombok.*;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "invalidated_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class InvalidatedToken {
    @Id
    private String token;

    @Column(nullable = false)
    private Date expiryDate;

    @Column(nullable = false)
    private Date invalidatedAt = new Date();
}