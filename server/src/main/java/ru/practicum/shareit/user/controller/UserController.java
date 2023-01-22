package ru.practicum.shareit.user.controller;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

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
    public UserDto save(@RequestBody UserDto userDto) {
        log.info("Send post request /users");
        return userService.save(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@RequestBody UserDto userDto, @PathVariable Long id) {
        log.info("Send patch request /users/{}", id);
        return userService.update(userDto, id);
    }

    @DeleteMapping({"/{id}"})
    public void deleteById(@PathVariable Long id) {
        log.info("Send delete request /users/{}", id);
        userService.deleteById(id);
    }
}
