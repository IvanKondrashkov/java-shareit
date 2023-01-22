package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.marker.Create;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send get request /bookings/{}", id);
        return bookingClient.findById(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByBookerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                    @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Send get request /bookings?state={}&from={}&size={}", state, from, size);
        final BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.findAllByBookerId(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findAllByItemOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                       @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                       @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Send get request /bookings/owner?state={}&from={}&size={}", state, from, size);
        final BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.findAllByItemOwnerId(userId, bookingState, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> save(@Validated({Create.class}) @RequestBody BookingDto bookingDto,
                                       @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send post request /bookings");
        return bookingClient.save(bookingDto, userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long id, @RequestParam(name = "approved") Boolean approved) {
        log.info("Send patch request /bookings/{}?approved={}", id, approved);
        return bookingClient.update(userId, id, approved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send delete request /bookings/{}", id);
        return bookingClient.deleteById(userId, id);
    }
}
