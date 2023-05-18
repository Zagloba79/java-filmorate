package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import java.util.List;

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
