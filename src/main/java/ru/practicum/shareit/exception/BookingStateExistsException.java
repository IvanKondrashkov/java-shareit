package ru.practicum.shareit.exception;

public class BookingStateExistsException extends RuntimeException {
    public BookingStateExistsException(String message) {
        super(message);
    }

    public BookingStateExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
