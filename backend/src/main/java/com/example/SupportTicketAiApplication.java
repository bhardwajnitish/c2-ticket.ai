package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.example")
@ConfigurationPropertiesScan("com.example")
public class SupportTicketAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportTicketAiApplication.class, args);
    }
}