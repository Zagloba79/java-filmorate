package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {
    @Qualifier("userDbStorage")
    UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        Optional<User> userOptional = userStorage.getUser(userId);
        Optional<User> friendOptional = userStorage.getUser(friendId);
//        User user;
//        User friend;
//        Map<Integer, Boolean> userFriends = new HashMap<>();
//        Map<Integer, Boolean> friendFriends = new HashMap<>();
//        if (userOptional.isPresent()) {
//            user = userOptional.get();
//            userFriends = user.getFriends();
//        }
//        if (friendOptional.isPresent()) {
//            friend = friendOptional.get();
//            friendFriends = friend.getFriends();
//        }
//        if (friendFriends.containsKey(userId)) {
//                userFriends.put(friendId, true);
//                friendFriends.remove(userId);
//                friendFriends.put(userId, true);
//            } else {
//                userFriends.put(friendId, false);
//            }
        userStorage.addFriends(userId, friendId);
    }

    public void argueFriends(int userId, int friendId) {
        Optional<User> userOptional = userStorage.getUser(userId);
        Optional<User> friendOptional = userStorage.getUser(userId);
        if (userOptional.isPresent() && friendOptional.isPresent()) {
            User user = userOptional.get();
            User friend = friendOptional.get();
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
        }
        userStorage.argueFriends(userId, friendId);
    }

    public List<User> showCommonFriends(int userId, int friendId) {
        User user = new User();
        User friend = new User();
        Optional<User> userOptional = userStorage.getUser(userId);
        Optional<User> friendOptional = userStorage.getUser(friendId);
        if (userOptional.isPresent() && friendOptional.isPresent()) {
            user = userOptional.get();
            friend = friendOptional.get();
        }
        Map<Integer, Boolean> userFriends = user.getFriends();
        Map<Integer, Boolean> friendFriends = friend.getFriends();
        if (userFriends == null || friendFriends == null) {
            return Collections.EMPTY_LIST;
        }
        Set<User> commonFriends = new HashSet<>();
        for (Integer thisId : userFriends.keySet()) {
            if (friendFriends.containsKey(thisId)) {
                Optional<User> thisUserOptional = userStorage.getUser(thisId);
                if (thisUserOptional.isPresent()) {
                    User thisUser = thisUserOptional.get();
                    commonFriends.add(thisUser);
                }
            }
        }
        return new ArrayList<>(commonFriends);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User getUser(int userId) {
        User user;
        Optional<User> userOptional = userStorage.getUser(userId);
        if (userOptional.isEmpty()) {
            log.info("Пользователя с " + userId + " не существует.");
            throw new ObjectNotFoundException("Пользователя с " + userId + " не существует.");
        } else {
            user = userOptional.get();
        }
        return user;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void delete(User user) {
        for (Film film : user.getLikes()) {
            Set<User> thisFilmFans = film.getFans();
            thisFilmFans.remove(user);
        }
        userStorage.delete(user);
    }

    public List<User> getFriends(int id) {
        User user = getUser(id);
//        List<User> friends = new ArrayList<>();
//        for (Integer friendId : user.getFriends().keySet()) {
//            User friend = getUser(friendId);
//            friends.add(friend);
//        }
//        return friends;
        return userStorage.getFriends(user);
    }
}