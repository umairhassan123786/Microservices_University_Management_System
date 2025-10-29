package com.university.Entities;
import lombok.*;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "invalidated_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class InvalidatedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, length = 1000)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;

    @Column(name = "invalidated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date invalidatedAt = new Date();

    public InvalidatedToken(String token, Date expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.invalidatedAt = new Date();
    }
}