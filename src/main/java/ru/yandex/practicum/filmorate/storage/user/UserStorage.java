package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    List<User> findAll();

    Optional<User> getUser(int id);

    User create(User user);

    User update(User user);

    void delete(User user);

    void addFriends(int userId, int friendId);

    void argueFriends(int userId, int friendId);

    Set<User> showCommonFriends(int userId, int friendId);
}