package ru.practicum.shareit.booking.controller;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.marker.Create;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{id}")
    public BookingInfoDto findById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send get request /bookings/{}", id);
        return bookingService.findById(userId, id);
    }

    @GetMapping
    public List<BookingInfoDto> findAllByBooker(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam(name = "state", defaultValue = "ALL") String state) {
        log.info("Send get request /bookings?state={}", state);
        return bookingService.findAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingInfoDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam(name = "state", defaultValue = "ALL") String state) {
        log.info("Send get request /bookings/owner?state={}", state);
        return bookingService.findAllByOwner(userId, state);
    }

    @PostMapping
    public BookingInfoDto save(@Validated({Create.class}) @RequestBody BookingDto bookingDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send post request /bookings");
        return bookingService.save(bookingDto, userId);
    }

    @PatchMapping("/{id}")
    public BookingInfoDto update(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id, @RequestParam(name = "approved") String approved) {
        log.info("Send patch request /bookings/{}?approved={}", id, approved);
        return bookingService.update(userId, id, approved);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send delete request /bookings/{}", id);
        bookingService.deleteById(userId, id);
    }
}
