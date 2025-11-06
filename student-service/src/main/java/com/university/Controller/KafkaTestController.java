package com.university.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class KafkaTestController {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/test-kafka-student")
    public String testKafkaStudent() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("userId", 9999L);
        testData.put("name", "Manual Test Student");
        testData.put("email", "manual-test@university.com");
        testData.put("rollNumber", "MANUAL001");
        testData.put("semester", "5");
        testData.put("department", "Computer Science");

        System.out.println("=== MANUAL KAFKA TEST FROM STUDENT SERVICE ===");
        System.out.println("Sending test data: " + testData);

        kafkaTemplate.send("student-registration-topic", testData)
                .addCallback(
                        result -> System.out.println("Manual test message sent successfully: " + result),
                        error -> System.out.println("Manual test message failed: " + error.getMessage())
                );

        return "Manual test message sent to student-registration-topic";
    }
}