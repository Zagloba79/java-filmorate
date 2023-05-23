package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hibernate.hql.internal.antlr.SqlTokenTypes.NULL;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private static final LocalDate FIRST_TIME = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION = 200;
    private final JdbcTemplate jdbcTemplate;
    private final MPAStorage mpaStorage;
    private final GenreStorage genreStorage;
    KeyHolder keyHolder = new GeneratedKeyHolder();

    public FilmDbStorage(JdbcTemplate jdbcTemplate, MPAStorage mpaStorage, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    @Override
    public List<Film> findAll() {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films");
        List<Film> films = createListOfFilms(filmRows);
        fillGenres(films);
        return films;
    }

    public List<Film> showTopList(int count) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films " +
                "ORDER BY rating DESC LIMIT ?", count);
        List<Film> films = createListOfFilms(filmRows);
        fillGenres(films);
        return films;
    }

    public List<Film> createListOfFilms(SqlRowSet filmRows) {
        ArrayList<Film> films = new ArrayList<>();
        while (filmRows.next()) {
            Film film = fillFilm(filmRows);
            films.add(film);
        }
        return films;
    }

    @Override
    public Film create(Film film) {
        validate(film);
        String insertSql = "INSERT INTO films (name, description, mpa_id, release_date, duration)" +
                "VALUES (?, ?, ?, ?, ?)";
        LocalDateTime releaseDateAsTimeStamp = film.getReleaseDate().atTime(LocalTime.MIDNIGHT);
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            if (film.getMpa() != null) {
                ps.setString(2, film.getDescription());
            } else {
                ps.setNull(3, NULL);
            }
            ps.setInt(3, film.getMpa().getId());
            ps.setTimestamp(4, Timestamp.valueOf(releaseDateAsTimeStamp));
            ps.setString(5, Long.toString(film.getDuration()));
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            film.setId((Integer) keyHolder.getKey());
        }
        updateGenresForFilmId(film.getId(), film.getGenres());
        return getFilm(film.getId()).get();
    }

    @Override
    public Film update(Film film) {
        validate(film);
        int id = film.getId();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);
        if (!filmRows.next()) {
            throw new ObjectNotFoundException("Нет такого фильма", HttpStatus.NOT_FOUND);
        } else {
            String updateSql = "UPDATE films SET name=?, description=?, rating=?, mpa_id=?, release_date=?, duration=? " +
                    " WHERE id=?";
            LocalDateTime releaseDateAsTimeStamp = film.getReleaseDate().atTime(LocalTime.MIDNIGHT);
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(updateSql);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setInt(3, film.getRating());
                ps.setInt(4, film.getMpa().getId());
                ps.setTimestamp(5, Timestamp.valueOf(releaseDateAsTimeStamp));
                ps.setString(6, Long.toString(film.getDuration()));
                ps.setInt(7, id);
                return ps;
            });
        }
        updateGenresForFilmId(film.getId(), film.getGenres());
        return getFilm(id).get();
    }

    @Override
    public void delete(Film film) {
        int id = film.getId();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);
        if (!filmRows.next()) {
            log.info("Фильм с идентификатором {} не найден.", id);
        } else {
            jdbcTemplate.queryForRowSet("DELETE FROM films WHERE id = ?", id);
        }
    }

    @Override
    public Optional<Film> getFilm(int id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);
        if (!filmRows.next()) {
            log.info("Фильм с идентификатором {} не найден.", id);
            throw new ObjectNotFoundException("Нет такого фильма", HttpStatus.NOT_FOUND);
        } else {
            log.info("Найден фильм: {}", filmRows.getString("id"));
            Film film = fillFilm(filmRows);
            fillGenres(List.of(film));
            return Optional.of(film);
        }
    }

    private Film fillFilm(SqlRowSet filmRows) {
        Film film = new Film();
        film.setId(filmRows.getInt("id"));
        film.setName(filmRows.getString("name"));
        film.setDescription(filmRows.getString("description"));
        int mpaId = filmRows.getInt("mpa_id");
        MPA mpa = mpaStorage.getMPA(mpaId);
        film.setMpa(mpa);
        film.setReleaseDate(filmRows.getTimestamp("release_date").toLocalDateTime().toLocalDate());
        film.setDuration(filmRows.getLong("duration"));
        return film;
    }

    public void updateGenresForFilmId(long filmId, Collection<Genre> genres) {
        deleteGenresForFilmId(filmId);
        Set<Integer> genresIds = genres.stream().map(e -> e.getId()).collect(Collectors.toSet());
        for (Integer genreId : genresIds) {
            jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) "
                    + "VALUES (?, ?)", filmId, genreId);
        }
    }

    public void fillGenres(List<Film> films) {
        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));
        Map<Integer, Genre> genreMap = genreStorage.getAllGenre().stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));
        String ids = films.stream().map(film -> String.valueOf(film.getId()))
                .collect(Collectors.joining(","));
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT genre_id, film_id " +
                "FROM film_genres f WHERE f.film_id IN(" + ids + ")");
        while (genresRows.next()) {
            Integer genreId = genresRows.getInt("genre_id");
            Integer filmId = genresRows.getInt("film_id");
            if (filmMap.get(filmId) != null && genreMap.get(genreId) != null) {
                filmMap.get(filmId).getGenres().add(genreMap.get(genreId));
            }
        }
    }

    private void deleteGenresForFilmId(long filmId) {
        jdbcTemplate.update("DELETE "
                + "FROM film_genres "
                + "WHERE film_id=?", filmId);
    }

    public void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.info("У фильма нет названия");
            throw new ValidationException("У фильма нет названия");
        }
        if (film.getDescription() == null) {
            log.info("Опишите фильм");
            throw new ValidationException("Опишите фильм");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION) {
            log.info("Максимальная длина описания - 200 символов");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.info("Дата выхода фильма отсутствует");
            throw new ValidationException("Дата выхода фильма отсутствует");
        }
        if (film.getReleaseDate().isBefore(FIRST_TIME)) {
            log.info("Исправьте дату на более позднюю. До 28 декабря 1895 года не выпускали фильмы");
            throw new ValidationException(
                    "Исправьте дату на более позднюю. До 28 декабря 1895 года не выпускали фильмы");
        }
        if (film.getDuration() <= 0) {
            log.info("Продолжительность фильма должна быть больше нуля");
            throw new ValidationException("Продолжительность фильма должна быть больше нуля");
        }
    }
}