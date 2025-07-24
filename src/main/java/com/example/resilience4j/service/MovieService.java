package com.example.resilience4j.service;

import com.example.resilience4j.client.MovieApiClient;
import com.example.resilience4j.entity.Movie;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final CircuitBreakerRegistry registry;


    private final MovieApiClient movieApiClient;

    @CircuitBreaker(name = "countBasedCircuitBreaker")
    public Movie getMovieDetailsWithCountBasedCircuitBreaker(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @CircuitBreaker(name = "timeBasedCircuitBreaker")
    public Movie getMovieDetailsWithTimedBasedCircuitBreaker(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @CircuitBreaker(name = "circuitBreakerOnException")
    public Movie getMovieDetailsWithCircuitBreakerOnException(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @CircuitBreaker(name = "circuitBreakerWithRecordFailurePredicate")
    public Movie getMovieDetailsWithCircuitBreakerRecordFailurePredicate(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @CircuitBreaker(name = "circuitBreakerWithIgnoreExceptionPredicate")
    public Movie getMovieDetailsWithCircuitBreakerIgnoreExceptionPredicate(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @CircuitBreaker(name = "circuitBreakerForSlowCalls")
    public Movie getMovieDetailsWithCircuitBreakerForSlowCalls(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @CircuitBreaker(name = "countBasedCircuitBreaker", fallbackMethod = "fetchMovieDetailsFallbackMethod")
    public Movie getMovieDetailsWithFallbackMethod(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @CircuitBreaker(name = "customCircuitBreaker")
    public Movie getMovieDetailsWithCustomCircuitBreaker(String movieId) {
        return fetchMovieDetails(movieId);
    }

    public Movie fetchMovieDetailsFallbackMethod(String movieId, CallNotPermittedException callNotPermittedException) {
        log.info("Fallback method called.");
        log.info("CallNotPermittedException exception message: {}", callNotPermittedException.getMessage());
        return new Movie("Default", "N/A", "N/A", 0.0);
    }

    private Movie fetchMovieDetails(String movieId) {
        Movie movie = null;
        try {
            movie = movieApiClient.getMovieDetails(movieId);
        } catch (HttpServerErrorException httpServerErrorException) {
            log.error("Received HTTP server error exception while fetching the movie details. Error Message: {}", httpServerErrorException.getMessage());
            throw httpServerErrorException;
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error("Received HTTP client error exception while fetching the movie details. Error Message: {}", httpClientErrorException.getMessage());
            throw httpClientErrorException;
        } catch (ResourceAccessException resourceAccessException) {
            log.error("Received Resource Access exception while fetching the movie details.");
            throw resourceAccessException;
        } catch (Exception exception) {
            log.error("Unexpected error encountered while fetching the movie details");
            throw exception;
        }
        return movie;
    }

    @PostConstruct
    public void postConstruct() {
        var eventPublisher = registry.circuitBreaker("countBasedCircuitBreaker").getEventPublisher();
        eventPublisher.onEvent(event -> System.out.println("Count Based Circuit Breaker - On Event. Event Details: " + event));
    }

    void updateCircuitBreakerState(String name) {
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = registry.circuitBreaker(name);
        if (name.equalsIgnoreCase("closed")) {
            circuitBreaker.transitionToClosedState();
        } else if (name.equalsIgnoreCase("disabled")) {
            circuitBreaker.transitionToDisabledState();
        } else if (name.equalsIgnoreCase("forced-open")) {
            circuitBreaker.transitionToForcedOpenState();
        } else if (name.equalsIgnoreCase("open")) {
            circuitBreaker.transitionToOpenState();
        } else if (name.equalsIgnoreCase("metrics-only")) {
            circuitBreaker.transitionToMetricsOnlyState();
        } else if (name.equalsIgnoreCase("half-open")) {
            circuitBreaker.transitionToHalfOpenState();
        } else if (name.equalsIgnoreCase("open-for-5-minutes")) {
            circuitBreaker.transitionToOpenStateFor(Duration.ofMinutes(5));
        }
    }

}