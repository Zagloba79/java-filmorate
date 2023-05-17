//package ru.yandex.practicum.filmorate.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import ru.yandex.practicum.filmorate.model.User;
//import ru.yandex.practicum.filmorate.storage.user.UserStorage;
//
//import java.util.*;
//
//@Service
//@Slf4j
//public class UserService {
//    UserStorage inMemoryUserStorage;
//
//    @Autowired
//    public UserService(UserStorage inMemoryUserStorage) {
//        this.inMemoryUserStorage = inMemoryUserStorage;
//    }
//
//    public void addFriend(Integer userId, Integer friendId) {
//        User user = inMemoryUserStorage.getUser(userId);
//        User friend = inMemoryUserStorage.getUser(friendId);
//        Set<Integer> userFriends = user.getFriends();
//        if (userFriends == null) {
//            userFriends = new HashSet<>();
//        }
//        userFriends.add(friendId);
//        Set<Integer> friendFriends = friend.getFriends();
//        if (friendFriends == null) {
//            friendFriends = new HashSet<>();
//        }
//        friendFriends.add(userId);
//    }
//
//    public void argueFriends(Integer userId, Integer friendId) {
//        User user = inMemoryUserStorage.getUser(userId);
//        User friend = inMemoryUserStorage.getUser(friendId);
//        user.getFriends().remove(friendId);
//        friend.getFriends().remove(userId);
//    }
//
//    public List<User> showCommonFriends(int userId, int friendId) {
//        User user = inMemoryUserStorage.getUser(userId);
//        User friend = inMemoryUserStorage.getUser(friendId);
//        Set<Integer> userFriends = user.getFriends();
//        Set<Integer> friendFriends = friend.getFriends();
//        if (userFriends == null || friendFriends == null) {
//            return Collections.EMPTY_LIST;
//        }
//        Set<User> commonFriends = new HashSet<>();
//        for (Integer thisUser : userFriends) {
//            if (friendFriends.contains(thisUser)) {
//                commonFriends.add(inMemoryUserStorage.getUser(thisUser));
//            }
//        }
//        return new ArrayList<>(commonFriends);
//    }
//
//    public List<User> findAll() {
//        return inMemoryUserStorage.findAll();
//    }
//
//    public User getUser(int userId) {
//        return inMemoryUserStorage.getUser(userId);
//    }
//
//    public User create(User user) {
//        return inMemoryUserStorage.create(user);
//    }
//
//
//    public User update(User user) {
//        return inMemoryUserStorage.update(user);
//    }
//
//    public void delete(User user) {
//        inMemoryUserStorage.delete(user);
//    }
//
//    public List<User> getFriends(int id) {
//        User user = inMemoryUserStorage.getUser(id);
//        List<User> friends = new ArrayList<>();
//        for (Integer friendId : user.getFriends()) {
//            User friend = inMemoryUserStorage.getUser(friendId);
//            friends.add(friend);
//        }
//        return friends;
//    }
//}