package ru.yandex.practicum.filmorate.controller;

import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@RequestBody Film film, HttpServletRequestWrapper request) {
        log.info("Получен запрос к эндпоинту: '{} {}', Строка параметров запроса: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        if(films.containsKey(film.getId())) {
            log.info("Фильм  " + film.getId() + " уже есть в базе.");
            throw new ValidationException("Фильм  " + film.getId() + " уже есть в базе.");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            log.info("У фильма нет названия");
            throw new ValidationException("У фильма нет названия");
        }
        if (film.getDescription().length() > 200) {
            log.info("Максимальная длина описания - 200 символов");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        LocalDateTime firstTime = LocalDateTime.of(1895, 12, 28, 0, 0, 0);
        if (film.getReleaseDate().isBefore(firstTime)) {
            log.info("Дата релиза какая-то странная");
            throw new ValidationException("Дата релиза какая-то странная");
        }
        if (film.getDuration() <= 0) {
            log.info("Продолжительность фильма должна быть больше нуля");
            throw new ValidationException("Продолжительность фильма должна быть больше нуля");
        }
        int id = film.getId();
        films.put(id, film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film, HttpServletRequestWrapper request) {
        log.info("Получен запрос к эндпоинту: '{} {}', Строка параметров запроса: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        int id = film.getId();
        films.remove(id);
        create(film, request);
        return film;
    }
}
