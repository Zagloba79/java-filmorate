package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 0;
    private static final LocalDate FIRST_TIME = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION = 200;

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> getFilm(int id) {
        if (!films.containsKey(id)) {
            log.info("Фильма с id=" + id + " не существует.");
            throw new ObjectNotFoundException("Фильма с id=" + id + " не существует.");
        }
        return Optional.of(films.get(id));
    }

    @Override
    public List<Film> showTopList(int count) {
        List<Film> sortedCollection = findAll();
        Collections.sort(sortedCollection, (film1, film2) -> film2.getRating() - film1.getRating());
        List<Film> topList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            topList.add(sortedCollection.get(i));
        }
        return topList;
    }

    @Override
    public Film create(Film film) {
        if (films.containsKey(film.getId())) {
            log.info("Фильм  " + film.getId() + " уже есть в базе.");
            throw new ObjectAlreadyExistException("Фильм  " + film.getId() + " уже есть в базе");
        }
        validate(film);
        film.setId(++currentId);
        films.put(currentId, film);
        log.info("Фильм  " + film.getId() + " добавлен в базу");
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Нет такого фильма");
            throw ValidationException.createNotFoundException("Нет такого фильма");
        }
        validate(film);
        films.put(film.getId(), film);
        log.info("Фильм  " + film.getId() + " обновлён в базе");
        return film;
    }

    @Override
    public void delete(Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Нет такого фильма");
            throw new ObjectNotFoundException("Нет такого фильма");
        }
        validate(film);
        films.remove(film.getId());
        log.info("Фильм  " + film.getId() + " удалён");
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.info("У фильма нет названия");
            throw new ValidationException("У фильма нет названия");
        }
        if (film.getDescription() == null) {
            log.info("Опишите фильм");
            throw new ValidationException("Опишите фильм");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION) {
            log.info("Максимальная длина описания - 200 символов");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.info("Дата выхода фильма отсутствует");
            throw new ValidationException("Дата выхода фильма отсутствует");
        }
        if (film.getReleaseDate().isBefore(FIRST_TIME)) {
            log.info("Исправьте дату на более позднюю. До 28 декабря 1895 года не выпускали фильмы");
            throw new ValidationException("Исправьте дату на более позднюю. До 28 декабря 1895 года не выпускали фильмы");
        }
        if (film.getDuration() <= 0) {
            log.info("Продолжительность фильма должна быть больше нуля");
            throw new ValidationException("Продолжительность фильма должна быть больше нуля");
        }
    }
}