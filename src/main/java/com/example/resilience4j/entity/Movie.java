package com.example.resilience4j.entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Movie {

    private String id;
    private String title;
    private String director;
    private double rating;
}
