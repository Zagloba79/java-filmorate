package ru.yandex.practicum.filmorate.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

@RequiredArgsConstructor
@Service
public class GenreService {

	private final GenreStorage storage;

	public List<Genre> getAll() {
		return storage.getAllGenre();
	}

	public Genre get(final int id) {
		return storage.getGenre(id);
	}
}
