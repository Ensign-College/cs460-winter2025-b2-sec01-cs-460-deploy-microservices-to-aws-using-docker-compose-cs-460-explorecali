package edu.ensign.cs460.recommendation;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test configuration for recommendation package tests.
 * Enables component scanning to find the main application configuration.
 */
@TestConfiguration
@ComponentScan(basePackages = { "com.example.explorecalijpa", "edu.ensign.cs460" })
public class RecommendationTestConfig {
}
