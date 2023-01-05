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
    public List<BookingInfoDto> findAllByBookerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        log.info("Send get request /bookings?state={}&from={}&size={}", state, from, size);
        return bookingService.findAllByBookerId(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingInfoDto> findAllByItemOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                     @RequestParam(defaultValue = "0") Integer from,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        log.info("Send get request /bookings/owner?state={}&from={}&size={}", state, from, size);
        return bookingService.findAllByItemOwnerId(userId, state, from, size);
    }

    @PostMapping
    public BookingInfoDto save(@Validated({Create.class}) @RequestBody BookingDto bookingDto,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send post request /bookings");
        return bookingService.save(bookingDto, userId);
    }

    @PatchMapping("/{id}")
    public BookingInfoDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long id, @RequestParam(name = "approved") String approved) {
        log.info("Send patch request /bookings/{}?approved={}", id, approved);
        return bookingService.update(userId, id, approved);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send delete request /bookings/{}", id);
        bookingService.deleteById(userId, id);
    }
}
