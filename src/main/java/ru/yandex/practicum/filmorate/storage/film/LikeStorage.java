package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LikeStorage {

    public LikeStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final JdbcTemplate jdbcTemplate;

    public void add(long filmId, long userId) {
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        updateRate(filmId);
    }

    public void delete(long filmId, long userId) {
        jdbcTemplate.update("DELETE FROM likes "
                + "WHERE film_id=? "
                + "AND user_id=?", filmId, userId);
        updateRate(filmId);
    }

    private Integer count(long filmId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM likes WHERE film_id=" + filmId, Integer.class);
    }

    private void updateRate(long filmId) {
        int newRating = count(filmId);
        jdbcTemplate.update("UPDATE films SET rating=? WHERE id=?", newRating, filmId);
    }
}