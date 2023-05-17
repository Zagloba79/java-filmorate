package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserDbService {
    private final UserStorage userDbStorage;

    public UserDbService(UserStorage userDbStorage) {
        this.userDbStorage = userDbStorage;
    }

    public Optional<User> findUserById(int id) {
        return userDbStorage.getUser(id);
    }

    public void addFriend(int userId, int friendId) {
        Optional<User> userOptional = userDbStorage.getUser(userId);
        Optional<User> friendOptional = userDbStorage.getUser(userId);
        if (userOptional.isPresent() && friendOptional.isPresent()) {
            User user = userOptional.get();
            User friend = friendOptional.get();
            Map<Integer, Boolean> userFriends = user.getFriends();
            Map<Integer, Boolean> friendFriends = friend.getFriends();
            if (userFriends == null) {
                userFriends = new HashMap<>();
            }
            if (friendFriends.containsKey(userId)) {
                userFriends.put(friendId, true);
                friendFriends.remove(userId);
                friendFriends.put(userId, true);
            } else {
                userFriends.put(friendId, false);
            }
        }
    }

    public void argueFriends(int userId, int friendId) {
        Optional<User> userOptional = userDbStorage.getUser(userId);
        Optional<User> friendOptional = userDbStorage.getUser(userId);
        if (userOptional.isPresent() && friendOptional.isPresent()) {
            User user = userOptional.get();
            User friend = friendOptional.get();
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
        }
    }

    public List<User> showCommonFriends(int userId, int friendId) {
        User user = new User();
        User friend = new User();
        Optional<User> userOptional = userDbStorage.getUser(userId);
        Optional<User> friendOptional = userDbStorage.getUser(userId);
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
                Optional<User> thisUserOptional = userDbStorage.getUser(thisId);
                if (thisUserOptional.isPresent()) {
                    User thisUser = thisUserOptional.get();
                    commonFriends.add(thisUser);
                }
            }
        }
        return new ArrayList<>(commonFriends);
    }

    public List<User> findAll() {
        return userDbStorage.findAll();
    }

    public User getUser(int userId) {
        User user = new User();
        Optional<User> userOptional = userDbStorage.getUser(userId);
        if (userOptional.isPresent()) {
            user = userOptional.get();
        }
        return user;
    }

    public User create(User user) {
        return userDbStorage.create(user);
    }


    public User update(User user) {
        return userDbStorage.update(user);
    }

    public void delete(User user) {
        for (Film film : user.getLikes()) {
            Set<User> thisFilmFans = film.getFans();
            thisFilmFans.remove(user);
        }
        userDbStorage.delete(user);
    }

    public List<User> getFriends(int id) {
        User user = getUser(id);
        List<User> friends = new ArrayList<>();
        for (Integer friendId : user.getFriends().keySet()) {
            User friend = getUser(friendId);
            friends.add(friend);
        }
        return friends;
    }
}