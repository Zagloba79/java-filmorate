package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LikeStorage {

	public LikeStorage(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate=jdbcTemplate;
	}
	private final JdbcTemplate jdbcTemplate;


	public void add(long filmId, long user_id) {

		jdbcTemplate.update(""
				+ "INSERT INTO likes (film_id, user_id) "
				+ "VALUES (?, ?)", filmId, user_id);

	}


	public void delete(long filmId, long user_id) {

		jdbcTemplate.update(""
				+ "DELETE FROM likes "
				+ "WHERE film_id=? "
				+ "AND user_id=?", filmId, user_id);

	}


	public Integer count(long filmId) {
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM likes WHERE film_id="+ filmId, Integer.class);
	}

}
