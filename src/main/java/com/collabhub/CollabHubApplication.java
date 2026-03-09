package com.collabhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CollabHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollabHubApplication.class, args);
    }
}
