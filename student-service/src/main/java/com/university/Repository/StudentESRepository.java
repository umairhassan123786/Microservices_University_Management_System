package com.university.Repository;
import com.university.Entities.StudentES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;
import java.util.Optional;

public interface StudentESRepository extends ElasticsearchRepository<StudentES, String> {

    Optional<StudentES> findByStudentId(Long studentId);

    Optional<StudentES> findByUserId(Long userId);

    List<StudentES> findByNameContainingIgnoreCase(String name);

    List<StudentES> findByDepartment(String department);

    Optional<StudentES> findByRollNumber(String rollNumber);

    List<StudentES> findByActiveTrue();
}