package ru.practicum.shareit.user;

import javax.validation.constraints.NotNull;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.BookerDto;

public class UserMapper {
    public static UserDto toUserDto(@NotNull User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static BookerDto toBookerDto(@NotNull User user) {
        return new BookerDto(
                user.getId()
        );
    }

    public static User toUser(@NotNull UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }
}
