package com.example.resilience4j.predicate;

import com.example.resilience4j.entity.Movie;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;
@Slf4j
public class ConditionPredicate implements Predicate<Movie> {
    @Override
    public boolean test(Movie movie) {
        log.debug("Condition predicate is called. Movie details is {} " , movie.toString());
        return movie.getId().equals("Default");
    }
}
