//package ru.yandex.practicum.filmorate.service;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.model.User;
//import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
//import ru.yandex.practicum.filmorate.storage.user.UserStorage;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class FilmService {
//    @Qualifier("filmStorage")
//    private FilmStorage filmStorage;
//    @Qualifier("userStorage")
//    private UserStorage userStorage;
//
//    public FilmService(FilmStorage filmDbStorage, UserStorage userDbStorage) {
//        this.filmStorage = filmDbStorage;
//        this.userStorage = userDbStorage;
//    }
//
//        public void addLike(int filmId, int userId) {
//            Optional<Film> filmOptional = filmStorage.getFilm(filmId);
//            Optional<User> userOptional = userStorage.getUser(userId);
//            if (filmOptional.isPresent() && userOptional.isPresent()) {
//                Film film = filmOptional.get();
//                User user = userOptional.get();
//                Set<User> thisFilmFans = film.getFans();
//                Set<Film> thisUserLikes = user.getLikes();
//                if (userFriends == null) {
//                    userFriends = new HashMap<>();
//                }
//                if (friendFriends.containsKey(userId)) {
//                    userFriends.put(friendId, true);
//                    friendFriends.remove(userId);
//                    friendFriends.put(userId, true);
//                } else {
//                    userFriends.put(friendId, false);
//                }
//            }
//            Optional<Film> film = filmStorage.getFilm(filmId);
//            Set<Integer> likes = film.getLikes();
//            if (filmStorage.getUser(userId) != null) {
//                likes.add(userId);
//            }
//        }
//
//        public void deleteLike(Integer filmId, Integer userId) {
//            Optional<Film> film = filmStorage.getFilm(filmId);
//            Optional<User> user = userStorage.getUser(userId);
//            Set<Integer> likes = film.getLikes();
//            likes.remove(user.getId());
//        }
//
//        public Map<Integer, ArrayList<Film>> likesAndFilms() {
//            Map<Integer, ArrayList<Film>> filmsByLikes = new HashMap<>();
//            for (Film film : filmStorage.findAll()) {
//                Integer countOfLikes = film.getLikes().size();
//                ArrayList<Film> thisLikes;
//                if (!filmsByLikes.containsKey(countOfLikes)) {
//                    thisLikes = new ArrayList<>();
//                } else {
//                    thisLikes = filmsByLikes.get(countOfLikes);
//                }
//                thisLikes.add(film);
//                filmsByLikes.put(countOfLikes, thisLikes);
//            }
//            return filmsByLikes;
//        }
//
//        public List<Film> showTopFilms(int limitOfTop) {
//            Map<Integer, ArrayList<Film>> filmsByLikes = likesAndFilms();
//            if (filmsByLikes.isEmpty()) {
//                return filmStorage.findAll().stream().limit(limitOfTop).collect(Collectors.toList());
//            } else {
//                Map<Integer, ArrayList<Film>> sortedMap = new TreeMap<>(Comparator.reverseOrder());
//                sortedMap.putAll(filmsByLikes);
//                return sortedMap.values().stream().flatMap(List::stream).limit(limitOfTop).collect(Collectors.toList());
//            }
//        }
//
//        public List<Film> findAll() {
//            return filmStorage.findAll();
//        }
//
//        public Optional<Film> getFilm(int filmId) {
//            return filmStorage.getFilm(filmId);
//        }
//
//        public Film create(Film film) {
//            return filmStorage.create(film);
//        }
//
//        public Film update(Film film) {
//            return filmStorage.update(film);
//        }
//
//        public void delete(Film film) {
//            filmStorage.delete(film);
//        }
//    }
//}
