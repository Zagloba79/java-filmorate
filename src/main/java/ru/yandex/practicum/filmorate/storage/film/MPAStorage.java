package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class MPAStorage {
    private final JdbcTemplate jdbcTemplate;

    public MPAStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MPA getMpaByFilmId(int filmId) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT m.* FROM mpa AS m " +
                "INNER JOIN films AS f " +
                "ON f.mpa_id = m.id" +
                " WHERE f.id = ?", filmId);
        if (!mpaRows.next()) {
            throw new ObjectNotFoundException("mpa not found");
        }
        MPA mpa = new MPA();
        mpa.setId(mpaRows.getInt("id"));
        mpa.setName(mpaRows.getString("name"));
        return mpa;
    }

    public MPA getMPA(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM mpa WHERE id = ?", id);
        if (!userRows.next()) {
            throw new ObjectNotFoundException("mpa not found");
        }
        MPA mpa = new MPA();
        mpa.setId(userRows.getInt("id"));
        mpa.setName(userRows.getString("name"));
        return mpa;
    }

    public List<MPA> getAllMPA() {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM mpa");
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