package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users");
        ArrayList<User> users = new ArrayList<>();
        while(userRows.next()) {
            User user = new User();
            user.setId(userRows.getInt("id"));
            user.setEmail(userRows.getString("email"));
            user.setLogin(userRows.getString("login"));
            user.setName(userRows.getString("name"));
            user.setBirthday(userRows.getDate("birthday").toLocalDate());
            users.add(user);
        }
        return users;
    }

    @Override
    public Optional<User> getUser(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where id = ?", id);
        if(userRows.next()) {
            log.info("Найден пользователь: {}", userRows.getString("id"));
            User user = new User();
            user.setId(userRows.getInt("id"));
            user.setEmail(userRows.getString("email"));
            user.setLogin(userRows.getString("login"));
            user.setName(userRows.getString("name"));
            user.setBirthday(userRows.getDate("birthday").toLocalDate());
            return Optional.of(user);
        } else {
            log.info("Пользователь с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }

    @Override
    public User create(User user) {
        validate(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String insertSql = "insert into users(email, login, name, birthday) values(?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(insertSql);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setString(4, user.getBirthday().toString());
            return ps;
        }, keyHolder);
        user.setId((Integer) keyHolder.getKey());
        return user;
    }

    @Override
    public User update(User user) {
        validate(user);
        int id = user.getId();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where id = ?", id);
        if (!userRows.next()) {
            log.info("Пользователь с идентификатором {} не найден.", id);
        } else {
            String insertSql = "insert into users(email, login, name, birthday) values(?, ?, ?, ?)";
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(insertSql);
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setString(4, user.getBirthday().toString());
                return ps;
            });
        }
        return user;
    }

    @Override
    public void delete(User user) {
        int id = user.getId();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where id = ?", id);
        if (!userRows.next()) {
            log.info("Пользователь с идентификатором {} не найден.", id);
        } else {
            jdbcTemplate.queryForRowSet("delete from users where id = ?", id);
        }
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.info("Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.info("Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
