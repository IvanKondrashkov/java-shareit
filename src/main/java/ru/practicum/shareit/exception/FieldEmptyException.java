package ru.practicum.shareit.exception;

public class FieldEmptyException extends RuntimeException {
    public FieldEmptyException(String message) {
        super(message);
    }

    public FieldEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
