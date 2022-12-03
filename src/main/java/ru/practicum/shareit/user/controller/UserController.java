package ru.practicum.shareit.user.controller;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.marker.Create;
import ru.practicum.shareit.marker.Update;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        log.info("Send get request /users/{}", id);
        return userService.findById(id);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("Send get request /users");
        return userService.findAll();
    }

    @PostMapping
    public UserDto save(@Validated({Create.class}) @RequestBody UserDto userDto) {
        log.info("Send post request /users");
        return userService.save(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@Validated({Update.class}) @RequestBody UserDto userDto, @PathVariable Long id) {
        log.info("Send patch request /users");
        return userService.update(userDto, id);
    }

    @DeleteMapping({"/{id}"})
    public void deleteById(@PathVariable Long id) {
        log.info("Send delete request /users/{}", id);
        userService.deleteById(id);
    }
}
