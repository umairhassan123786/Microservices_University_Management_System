package com.university.kafka;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class UserRegistrationProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendStudentRegistrationEvent(Map<String, Object> studentData) {
        try {
            kafkaTemplate.send("student-registration-topic", studentData)
                    .addCallback(
                            result -> log.info("Student event sent successfully: {}", result),
                            error -> log.error("Failed to send student event: {}", error.getMessage())
                    );
        } catch (Exception e) {
        }
    }

    public void sendTeacherRegistrationEvent(Map<String, Object> teacherData) {
        try {

            kafkaTemplate.send("teacher-registration-topic", teacherData)
                    .addCallback(
                            result -> log.info("Teacher event sent successfully: {}", result),
                            error -> log.error("Failed to send teacher event: {}", error.getMessage())
                    );
        } catch (Exception e) {
        }
    }
}