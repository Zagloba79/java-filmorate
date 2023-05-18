package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component("userDbStorage")
@Primary
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users");
        ArrayList<User> users = new ArrayList<>();
        while (userRows.next()) {
            User user = fillUser(userRows);
            users.add(user);
        }
        return users;
    }

    @Override
    public Optional<User> getUser(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);
        if (!userRows.next()) {
            log.info("Пользователь с идентификатором {} не найден.", id);
            throw new ValidationException("Нет такого пользователя", HttpStatus.NOT_FOUND);
        } else {
            log.info("Найден пользователь: {}", userRows.getString("id"));
            User user = fillUser(userRows);
            return Optional.of(user);
        }
    }

    @Override
    public User create(User user) {
        validate(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String insertSql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setString(4, user.getBirthday().toString());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            user.setId((Integer) keyHolder.getKey());
        }
        return user;
    }

    @Override
    public User update(User user) {
        int id = user.getId();
        getUser(id);
        validate(user);
        String insertSql = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(insertSql);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setString(4, user.getBirthday().toString());
            ps.setInt(5, id);
            return ps;
        });
        return user;
    }

    @Override
    public void delete(User user) {
        int id = user.getId();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id=?", id);
        if (!userRows.next()) {
            log.info("Пользователь с идентификатором {} не найден.", id);
        } else {
            jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        }
    }

    @Override
    public void addFriends(int userId, int friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM friendship " +
                "WHERE user_id=? AND friend_id=?", friendId, userId);
        if (userRows.next()) {
            jdbcTemplate.update("UPDATE friendship SET confirmed_friend=true WHERE " +
                    "user_id=? AND friend_id=?", friendId, userId);
        } else {
            jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id) VALUES(?, ?)", userId, friendId);
        }
    }

    @Override
    public void argueFriends(int userId, int friendId) {
        SqlRowSet userFriendRows = jdbcTemplate.queryForRowSet("SELECT * FROM friendship " +
                "WHERE user_id=? AND friend_id=?", userId, friendId);
        SqlRowSet friendUserRows = jdbcTemplate.queryForRowSet("SELECT * FROM friendship " +
                "WHERE user_id=? AND friend_id=?", friendId, userId);
        if (!userFriendRows.next() && !friendUserRows.next()) {
            log.info("Дружба не зафиксирована.");
        } else {
            jdbcTemplate.update("DELETE FROM friendship WHERE user_id=? AND friend_id=?", userId, friendId);
            jdbcTemplate.update("DELETE FROM friendship WHERE user_id=? AND friend_id=?", friendId, userId);
        }
    }

    @Override
    public List<User> showCommonFriends(int userId, int friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * from users u WHERE u.id IN(SELECT f1.friend_id FROM friendship AS f1 JOIN " +
                        "friendship AS f2 ON f1.friend_id=f2.friend_id WHERE (f1.user_id =" + userId + " AND f1.friend_id <> " + friendId + ") AND " +
                        "(f2.user_id =" + friendId + " AND f2.friend_id <> " + userId + "))");
        List<User> commonFriends = new ArrayList<>();
        while (userRows.next()) {
            User user = fillUser(userRows);
            commonFriends.add(user);
        }
        return commonFriends;
    }

    @Override
    public List<User> getFriends(User user) {
        validate(user);
        int id = user.getId();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM users WHERE id IN (SELECT  f.friend_id FROM friendship f WHERE f.user_id =?)", id);
        List<User> friends = new ArrayList<>();
        while (userRows.next()) {
            User friend = fillUser(userRows);
            friends.add(friend);
        }
        return friends;
    }

    private User fillUser(SqlRowSet userRows) {
        User user = new User();
        user.setId(userRows.getInt("id"));
        user.setEmail(userRows.getString("email"));
        user.setLogin(userRows.getString("login"));
        user.setName(userRows.getString("name"));
        user.setBirthday(userRows.getDate("birthday").toLocalDate());
        return user;
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
