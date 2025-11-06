package com.university.Service;

import com.university.Entities.Student;
import com.university.Entities.StudentES;
import com.university.Repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DataMigration {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateData() {
        System.out.println("Checking Elasticsearch migration...");

        if (elasticsearchService == null) {
            System.out.println("Elasticsearch service not available - skipping migration");
            return;
        }

        try {
            System.out.println("Migrating existing students to Elasticsearch...");
            int successCount = 0;

            for (Student student : studentRepository.findAll()) {
                try {
                    // ✅ Create proper StudentES object
                    StudentES studentES = new StudentES(
                            student.getId().toString(),  // String ID
                            student.getId(),             // studentId (Long)
                            student.getUserId(),         // userId (Long)
                            student.getName(),
                            student.getEmail(),
                            student.getRollNumber(),
                            student.getDepartment(),
                            student.getSemester(),
                            student.getActive()
                    );

                    elasticsearchService.syncStudent(studentES);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("Failed to migrate student " + student.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("Data migration completed: " + successCount + " students migrated");
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
        }
    }
}