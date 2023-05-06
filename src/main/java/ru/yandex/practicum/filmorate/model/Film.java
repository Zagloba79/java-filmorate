package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import lombok.*;

@Data
public class Film {
    private int id;
    private String name;
    private int genre;
    private String description;
    private String rating;
    private LocalDate releaseDate;
    private long duration;
}