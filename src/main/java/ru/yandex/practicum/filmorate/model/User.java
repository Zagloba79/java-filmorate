package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.HashMap;

import lombok.*;

@Data
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private HashMap<Integer, String> friendship;
}