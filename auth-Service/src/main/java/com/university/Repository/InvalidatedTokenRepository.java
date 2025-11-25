package com.university.Repository;
import com.university.Entities.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
import java.util.List;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {
    boolean existsByToken(String token);
    List<InvalidatedToken> findByExpiryDateBefore(Date currentDate);
}