package ru.yandex.practicum.filmorate.model;

import java.time.LocalDateTime;
import lombok.*;

@Data
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDateTime releaseDate;
    private long duration;
}
