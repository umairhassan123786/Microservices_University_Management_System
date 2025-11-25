package com.university.Repository;
import com.university.Entities.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    Optional<Privilege> findByUserId(Long userId);
    Optional<Privilege> findByUserIdAndActive(Long userId, Integer active);

    @Query("SELECT p FROM Privilege p WHERE p.userId = :userId AND p.active = 1")
    Optional<Privilege> findActivePrivilegeByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndActive(Long userId, Integer active);
}