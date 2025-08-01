package com.example.resilience4j.controller;

import com.example.resilience4j.entity.Movie;
import com.example.resilience4j.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/movies")
@Slf4j
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable String id, @RequestParam(name = "requestId") String requestId) {
        log.info("requestId {} ", requestId);
        MDC.put("requestId",requestId);
        Movie movie = movieService.getMovieDetailsWithCountBasedCircuitBreaker(id);
        return  ResponseEntity.ok(movie);
    }
}
