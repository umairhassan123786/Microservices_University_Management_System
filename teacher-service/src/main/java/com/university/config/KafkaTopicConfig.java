package com.university.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic studentRegistrationTopic() {
        return TopicBuilder.name("student-registration-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic teacherRegistrationTopic() {
        return TopicBuilder.name("teacher-registration-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
