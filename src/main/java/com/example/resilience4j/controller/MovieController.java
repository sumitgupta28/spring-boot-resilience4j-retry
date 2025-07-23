package com.example.resilience4j.controller;


import com.example.resilience4j.entity.Movie;
import com.example.resilience4j.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movies")
@Slf4j
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable String id, @RequestParam(defaultValue = "simple-retry") String retryType) {
        switch (retryType) {
            case "simple-retry" -> {
                log.info("Simple retry example");
                Movie movie = movieService.getMovieDetails(id);
                return ResponseEntity.ok(movie);
            }
            case "retry-on-exception" -> {
                log.info("Retry on configured exceptions example");
                Movie movie = movieService.getMovieDetailsRetryOnException(id);
                return ResponseEntity.ok(movie);
            }
            case "retry-on-exception-predicate" -> {
                log.info("Retry on exception predicate example");
                Movie movie = movieService.getMovieDetailsRetryOnExceptionPredicate(id);
                return ResponseEntity.ok(movie);
            }
            case "retry-on-conditional-predicate" -> {
                log.info("Retry on conditional predicate example");
                Movie movie = movieService.getMovieDetailsRetryOnConditionalPredicate(id);
                return ResponseEntity.ok(movie);
            }
            case "retry-using-exponential-backoff" -> {
                log.info("Retry using exponential backoff example");
                Movie movie = movieService.getMovieDetailsRetryUsingExponentialBackoff(id);
                return ResponseEntity.ok(movie);
            }
            case "retry-using-randomized-wait" -> {
                log.info("Retry using randomized wait example");
                Movie movie = movieService.getMovieDetailsRetryUsingRandomizedWait(id);
                return ResponseEntity.ok(movie);
            }
            case "retry-with-fallback" -> {
                log.info("Retry with fallback example");
                Movie movie = movieService.getMovieDetailsWithFallback(id);
                return ResponseEntity.ok(movie);
            }
            case "retry-with-custom-config" -> {
                log.info("Retry with custom config example");
                Movie movie = movieService.getMovieDetailsWithCustomRetryConfig(id);
                return ResponseEntity.ok(movie);
            }
            case "retry-with-event-details" -> {
                log.info("Retry with event details example");
                Movie movie = movieService.getMovieDetailsWithRetryEventDetails(id);
                return ResponseEntity.ok(movie);
            }
        }
        return null;
    }
}
