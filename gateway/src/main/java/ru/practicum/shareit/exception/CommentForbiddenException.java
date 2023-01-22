package ru.practicum.shareit.exception;

public class CommentForbiddenException extends RuntimeException {
    public CommentForbiddenException(String message) {
        super(message);
    }

    public CommentForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
