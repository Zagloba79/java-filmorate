package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class ObjectNotFoundException extends RuntimeException {
    private final HttpStatus httpStatus;

    public ObjectNotFoundException(String s, HttpStatus httpStatus) {
        super(s);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ObjectNotFoundException(String s) {
        super(s);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public static ObjectNotFoundException createBadRequestException(String message) {
        return new ObjectNotFoundException(message, HttpStatus.BAD_REQUEST);
    }

    public static ObjectNotFoundException createNotFoundException(String message) {
        return new ObjectNotFoundException(message, HttpStatus.NOT_FOUND);
    }
}