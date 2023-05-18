package ru.yandex.practicum.filmorate.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.MPAStorage;

@RequiredArgsConstructor
@Service
public class MpaService {

	private final MPAStorage storage;

	public MPA get(final int id) {
		return storage.getMPA(id);
	}

	public List<MPA> getAll() {
		return storage.getAllMPA();
	}
}
