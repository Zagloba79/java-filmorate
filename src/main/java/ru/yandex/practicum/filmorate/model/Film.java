package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private Set<Genre> genres = new HashSet<>();
    private int rating;
    private MPA mpa;
    private LocalDate releaseDate;
    private long duration;
}