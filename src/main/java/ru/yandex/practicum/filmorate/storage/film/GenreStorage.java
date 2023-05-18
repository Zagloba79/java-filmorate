package ru.yandex.practicum.filmorate.storage.film;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

@Slf4j
@Repository
public class GenreStorage {

	private final JdbcTemplate jdbcTemplate;

	public GenreStorage(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}



	public Genre getGenre(int id) {
		SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE id = ?", id);
		if (userRows.next()) {
			Genre genre = new Genre();
			genre.setId(userRows.getInt("id"));
			genre.setName(userRows.getString("name"));
			return genre;
		} else {
			throw  new ObjectNotFoundException("genre not found");
		}
	}

	public List<Genre> getAllGenre() {
		SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT * FROM genres");
		List<Genre> genresList = new ArrayList<>();
		while (genresRows.next()) {
			Genre genre = new Genre();
			genre.setId(genresRows.getInt("id"));
			genre.setName(genresRows.getString("name"));
			genresList.add(genre);

		}
		return genresList;
	}


}
