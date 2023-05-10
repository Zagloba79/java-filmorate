package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    @Autowired
    FilmStorage inMemoryFilmStorage;
    @Autowired
    UserStorage inMemoryUserStorage;

    public void addLike(Integer filmId, Integer userId) {
        Film film = inMemoryFilmStorage.getFilm(filmId);
        Set<Integer> likes = film.getLikes();
        if (likes == null) {
            likes = new HashSet<>();
        }
        if (inMemoryUserStorage.getUser(userId) != null) {
            likes.add(userId);
        }
    }

    public void deleteLike(Integer filmId, Integer userId) {
        Film film = inMemoryFilmStorage.getFilm(filmId);
        Set<Integer> likes = film.getLikes();
        if (likes != null) {
            likes.remove(userId);
        }
    }

    public Map<Integer, ArrayList<Film>> likesAndFilms() {
        Map<Integer, ArrayList<Film>> filmsByLikes = new HashMap<>();
        for (Film film : inMemoryFilmStorage.findAll()) {
            Integer countOfLikes = film.getLikes().size();
            ArrayList<Film> thisLikes;
            if (!filmsByLikes.containsKey(countOfLikes)) {
                thisLikes = new ArrayList<>();
            } else {
                thisLikes = filmsByLikes.get(countOfLikes);
            }
            thisLikes.add(film);
            filmsByLikes.put(countOfLikes, thisLikes);
        }
        return filmsByLikes;
    }

    public List<Film> showTopFilms(int limitOfTop) {
        Map<Integer, ArrayList<Film>> filmsByLikes = likesAndFilms();
        if (filmsByLikes.isEmpty()) {
            return inMemoryFilmStorage.findAll().stream().limit(limitOfTop).collect(Collectors.toList());
        } else {
            Map<Integer, ArrayList<Film>> sortedMap = new TreeMap<>(Comparator.reverseOrder());
            sortedMap.putAll(filmsByLikes);
            return sortedMap.values().stream().flatMap(List::stream).limit(limitOfTop).collect(Collectors.toList());
        }
    }

    public List<Film> findAll() {
        return inMemoryFilmStorage.findAll();
    }

    public Film getFilm(@PathVariable int filmId) {
        return inMemoryFilmStorage.getFilm(filmId);
    }

    public Film create(@RequestBody Film film) {
        return inMemoryFilmStorage.create(film);
    }

    public Film update(@RequestBody Film film) {
        return inMemoryFilmStorage.update(film);
    }

    public void delete(@RequestBody Film film) {
        inMemoryFilmStorage.delete(film);
    }
}