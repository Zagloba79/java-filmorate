package ru.yandex.practicum.filmorate.storage.film;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
	private static final LocalDate FIRST_TIME = LocalDate.of(1895, 12, 28);
	private static final int MAX_DESCRIPTION = 200;
	private final JdbcTemplate jdbcTemplate;
	private final MPAStorage mpaStorage;
	KeyHolder keyHolder = new GeneratedKeyHolder();

	public FilmDbStorage(JdbcTemplate jdbcTemplate, MPAStorage mpaStorage) {
		this.jdbcTemplate = jdbcTemplate;
		this.mpaStorage = mpaStorage;
	}

	@Override
	public List<Film> findAll() {
		SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films");
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

		String insertSql = "insert into films(name, description, rating_id, release_date, duration)" +
				"values(?, ?, ?, ?, ?)";
		LocalDateTime releaseDateAsTimeStamp = film.getReleaseDate().atTime(LocalTime.MIDNIGHT);
	   jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection
					.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, film.getName());
			ps.setString(2, film.getDescription());
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
		SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films where id = ?", id);
		if (!filmRows.next()) {
			throw new ObjectNotFoundException("film not found");
		} else {
			String updateSql = "UPDATE films SET name=?, description=?, rating_id=?, release_date=?, duration=? " +
					" WHERE id=?";
			LocalDateTime releaseDateAsTimeStamp = film.getReleaseDate().atTime(LocalTime.MIDNIGHT);
		    jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection
						.prepareStatement(updateSql);
				ps.setString(1, film.getName());
				ps.setString(2, film.getDescription());
				ps.setInt(3, film.getMpa().getId());
				ps.setTimestamp(4, Timestamp.valueOf(releaseDateAsTimeStamp));
				ps.setString(5, Long.toString(film.getDuration()));
				ps.setInt(6, id);
				return ps;
			});
		}
		updateGenresForFilmId(film.getId(), film.getGenres());
		return getFilm(id).get();
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
	public Optional<Film> getFilm(int id) {
		SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);
		if (!filmRows.next()) {
			log.info("Фильм с идентификатором {} не найден.", id);
			throw new ValidationException("Нет такого фильма", HttpStatus.NOT_FOUND);
		} else {
			log.info("Найден фильм: {}", filmRows.getString("id"));
			Film film = fillFilm(filmRows);
			return Optional.of(film);
		}
	}

	private Film fillFilm(SqlRowSet filmRows) {
		Film film = new Film();
		film.setId(filmRows.getInt("id"));
		film.setName(filmRows.getString("name"));
		film.setDescription(filmRows.getString("description"));
		int mpaId = filmRows.getInt("rating_id");
		MPA mpa = mpaStorage.getMPA(mpaId);
		film.setMpa(mpa);
		List<Genre> genres = getGenres(film.getId());
		film.setGenres(genres);
		film.setReleaseDate(filmRows.getTimestamp("release_date").toLocalDateTime().toLocalDate());
		film.setDuration(filmRows.getLong("duration"));
		return film;
	}


	public void updateGenresForFilmId(long filmId, Collection<Genre> genres) {

		deleteGenresForFilmId(filmId);
		Set<Integer> genresIds = genres.stream().map(e->e.getId()).collect(Collectors.toSet());
		for (Integer genreId : genresIds) {
			jdbcTemplate.update(""
					+ "INSERT INTO film_genres (film_id, genre_id) "
					+ "VALUES (?, ?)", filmId, genreId);
		}
	}

	public List<Genre> getGenres(long filmId) {
		List<Genre> genres = new ArrayList<>();
		SqlRowSet genresRows = jdbcTemplate.queryForRowSet("SELECT id, name "
				+ "FROM genres g WHERE g.id IN(SELECT genre_id FROM film_genres f WHERE f.film_id=?) "
				+ "ORDER BY g.id", filmId);
		while (genresRows.next()) {
			Genre genre = new Genre();
			genre.setId(genresRows.getInt("id"));
			genre.setName(genresRows.getString("name"));
			genres.add(genre);
		}
		return genres;
	}
	private void deleteGenresForFilmId(long filmId) {

		jdbcTemplate.update(""
				+ "DELETE "
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
