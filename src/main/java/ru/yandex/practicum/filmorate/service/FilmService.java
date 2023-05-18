package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

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
		Optional<Film> filmOptional = filmStorage.getFilm(filmId);
		Optional<User> userOptional = userStorage.getUser(userId);
		if (filmOptional.isPresent() && userOptional.isPresent()) {
			likeStorage.add(filmId, userId);
		}
	}

	public void deleteLike(Integer filmId, Integer userId) {
		Optional<Film> filmOptional = filmStorage.getFilm(filmId);
		Optional<User> userOptional = userStorage.getUser(userId);
		if (filmOptional.isPresent() && userOptional.isPresent()) {
			likeStorage.delete(filmId, userId);
		}

	}

	public List<Film> showTopList(int count) {
		List<Film> popularFilms = filmStorage.findAll().stream()
				.sorted(this::likeCompare)
				.limit(count)
				.collect(Collectors.toList());
		log.trace("Возвращены популярные фильмы: {}.", popularFilms);
		return popularFilms;

	}

	private int likeCompare(Film film, Film otherFilm) {
		return Integer.compare(likeStorage.count(otherFilm.getId()), likeStorage.count(film.getId()));
	}

	public List<Film> findAll() {
		return filmStorage.findAll();
	}

	public Film getFilm(int filmId) {
		Optional<Film> filmOptional = filmStorage.getFilm(filmId);
		if (filmOptional.isPresent()) {
			return filmOptional.get();
		} else {
			throw new ObjectNotFoundException("film with id = " + filmId + " not found");
		}
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
