package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {
    @Autowired
    UserStorage inMemoryUserStorage;

    public void addFriend(Integer userId, Integer friendId) {
        User user = inMemoryUserStorage.getUser(userId);
        User friend = inMemoryUserStorage.getUser(friendId);
        Set<Integer> userFriends = user.getFriends();
        userFriends.add(friendId);
        user.setFriends(userFriends);
        Set<Integer> friendFriends = friend.getFriends();
        friendFriends.add(userId);
        friend.setFriends(friendFriends);
    }

    public void argueFriends(Integer userId, Integer friendId) {
        User user = inMemoryUserStorage.getUser(userId);
        User friend = inMemoryUserStorage.getUser(friendId);
        Set<Integer> userFriends = user.getFriends();
        userFriends.remove(friendId);
        user.setFriends(userFriends);
        Set<Integer> friendFriends = friend.getFriends();
        friendFriends.remove(userId);
        friend.setFriends(friendFriends);
    }

    public List<User> showCommonFriends(int userId, int friendId) {
        User user = inMemoryUserStorage.getUser(userId);
        User friend = inMemoryUserStorage.getUser(friendId);
        Set<Integer> userFriends = user.getFriends();
        Set<Integer> friendFriends = friend.getFriends();
        if (userFriends == null || friendFriends == null) {
            return Collections.EMPTY_LIST;
        }
        Set<Integer> commonFriends = new HashSet<>();
        for (Integer thisUser : userFriends) {
            if (friendFriends.contains(thisUser)) {
                commonFriends.add(thisUser);
            }
        }
        return inMemoryUserStorage.getUsers(commonFriends);
    }

    public List<User> findAll() {
        return inMemoryUserStorage.findAll();
    }

    public User getUser(int userId) {
        return inMemoryUserStorage.getUser(userId);
    }

    public User create(User user) {
        return inMemoryUserStorage.create(user);
    }


    public User update(User user) {
        return inMemoryUserStorage.update(user);
    }

    public void delete(User user) {
        Integer userId = user.getId();
        for (Integer friendId : user.getFriends()) {
            User friend = inMemoryUserStorage.getUser(friendId);
            Set<Integer> friendFriends = friend.getFriends();
            friendFriends.remove(userId);
            friend.setFriends(friendFriends);
        }
        inMemoryUserStorage.delete(user);
    }

    public List<User> getFriends(int id) {
        User user = inMemoryUserStorage.getUser(id);
        List<User> friends = new ArrayList<>();
        for (Integer friendId : user.getFriends()) {
            User friend = inMemoryUserStorage.getUser(friendId);
            friends.add(friend);
        }
        return friends;
    }
}