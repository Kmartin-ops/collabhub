package com.collabhub;

import com.collabhub.config.CollabHubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CollabHubProperties.class)
public class CollabHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollabHubApplication.class, args);
    }
}