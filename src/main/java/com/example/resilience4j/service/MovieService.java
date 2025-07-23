package com.example.resilience4j.service;

import com.example.resilience4j.client.MovieApiClient;
import com.example.resilience4j.entity.Movie;
import com.example.resilience4j.exception.MovieNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
@Slf4j
public class MovieService {

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private MovieApiClient movieApiClient;

    @Retry(name = "simpleRetry")
    public Movie getMovieDetails(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @Retry(name = "retryWithEventDetails")
    public Movie getMovieDetailsWithRetryEventDetails(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @Retry(name = "simpleRetry", fallbackMethod = "getMovieDetailsFallbackMethod")
    public Movie getMovieDetailsWithFallback(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @Retry(name = "customRetryConfig")
    public Movie getMovieDetailsWithCustomRetryConfig(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @Retry(name = "retryOnException")
    public Movie getMovieDetailsRetryOnException(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @Retry(name = "retryBasedOnConditionalPredicate")
    public Movie getMovieDetailsRetryOnConditionalPredicate(String movieId) {
        try {
            return fetchMovieDetails(movieId);
        } catch (MovieNotFoundException movieNotFoundException) {
            log.info("Movie not found exception encountered. Returning default value");
            return new Movie("Default", "N/A", "N/A", 0.0);
        }
    }

    @Retry(name = "retryBasedOnExceptionPredicate")
    public Movie getMovieDetailsRetryOnExceptionPredicate(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @Retry(name = "retryUsingExponentialBackoff")
    public Movie getMovieDetailsRetryUsingExponentialBackoff(String movieId) {
        return fetchMovieDetails(movieId);
    }

    @Retry(name = "retryUsingRandomizedWait")
    public Movie getMovieDetailsRetryUsingRandomizedWait(String movieId) {
        return fetchMovieDetails(movieId);
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

    private Movie getMovieDetailsFallbackMethod(String movieId, MovieNotFoundException movieNotFoundException) {
        log.info("Fallback method called.");
        log.info("Original exception message: {}", movieNotFoundException.getMessage());
        return new Movie("Default", "N/A", "N/A", 0.0);
    }

    @PostConstruct
    public void postConstruct() {
        io.github.resilience4j.retry.Retry.EventPublisher eventPublisher = retryRegistry.retry("retryWithEventDetails").getEventPublisher();
        eventPublisher.onEvent(event -> log.debug("Simple Retry - On Event. Event Details: {} " , event));
        eventPublisher.onError(event -> log.debug("Simple Retry - On Error. Event Details: {} " , event));
        eventPublisher.onRetry(event -> log.debug("Simple Retry - On Retry. Event Details: {} " , event));
        eventPublisher.onSuccess(event -> log.debug("Simple Retry - On Success. Event Details: {} " , event));
        eventPublisher.onIgnoredError(event -> log.debug("Simple Retry - On Ignored Error. Event Details: {} " , event));
    }

}
