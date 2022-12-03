package ru.practicum.shareit.exception;

public class FieldWithoutException extends RuntimeException {
    public FieldWithoutException(String message) {
        super(message);
    }

    public FieldWithoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
