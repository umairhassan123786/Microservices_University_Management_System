package com.university.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AttendanceProducer {

    private static final String TOPIC = "attendance-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendAttendanceEvent(String attendanceJson) {
        try {
            System.out.println("Sending attendance JSON to Kafka: " + attendanceJson);
            kafkaTemplate.send(TOPIC, attendanceJson);
            System.out.println("Attendance JSON sent successfully to Kafka topic: " + TOPIC);
        } catch (Exception e) {
            System.err.println("Failed to send attendance event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}