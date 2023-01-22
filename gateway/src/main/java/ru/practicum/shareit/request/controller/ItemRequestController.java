package ru.practicum.shareit.request.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.marker.Create;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send get request /requests/{}", id);
        return requestClient.findById(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send get request /requests");
        return requestClient.findAll(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findByPage(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                             @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Send get request /requests/all?from={}&size={}", from, size);
        return requestClient.findByPage(userId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> save(@Validated({Create.class}) @RequestBody ItemRequestDto requestDto,
                                       @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send post request /requests");
        return requestClient.save(requestDto, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send delete request /requests/{}", id);
        return requestClient.deleteById(userId, id);
    }
}
