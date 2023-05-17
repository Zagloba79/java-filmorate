package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final LocalDate FIRST_TIME = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION = 200;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAll() {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films");
        ArrayList<Film> films = new ArrayList<>();
        while(filmRows.next()) {
            Film film = new Film();
            film.setId(filmRows.getInt("id"));
            film.setName(filmRows.getString("name"));
            film.setDescription(filmRows.getString("description"));
            film.setGenre(filmRows.getInt("genre"));
            film.setRating(filmRows.getString("rating"));
            film.setReleaseDate(filmRows.getDate("releaseDate").toLocalDate());
            film.setDuration(filmRows.getLong("duration"));
            films.add(film);
        }
        return films;
    }

    @Override
    public Film create(Film film) {
        validate(film);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String insertSql = "insert into films(name, description, genre, rating, releaseDate, duration)" +
                "values(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(insertSql);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setString(3, Integer.toString(film.getGenre()));
            ps.setString(4, film.getRating());
            ps.setString(5, film.getReleaseDate().toString());
            ps.setString(6, Long.toString(film.getDuration()));
            return ps;
        }, keyHolder);

        film.setId((Integer) keyHolder.getKey());
        return film;
    }

    @Override
    public Film update(Film film) {
        validate(film);
        int id = film.getId();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films where id = ?", id);
        if (!filmRows.next()) {
            log.info("Фильм с идентификатором {} не найден.", id);
        } else {
            String insertSql = "insert into films(name, description, genre, rating, releaseDate, duration) " +
                    "values(?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(insertSql);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setString(3, Integer.toString(film.getGenre()));
                ps.setString(4, film.getRating());
                ps.setString(5, film.getReleaseDate().toString());
                ps.setString(6, Long.toString(film.getDuration()));
                return ps;
            });
        }
        return film;
    }

    @Override
    public void delete(Film film) {
        int id = film.getId();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films where id = ?", id);
        if (!filmRows.next()) {
            log.info("Фильм с идентификатором {} не найден.", id);
        } else {
            jdbcTemplate.queryForRowSet("delete from films where id = ?", id);
        }
    }

    @Override
    public Optional<Film> getFilm(int filmId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films where id = ?", filmId);
        if(filmRows.next()) {
            log.info("Найден фильм: {}", filmRows.getString("id"));
            Film film = new Film();
            film.setId(filmRows.getInt("id"));
            film.setName(filmRows.getString("name"));
            film.setDescription(filmRows.getString("description"));
            film.setGenre(filmRows.getInt("genre"));
            film.setRating(filmRows.getString("rating"));
            film.setReleaseDate(filmRows.getDate("releaseDate").toLocalDate());
            film.setDuration(filmRows.getLong("duration"));
            return Optional.of(film);
        } else {
            log.info("Фильм с идентификатором {} не найден.", filmId);
            return Optional.empty();
        }
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
            throw new ValidationException("Исправьте дату на более позднюю. До 28 декабря 1895 года не выпускали фильмы");
        }
        if (film.getDuration() <= 0) {
            log.info("Продолжительность фильма должна быть больше нуля");
            throw new ValidationException("Продолжительность фильма должна быть больше нуля");
        }
    }
}
