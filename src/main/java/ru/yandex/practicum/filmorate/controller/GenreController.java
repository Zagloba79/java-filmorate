package ru.yandex.practicum.filmorate.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
	private final GenreService genreService;

	@GetMapping
	public List<Genre> getGenres() {
		return genreService.getAll();
	}

	@GetMapping("/{id}")
	public Genre getGenre(@PathVariable int id) {
		return genreService.get(id);
	}
}