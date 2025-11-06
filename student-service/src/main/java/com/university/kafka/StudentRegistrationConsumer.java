package com.university.kafka;
import com.university.Service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Slf4j
@Component
public class StudentRegistrationConsumer {

    @Autowired
    private StudentService studentService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "student-registration-topic", groupId = "university-group")
    public void consumeStudentRegistration(String message) {
        log.info(" KAFKA LISTENER TRIGGERED WITH STRING");
        log.info("Received raw message: {}", message);

        try {
            Map<String, Object> studentData = objectMapper.readValue(message, Map.class);

            log.info("Successfully parsed student data: {}", studentData);
            log.info("User ID: {}", studentData.get("userId"));
            log.info("Name: {}", studentData.get("name"));
            log.info("Roll Number: {}", studentData.get("rollNumber"));

            Object result = studentService.createStudent(studentData);
            log.info("Student created successfully: {}", result);

        } catch (Exception e) {
            log.error("Error processing student registration", e);
            e.printStackTrace();
        }
    }
}