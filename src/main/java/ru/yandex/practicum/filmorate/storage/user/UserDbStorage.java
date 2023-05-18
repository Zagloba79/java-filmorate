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
import java.util.*;

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
        if(keyHolder.getKey()!=null){
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
    public Set<User> showCommonFriends(int userId, int friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT f1.friend_id FROM friendship AS f1 JOIN " +
                "friendship AS f2 ON f1.friend_id=f2.friend_id WHERE (f1.user_id =1 AND f1.friend_id <> 2) AND " +
                "(f2.user_id =2 AND f2.friend_id <> 1)", userId, friendId);
        SqlRowSet friendRows = jdbcTemplate.queryForRowSet("SELECT user_id FROM friendship WHERE friend_id " +
                "IN (?, ?) GROUP BY COUNT(user_id) HAVING COUNT(user_id)=2", userId, friendId);
        Set<User> commonFriends = new HashSet<>();
        while (userRows.next()) {
            User user = fillUser(userRows);
            commonFriends.add(user);
        }
        while (friendRows.next()) {
            User user = fillUser(friendRows);
            commonFriends.add(user);
        }
        return commonFriends;
    }

    @Override
    public List<User> getFriends(User user) {
        validate(user);
        int id = user.getId();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT f.user_id, f.friend_id, u.email, u.login, u.name, " +
                "u.birthday FROM friendship AS f WHERE f.user_id=? INNER JOIN users AS u ON u.id=f.friend_id;", id);
        if (!userRows.next()) {
            log.info("Друзья не найдены");
            throw new ValidationException("Нет друзей", HttpStatus.NOT_FOUND);
        }
        ArrayList<User> friends = new ArrayList<>();
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
