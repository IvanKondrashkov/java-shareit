package ru.practicum.shareit.item.controller;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.marker.Create;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("{id}")
    public ItemDto findById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send get request /items/{}", id);
        return itemService.findById(userId, id);
    }

    @GetMapping("/search")
    public List<ItemDto> findByKeyWord(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam(value = "text") String text) {
        log.info("Send get request /items/search?text={}", text);
        return itemService.findByKeyWord(userId, text);
    }

    @GetMapping
    public List<ItemDto> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send get request /items");
        return itemService.findAll(userId);
    }

    @PostMapping
    public ItemDto save(@Validated({Create.class}) @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send post request /items");
        return itemService.save(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send patch request /items/{}", id);
        return itemService.update(itemDto, userId, id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send delete request /items/{}", id);
        itemService.deleteById(userId, id);
    }
}
