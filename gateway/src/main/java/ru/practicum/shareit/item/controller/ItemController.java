package ru.practicum.shareit.item.controller;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.marker.Create;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping("{id}")
    public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send get request /items/{}", id);
        return itemClient.findById(userId, id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findAllByText(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @RequestParam(value = "text") String text) {
        log.info("Send get request /items/search?text={}", text);
        return text.isBlank() ? ResponseEntity.ok(Collections.emptyList()) : itemClient.findAllByText(userId, text);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send get request /items");
        return itemClient.findAll(userId);
    }

    @PostMapping
    public ResponseEntity<Object> save(@Validated({Create.class}) @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send post request /items");
        return itemClient.save(itemDto, userId);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> saveComment(@Validated({Create.class}) @RequestBody CommentDto commentDto,
                                              @RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send post request /items/{}/comment", id);
        return itemClient.saveComment(commentDto, userId, id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send patch request /items/{}", id);
        return itemClient.update(itemDto, userId, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send delete request /items/{}", id);
        return itemClient.deleteById(userId, id);
    }
}
