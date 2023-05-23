package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.MPAStorage;

import java.util.List;

@Service
public class MpaService {

    private final MPAStorage storage;

    public MpaService(MPAStorage storage) {
        this.storage = storage;
    }

    public MPA get(final int id) {
        return storage.getMPA(id);
    }

    public List<MPA> getAll() {
        return storage.getAllMPA();
    }
}