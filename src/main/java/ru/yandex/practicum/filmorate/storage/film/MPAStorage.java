package ru.yandex.practicum.filmorate.storage.film;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

@Slf4j
@Repository
public class MPAStorage {
	private final JdbcTemplate jdbcTemplate;

	public MPAStorage(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public MPA getMPA(int id) {
		SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM rating WHERE id = ?", id);
		if (userRows.next()) {
			MPA mpa = new MPA();
			mpa.setId(userRows.getInt("id"));
			mpa.setName(userRows.getString("name"));
			return mpa;
		} else {
			throw  new ObjectNotFoundException("mpa not found");
		}
	}

	public List<MPA> getAllMPA() {
		SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM rating");
		List<MPA> mpaList = new ArrayList<>();
		while (mpaRows.next()) {
			MPA mpa = new MPA();
			mpa.setId(mpaRows.getInt("id"));
			mpa.setName(mpaRows.getString("name"));
			mpaList.add(mpa);

		}
		return mpaList;
	}
}
