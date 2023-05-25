package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    private final LikeStorage likeStorage;

    public FilmService(FilmStorage filmDbStorage, UserStorage userDbStorage, LikeStorage likeStorage) {
        this.filmStorage = filmDbStorage;
        this.userStorage = userDbStorage;
        this.likeStorage = likeStorage;
    }

    public void addLike(int filmId, int userId) {
        filmStorage.getFilm(filmId).orElseThrow(() ->
                new ObjectNotFoundException("film with id = " + filmId + " not found"));
        userStorage.getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("user with id = " + userId + " not found"));
        likeStorage.add(filmId, userId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        filmStorage.getFilm(filmId).orElseThrow(() ->
                new ObjectNotFoundException("film with id = " + filmId + " not found"));
        userStorage.getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("user with id = " + userId + " not found"));
        likeStorage.delete(filmId, userId);
    }

    public List<Film> showTopList(int count) {
        List<Film> popularFilms = filmStorage.showTopList(count);
        log.trace("Возвращены популярные фильмы: {}.", popularFilms);
        return popularFilms;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film getFilm(int filmId) {
        return filmStorage.getFilm(filmId).orElseThrow(() ->
                new ObjectNotFoundException("film with id = " + filmId + " not found"));
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        filmStorage.getFilm(film.getId());
        return filmStorage.update(film);
    }

    public void delete(Film film) {
        filmStorage.delete(film);
    }
}