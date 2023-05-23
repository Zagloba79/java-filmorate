package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {

    @Qualifier("userDbStorage")
    UserStorage userStorage;
    LikeStorage likeStorage;

    public UserService(UserStorage userStorage, LikeStorage likeStorage) {
        this.userStorage = userStorage;
        this.likeStorage = likeStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        userStorage.getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + userId + " не существует."));
        userStorage.getUser(friendId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + friendId + " не существует."));
        userStorage.addFriends(userId, friendId);
    }

    public void argueFriends(int userId, int friendId) {
        userStorage.getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + userId + " не существует."));
        userStorage.getUser(friendId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + friendId + " не существует."));
        userStorage.argueFriends(userId, friendId);
    }

    public List<User> showCommonFriends(int userId, int friendId) {
        userStorage.getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + userId + " не существует."));
        userStorage.getUser(friendId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + friendId + " не существует."));
        return userStorage.showCommonFriends(userId, friendId);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User getUser(int userId) {
        return userStorage.getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + userId + " не существует."));
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void delete(User user) {
        for (Film film : user.getLikes()) {
            int userId = user.getId();
            int filmId = film.getId();
            int rating = film.getRating();
            film.setRating(--rating);
            likeStorage.delete(filmId, userId);
            likeStorage.update(filmId);
        }
        userStorage.delete(user);
    }

    public List<User> getFriends(int id) {
        User user = getUser(id);
        return userStorage.getFriends(user);
    }
}