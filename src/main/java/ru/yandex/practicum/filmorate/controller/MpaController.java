package ru.yandex.practicum.filmorate.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.MpaService;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
	private final MpaService mpaService;

	@GetMapping
	public List<MPA> getMpaRatings() {
		return mpaService.getAll();
	}

	@GetMapping("/{id}")
	public MPA getMpa(@PathVariable int id) {
		return mpaService.get(id);
	}
}