package com.example.resilience4j.service;

import com.example.resilience4j.client.MovieApiClient;
import com.example.resilience4j.entity.Movie;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;



@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieApiClient movieApiClient;

    @CircuitBreaker(name = "countBasedCircuitBreaker",fallbackMethod = "fetchMovieDetailsFallbackMethod")
    public Movie getMovieDetailsWithCountBasedCircuitBreaker(String movieId) {
        return fetchMovieDetails(movieId);
    }

    public Movie fetchMovieDetailsFallbackMethod(String movieId, Exception exception) {
        log.info("Fallback method called.");
        log.info("CallNotPermittedException exception message: {}", exception.getMessage());
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




}