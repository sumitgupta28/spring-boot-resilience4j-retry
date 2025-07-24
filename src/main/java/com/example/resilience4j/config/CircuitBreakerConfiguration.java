package com.example.resilience4j.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Bean
    public CircuitBreaker circuitBreakerWithCustomConfig() {
        CircuitBreakerConfig customConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .maxWaitDurationInHalfOpenState(Duration.ofMillis(5000))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();

        return circuitBreakerRegistry.circuitBreaker("customCircuitBreaker", customConfig);
    }
}
