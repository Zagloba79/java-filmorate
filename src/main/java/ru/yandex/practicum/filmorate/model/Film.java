package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@ToString
public class Film {
    private Integer id;
    private String name;
    private String description;
    private List<Genre> genres = new ArrayList<>();
    private int rating;
    private LocalDate releaseDate;
    private long duration;
    private Set<User> fans;
    private MPA mpa;
}