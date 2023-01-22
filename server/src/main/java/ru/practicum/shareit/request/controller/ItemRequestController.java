package ru.practicum.shareit.request.controller;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @GetMapping("/{id}")
    public ItemRequestDto findById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send get request /requests/{}", id);
        return itemRequestService.findById(userId, id);
    }

    @GetMapping
    public List<ItemRequestDto> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send get request /requests");
        return itemRequestService.findAll(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findByPage(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(name = "from", defaultValue = "0") Integer from,
                                           @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Send get request /requests/all?from={}&size={}", from, size);
        return itemRequestService.findByPage(userId, from, size);
    }

    @PostMapping
    public ItemRequestDto save(@RequestBody ItemRequestDto requestDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Send post request /requests");
        return itemRequestService.save(requestDto, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long id) {
        log.info("Send delete request /requests/{}", id);
        itemRequestService.deleteById(userId, id);
    }
}
