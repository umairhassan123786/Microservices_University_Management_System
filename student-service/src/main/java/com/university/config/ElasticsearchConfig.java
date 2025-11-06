package com.university.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.university.Repository")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        try {
            ClientConfiguration clientConfiguration;

            if (elasticsearchUrl.startsWith("https")) {
                // HTTPS with authentication
                String hostAndPort = elasticsearchUrl.replace("https://", "");
                clientConfiguration = ClientConfiguration.builder()
                        .connectedTo(hostAndPort)
                        .usingSsl()
                        .withBasicAuth(username, password)
                        .withConnectTimeout(10000)
                        .withSocketTimeout(30000)
                        .build();
            } else {
                // HTTP without authentication
                clientConfiguration = ClientConfiguration.builder()
                        .connectedTo("localhost:9200")
                        .withConnectTimeout(10000)
                        .withSocketTimeout(30000)
                        .build();
            }

            return RestClients.create(clientConfiguration).rest();
        } catch (Exception e) {
            System.err.println("Elasticsearch client configuration failed: " + e.getMessage());
            return null;
        }
    }
}