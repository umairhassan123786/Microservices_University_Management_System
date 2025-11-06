package com.university.Service;
import com.university.Entities.StudentES;
import com.university.Repository.StudentESRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ElasticsearchService {

    @Autowired(required = false)
    private StudentESRepository studentESRepository;

    public StudentES syncStudent(StudentES studentES) {
        if (studentESRepository == null) {
            System.out.println("Elasticsearch repository not available - skipping sync");
            return studentES;
        }
        try {
            return studentESRepository.save(studentES);
        } catch (Exception e) {
            System.err.println("Error syncing student to Elasticsearch: " + e.getMessage());
            return studentES;
        }
    }

    public Optional<StudentES> findByStudentId(Long studentId) {
        if (studentESRepository == null) {
            System.out.println("Elasticsearch repository not available");
            return Optional.empty();
        }
        try {
            return studentESRepository.findByStudentId(studentId);
        } catch (Exception e) {
            System.err.println("Error finding student in Elasticsearch: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<StudentES> searchByName(String name) {
        if (studentESRepository == null) {
            System.out.println("Elasticsearch repository not available");
            return List.of();
        }
        try {
            return studentESRepository.findByNameContainingIgnoreCase(name);
        } catch (Exception e) {
            System.err.println("Error searching students in Elasticsearch: " + e.getMessage());
            return List.of();
        }
    }

    public List<StudentES> findByDepartment(String department) {
        if (studentESRepository == null) {
            System.out.println("Elasticsearch repository not available");
            return List.of();
        }
        try {
            return studentESRepository.findByDepartment(department);
        } catch (Exception e) {
            System.err.println("Error finding students by department in Elasticsearch: " + e.getMessage());
            return List.of();
        }
    }

    public void deleteStudent(String id) {
        if (studentESRepository != null) {
            try {
                studentESRepository.deleteById(id);
            } catch (Exception e) {
                System.err.println("Error deleting student from Elasticsearch: " + e.getMessage());
            }
        }
    }
}