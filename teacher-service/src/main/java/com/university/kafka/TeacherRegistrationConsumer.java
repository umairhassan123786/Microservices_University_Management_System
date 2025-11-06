package com.university.kafka;
import com.university.Service.TeacherService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class TeacherRegistrationConsumer {

    @Autowired
    private TeacherService teacherService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "teacher-registration-topic", groupId = "university-group")
    public void consumeTeacherRegistration(String teacherDataJson) {
        log.info("Received teacher data as JSON: {}", teacherDataJson);

        try {
            Map<String, Object> teacherData = objectMapper.readValue(
                    teacherDataJson,
                    new TypeReference<Map<String, Object>>() {}
            );

            Object result = teacherService.createTeacher(teacherData);
            log.info("Teacher created successfully: {}", result);
        } catch (Exception e) {
            log.error("Error processing teacher registration", e);
        }
    }
}