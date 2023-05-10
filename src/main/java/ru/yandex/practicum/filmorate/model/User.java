package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    @Getter(lazy = true)
    private final Set<Integer> friends = initFriends();

    private Set<Integer> initFriends() {
        return new HashSet<>();
    }
}