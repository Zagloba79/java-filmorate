//package ru.yandex.practicum.filmorate.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.service.FilmDbService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/films")
//public class FilmController {
//    private final FilmDbService filmService;
//
//    @Autowired
//    public FilmController(FilmDbService filmService) {
//        this.filmService = filmService;
//    }
//
//    @GetMapping
//    public List<Film> findAll() {
//        return filmService.findAll();
//    }
//
//    @GetMapping("/{filmId}")
//    public Film findById(@PathVariable int filmId) {
//        return filmService.getFilm(filmId);
//    }
//
//    @GetMapping("/popular")
//    public List<Film> showTopList(@RequestParam(defaultValue = "10") Integer count) {
//        return filmService.showTopFilms(count);
//    }
//
//    @PostMapping
//    public Film create(@RequestBody Film film) {
//        return filmService.create(film);
//    }
//
//    @PutMapping
//    public Film update(@RequestBody Film film) {
//        return filmService.update(film);
//    }
//
//    @PutMapping("/{id}/like/{userId}")
//    public void like(@PathVariable int id, @PathVariable int userId) {
//        filmService.addLike(id, userId);
//    }
//
//    @DeleteMapping
//    public void delete(@RequestBody Film film) {
//        filmService.delete(film);
//    }
//
//    @DeleteMapping("/{id}/like/{userId}")
//    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
//        filmService.deleteLike(id, userId);
//    }
//}