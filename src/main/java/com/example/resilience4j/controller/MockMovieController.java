package com.example.resilience4j.controller;

import com.example.resilience4j.entity.Movie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock/movies")
public class MockMovieController {

    @GetMapping("/{id}")
    public ResponseEntity<?> getMovieById(@PathVariable String id) throws InterruptedException {
        Movie movie = null;
        switch (id) {
            case "1" -> {
                    movie = new Movie("1", "The Matrix", "Lana Wachowski, Lilly Wachowski", 8.7);
            } // end of 1
            case "2" -> {
                Thread.sleep(2000);
                movie = new Movie("2", "Inception", "Christopher Nolan", 8.8);
            } // end of 2
            case "3" -> {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
            } // end of 3
            case "4" -> {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("BAD_REQUEST");
            } // end of 4
            case "5" -> {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("INTERNAL_SERVER_ERROR");
            } // end of 5
        }
        return ResponseEntity.ok(movie);
    }
}
