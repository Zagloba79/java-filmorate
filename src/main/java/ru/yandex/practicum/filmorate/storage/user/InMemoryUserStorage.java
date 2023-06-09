package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int currentId = 0;

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> getUser(int id) {
        if (!users.containsKey(id)) {
            log.info("Пользователя с id=" + id + " не существует.");
            throw new ObjectNotFoundException("Пользователя с id=" + id + " не существует.");
        }
        return Optional.of(users.get(id));
    }

    public User create(User user) {
        if (users.containsKey(user.getId())) {
            log.info("Пользователь  " + user.getId() + " уже есть зарегистрирован.");
            throw new ObjectAlreadyExistException("Пользователь  " + user.getId() + " уже есть зарегистрирован.");
        }
        validate(user);
        ++currentId;
        user.setId(currentId);
        users.put(currentId, user);
        log.info("Вы только что зарегистрировали пользователя с именем " + user.getName()
                + " и электронной почтой " + user.getEmail());
        return user;
    }

    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Нет такого пользователя");
            throw new ValidationException("Нет такого пользователя", HttpStatus.NOT_FOUND);
        }
        validate(user);
        int id = user.getId();
        users.put(id, user);
        log.info("Данные пользователя с именем " + user.getName() + " обновлены.");
        return user;
    }

    public void delete(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Такой пользователь не зарегистрирован");
            throw new ObjectNotFoundException("Такой пользователь не зарегистрирован");
        }
        users.remove(user.getId());
        log.info("Пользователь  " + user.getId() + " удалён");
    }

    @Override
    public void addFriends(int userId, int friendId) {
        User user = getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + userId + " не существует."));
        User friend = getUser(friendId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + friendId + " не существует."));
        if (friend.getFriends().containsKey(userId)) {
            user.getFriends().put(friendId, true);
            friend.getFriends().remove(userId);
            friend.getFriends().put(userId, true);
        } else {
            user.getFriends().put(friendId, false);
        }
    }

    @Override
    public void argueFriends(int userId, int friendId) {
        User user = getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + userId + " не существует."));
        User friend = getUser(friendId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + friendId + " не существует."));
        user.getFriends().put(friendId, true);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public List<User> showCommonFriends(int userId, int friendId) {
        ArrayList<User> commonFriends = new ArrayList<>();
        User user = getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + userId + " не существует."));
        User friend = getUser(friendId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователя с " + friendId + " не существует."));
        for (Integer id : user.getFriends().keySet()) {
            if (friend.getFriends().containsKey(id)) {
                User theirFriend = getUser(id).orElseThrow(() ->
                        new ObjectNotFoundException("Пользователя с " + id + " не существует."));
                commonFriends.add(theirFriend);
            }
        }
        return commonFriends;
    }

    @Override
    public List<User> getFriends(User user) {
        ArrayList<User> friends = new ArrayList<>();
        for (Integer id : user.getFriends().keySet()) {
            User friend = getUser(id).orElseThrow(() ->
                    new ObjectNotFoundException("Пользователя с " + id + " не существует."));
            friends.add(friend);
        }
        return friends;
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