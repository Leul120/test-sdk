package com.example.sdkdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A simple demo application monitored by AI-Synapse SDK.
 * The @EnableSynapse annotation activates the SDK monitoring.
 */
@SpringBootApplication
public class SdkDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdkDemoApplication.class, args);
    }
}
